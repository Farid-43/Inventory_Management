package com.example.inventory_management.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.inventory_management.dto.OrderCreateRequestDto;
import com.example.inventory_management.dto.OrderResponseDto;
import com.example.inventory_management.service.OrderService;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','SELLER','BUYER')")
    public OrderResponseDto placeOrder(@RequestBody OrderCreateRequestDto request, Principal principal) {
        return orderService.placeOrder(principal.getName(), request);
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('ADMIN','SELLER','BUYER')")
    public List<OrderResponseDto> getMyOrders(Principal principal) {
        return orderService.getOrdersForUser(principal.getName());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SELLER')")
    public List<OrderResponseDto> getAllOrders() {
        return orderService.getAllOrders();
    }
}
