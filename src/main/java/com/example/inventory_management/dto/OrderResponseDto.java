package com.example.inventory_management.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderResponseDto {

    private Long id;
    private LocalDateTime orderDate;
    private String username;
    private BigDecimal totalAmount;
    private List<OrderItemResponseDto> items = new ArrayList<>();

    public OrderResponseDto() {
    }

    public OrderResponseDto(Long id, LocalDateTime orderDate, String username, BigDecimal totalAmount,
            List<OrderItemResponseDto> items) {
        this.id = id;
        this.orderDate = orderDate;
        this.username = username;
        this.totalAmount = totalAmount;
        this.items = items;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public List<OrderItemResponseDto> getItems() {
        return items;
    }

    public void setItems(List<OrderItemResponseDto> items) {
        this.items = items;
    }
}
