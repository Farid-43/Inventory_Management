package com.example.inventory_management.dto;

import java.util.ArrayList;
import java.util.List;

public class OrderCreateRequestDto {

    private List<OrderItemRequestDto> items = new ArrayList<>();

    public OrderCreateRequestDto() {
    }

    public OrderCreateRequestDto(List<OrderItemRequestDto> items) {
        this.items = items;
    }

    public List<OrderItemRequestDto> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequestDto> items) {
        this.items = items;
    }
}
