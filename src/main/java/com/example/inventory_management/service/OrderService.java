package com.example.inventory_management.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.inventory_management.dto.OrderCreateRequestDto;
import com.example.inventory_management.dto.OrderItemRequestDto;
import com.example.inventory_management.dto.OrderItemResponseDto;
import com.example.inventory_management.dto.OrderResponseDto;
import com.example.inventory_management.exception.BadRequestException;
import com.example.inventory_management.exception.ResourceNotFoundException;
import com.example.inventory_management.model.CustomerOrder;
import com.example.inventory_management.model.OrderItem;
import com.example.inventory_management.model.Product;
import com.example.inventory_management.model.User;
import com.example.inventory_management.repository.CustomerOrderRepository;
import com.example.inventory_management.repository.ProductRepository;
import com.example.inventory_management.repository.UserRepository;

@Service
public class OrderService {

    private final CustomerOrderRepository customerOrderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public OrderService(CustomerOrderRepository customerOrderRepository,
            UserRepository userRepository,
            ProductRepository productRepository) {
        this.customerOrderRepository = customerOrderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public OrderResponseDto placeOrder(String username, OrderCreateRequestDto request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BadRequestException("Order must contain at least one item");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        CustomerOrder order = new CustomerOrder(user);

        for (OrderItemRequestDto itemRequest : request.getItems()) {
            if (itemRequest.getQuantity() == null || itemRequest.getQuantity() <= 0) {
                throw new BadRequestException("Item quantity must be greater than zero");
            }

            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found with id: " + itemRequest.getProductId()));

            if (product.getStockQuantity() < itemRequest.getQuantity()) {
                throw new BadRequestException("Insufficient stock for product: " + product.getName());
            }

            product.setStockQuantity(product.getStockQuantity() - itemRequest.getQuantity());

            OrderItem item = new OrderItem(product, itemRequest.getQuantity(), product.getPrice());
            order.addOrderItem(item);
        }

        CustomerOrder saved = customerOrderRepository.save(order);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDto> getOrdersForUser(String username) {
        return customerOrderRepository.findByUserUsernameOrderByOrderDateDesc(username)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDto> getAllOrders() {
        return customerOrderRepository.findAllByOrderByOrderDateDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void cancelOrder(Long orderId, String requesterUsername, boolean isAdmin) {
        CustomerOrder order = customerOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        boolean isOwner = order.getUser().getUsername().equals(requesterUsername);
        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("You can only cancel your own orders");
        }

        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
        }

        customerOrderRepository.delete(order);
    }

    private OrderResponseDto toResponse(CustomerOrder order) {
        List<OrderItemResponseDto> items = order.getOrderItems().stream()
                .map(item -> new OrderItemResponseDto(
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()))))
                .toList();

        BigDecimal total = items.stream()
                .map(OrderItemResponseDto::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new OrderResponseDto(
                order.getId(),
                order.getOrderDate(),
                order.getUser().getUsername(),
                total,
                items);
    }
}
