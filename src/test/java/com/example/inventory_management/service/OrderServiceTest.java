package com.example.inventory_management.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.example.inventory_management.dto.OrderCreateRequestDto;
import com.example.inventory_management.dto.OrderItemRequestDto;
import com.example.inventory_management.dto.OrderResponseDto;
import com.example.inventory_management.exception.BadRequestException;
import com.example.inventory_management.exception.ResourceNotFoundException;
import com.example.inventory_management.model.CustomerOrder;
import com.example.inventory_management.model.Product;
import com.example.inventory_management.model.User;
import com.example.inventory_management.repository.CustomerOrderRepository;
import com.example.inventory_management.repository.ProductRepository;
import com.example.inventory_management.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private CustomerOrderRepository customerOrderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void placeOrder_throwsWhenItemsEmpty() {
        OrderCreateRequestDto request = new OrderCreateRequestDto();
        request.setItems(List.of());

        assertThatThrownBy(() -> orderService.placeOrder("buyer", request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("at least one item");

        verifyNoInteractions(userRepository, productRepository, customerOrderRepository);
    }

    @Test
    void placeOrder_throwsWhenUserMissing() {
        OrderCreateRequestDto request = new OrderCreateRequestDto(List.of(new OrderItemRequestDto(1L, 1)));
        when(userRepository.findByUsername("buyer")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.placeOrder("buyer", request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void placeOrder_throwsWhenQuantityInvalid() {
        OrderCreateRequestDto request = new OrderCreateRequestDto(List.of(new OrderItemRequestDto(1L, 0)));
        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(new User("buyer", "b@x.com", "pw")));

        assertThatThrownBy(() -> orderService.placeOrder("buyer", request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("quantity must be greater than zero");
    }

    @Test
    void placeOrder_throwsWhenProductMissing() {
        OrderCreateRequestDto request = new OrderCreateRequestDto(List.of(new OrderItemRequestDto(99L, 1)));
        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(new User("buyer", "b@x.com", "pw")));
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.placeOrder("buyer", request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found");
    }

    @Test
    void placeOrder_throwsWhenInsufficientStock() {
        Product product = new Product("Laptop", "Lightweight", new BigDecimal("1000.00"), 1);
        OrderCreateRequestDto request = new OrderCreateRequestDto(List.of(new OrderItemRequestDto(1L, 2)));

        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(new User("buyer", "b@x.com", "pw")));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> orderService.placeOrder("buyer", request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Insufficient stock");
    }

    @Test
    void placeOrder_reducesStockAndReturnsTotal() {
        Product product = new Product("Laptop", "Lightweight", new BigDecimal("1000.00"), 5);
        OrderCreateRequestDto request = new OrderCreateRequestDto(List.of(new OrderItemRequestDto(1L, 2)));

        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(new User("buyer", "b@x.com", "pw")));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(customerOrderRepository.save(any(CustomerOrder.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponseDto response = orderService.placeOrder("buyer", request);

        assertThat(product.getStockQuantity()).isEqualTo(3);
        assertThat(response.getUsername()).isEqualTo("buyer");
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getQuantity()).isEqualTo(2);
        assertThat(response.getTotalAmount()).isEqualByComparingTo("2000.00");

        verify(customerOrderRepository).save(any(CustomerOrder.class));
    }

    @Test
    void cancelOrder_throwsWhenOrderMissing() {
        when(customerOrderRepository.findById(44L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.cancelOrder(44L, "buyer", false))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Order not found");
    }

    @Test
    void cancelOrder_rejectsNonOwnerBuyer() {
        Product product = new Product("Laptop", "Lightweight", new BigDecimal("1000.00"), 3);
        CustomerOrder order = new CustomerOrder(new User("buyer1", "b1@example.com", "pw"));
        order.addOrderItem(new com.example.inventory_management.model.OrderItem(product, 2, new BigDecimal("1000.00")));

        when(customerOrderRepository.findById(10L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder(10L, "buyer2", false))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("cancel your own orders");

        assertThat(product.getStockQuantity()).isEqualTo(3);
        verify(customerOrderRepository, never()).delete(any(CustomerOrder.class));
    }

    @Test
    void cancelOrder_restoresStockAndDeletesOrderForOwner() {
        Product product = new Product("Laptop", "Lightweight", new BigDecimal("1000.00"), 3);
        CustomerOrder order = new CustomerOrder(new User("buyer", "buyer@example.com", "pw"));
        order.addOrderItem(new com.example.inventory_management.model.OrderItem(product, 2, new BigDecimal("1000.00")));

        when(customerOrderRepository.findById(11L)).thenReturn(Optional.of(order));

        orderService.cancelOrder(11L, "buyer", false);

        assertThat(product.getStockQuantity()).isEqualTo(5);
        verify(customerOrderRepository).delete(order);
    }

    @Test
    void cancelOrder_allowsAdminToCancelAnyOrder() {
        Product product = new Product("Laptop", "Lightweight", new BigDecimal("1000.00"), 2);
        CustomerOrder order = new CustomerOrder(new User("buyer1", "b1@example.com", "pw"));
        order.addOrderItem(new com.example.inventory_management.model.OrderItem(product, 1, new BigDecimal("1000.00")));

        when(customerOrderRepository.findById(12L)).thenReturn(Optional.of(order));

        orderService.cancelOrder(12L, "admin", true);

        assertThat(product.getStockQuantity()).isEqualTo(3);
        verify(customerOrderRepository).delete(order);
    }
}
