package com.example.inventory_management.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.inventory_management.model.CustomerOrder;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {
    List<CustomerOrder> findByUserUsernameOrderByOrderDateDesc(String username);

    List<CustomerOrder> findAllByOrderByOrderDateDesc();
}
