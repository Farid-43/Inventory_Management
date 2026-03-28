package com.example.inventory_management.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.example.inventory_management.model.Product;
import com.example.inventory_management.model.Role;
import com.example.inventory_management.model.User;
import com.example.inventory_management.repository.CustomerOrderRepository;
import com.example.inventory_management.repository.OrderItemRepository;
import com.example.inventory_management.repository.ProductRepository;
import com.example.inventory_management.repository.RoleRepository;
import com.example.inventory_management.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
class ControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CustomerOrderRepository customerOrderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @BeforeEach
    void setUp() {
        orderItemRepository.deleteAll();
        customerOrderRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
        productRepository.deleteAll();

        Role buyerRole = roleRepository.save(new Role("BUYER"));
        roleRepository.save(new Role("SELLER"));
        roleRepository.save(new Role("ADMIN"));

        User buyer = new User("buyer", "buyer@example.com", "encoded-password");
        buyer.addRole(buyerRole);
        userRepository.save(buyer);
    }

    @Test
    void unauthenticatedUser_cannotAccessProducts() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "seller", roles = { "SELLER" })
    void seller_canCreateProduct() throws Exception {
        String body = """
                {
                  "name": "Mouse",
                  "description": "Wireless mouse",
                  "price": 20.50,
                  "stockQuantity": 15
                }
                """;

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Mouse"));
    }

    @Test
    @WithMockUser(username = "buyer", roles = { "BUYER" })
    void buyer_cannotCreateProduct() throws Exception {
        String body = """
                {
                  "name": "Keyboard",
                  "description": "Mechanical",
                  "price": 70.00,
                  "stockQuantity": 10
                }
                """;

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void registerEndpoint_isPublic() throws Exception {
        String body = """
                {
                  "username": "newuser",
                  "email": "newuser@example.com",
                  "password": "password123"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("newuser"));
    }

    @Test
    @WithMockUser(username = "buyer", roles = { "BUYER" })
    void buyer_canPlaceAndFetchOwnOrders() throws Exception {
        Product product = productRepository.save(new Product(
                "Laptop",
                "Lightweight",
                new BigDecimal("1000.00"),
                5));

        String placeOrderBody = """
                {
                  "items": [
                    {
                      "productId": %d,
                      "quantity": 2
                    }
                  ]
                }
                """.formatted(product.getId());

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(placeOrderBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.items[0].productId").value(product.getId()))
                .andExpect(jsonPath("$.totalAmount").value(2000.00));

        mockMvc.perform(get("/api/orders/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].items[0].quantity").value(2));
    }
}
