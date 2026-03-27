package com.example.inventory_management;

import com.example.inventory_management.model.CustomerOrder;
import com.example.inventory_management.model.OrderItem;
import com.example.inventory_management.model.Product;
import com.example.inventory_management.model.Role;
import com.example.inventory_management.model.User;
import com.example.inventory_management.repository.CustomerOrderRepository;
import com.example.inventory_management.repository.ProductRepository;
import com.example.inventory_management.repository.RoleRepository;
import com.example.inventory_management.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class InventoryDataJpaTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CustomerOrderRepository customerOrderRepository;

    @Test
    void shouldSaveUserWithRoleRelationship() {
        Role adminRole = roleRepository.save(new Role("ADMIN"));

        User user = new User("john", "john@example.com", "secret");
        user.addRole(adminRole);

        User savedUser = userRepository.save(user);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getRoles()).hasSize(1);
        assertThat(savedUser.getRoles().iterator().next().getName()).isEqualTo("ADMIN");
    }

    @Test
    void shouldSaveOrderWithOrderItemsAndProductRelationship() {
        User user = userRepository.save(new User("seller", "seller@example.com", "secret"));
        Product product = productRepository.save(
                new Product("Keyboard", "Mechanical keyboard", new BigDecimal("49.99"), 20)
        );

        CustomerOrder order = new CustomerOrder(user);
        OrderItem orderItem = new OrderItem(product, 2, product.getPrice());
        order.addOrderItem(orderItem);

        CustomerOrder savedOrder = customerOrderRepository.save(order);

        assertThat(savedOrder.getId()).isNotNull();
        assertThat(savedOrder.getOrderItems()).hasSize(1);
        assertThat(savedOrder.getOrderItems().get(0).getId()).isNotNull();
        assertThat(savedOrder.getOrderItems().get(0).getProduct().getName()).isEqualTo("Keyboard");
    }
}
