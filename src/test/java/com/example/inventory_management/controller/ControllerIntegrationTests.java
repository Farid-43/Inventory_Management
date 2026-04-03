package com.example.inventory_management.controller;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
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

import com.example.inventory_management.model.CustomerOrder;
import com.example.inventory_management.model.OrderItem;
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
    void loginPage_isPublicAndReturnsLoginView() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void registerPage_isPublicAndReturnsRegisterView() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"));
    }

    @Test
    void forgotPasswordPage_isPublicAndReturnsView() throws Exception {
        mockMvc.perform(get("/forgot-password"))
                .andExpect(status().isOk())
                .andExpect(view().name("forgot-password"));
    }

    @Test
    void forgotPasswordSubmit_redirectsToLoginMessage() throws Exception {
        mockMvc.perform(post("/forgot-password")
                .with(csrf())
                .param("emailOrUsername", "buyer@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?resetRequested"));
    }

    @Test
    @WithMockUser(username = "buyer", roles = { "BUYER" })
    void dashboard_forAuthenticatedUser_returnsDashboardView() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attribute("username", "buyer"));
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
    void registerEndpoint_acceptsSellerRole() throws Exception {
        String body = """
                {
                  "username": "seller-signup",
                  "email": "seller-signup@example.com",
                  "password": "password123",
                  "role": "SELLER"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roles", hasItem("SELLER")));
    }

    @Test
    void registerEndpoint_rejectsAdminRole() throws Exception {
        String body = """
                {
                  "username": "admin-signup",
                  "email": "admin-signup@example.com",
                  "password": "password123",
                  "role": "ADMIN"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Only BUYER or SELLER accounts can be self-registered"));
    }

    @Test
    void registerForm_buyerRedirectsToDashboard() throws Exception {
        mockMvc.perform(post("/register")
                .with(csrf())
                .param("username", "buyer-form")
                .param("email", "buyer-form@example.com")
                .param("password", "password123")
                .param("role", "BUYER"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    @Test
    void registerForm_sellerRedirectsToDashboard() throws Exception {
        mockMvc.perform(post("/register")
                .with(csrf())
                .param("username", "seller-form")
                .param("email", "seller-form@example.com")
                .param("password", "password123")
                .param("role", "SELLER"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
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

    @Test
    @WithMockUser(username = "seller", roles = { "SELLER" })
    void seller_cannotPlaceOrdersViaApi() throws Exception {
        String placeOrderBody = """
                {
                        "items": [
                                {
                                        "productId": 1,
                                        "quantity": 1
                                }
                        ]
                }
                """;

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(placeOrderBody))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "buyer", roles = { "BUYER" })
    void buyer_canCancelOwnOrderAndStockIsRestored() throws Exception {
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
                .andExpect(status().isCreated());

        Long orderId = customerOrderRepository.findByUserUsernameOrderByOrderDateDesc("buyer").get(0).getId();

        mockMvc.perform(delete("/api/orders/{id}", orderId))
                .andExpect(status().isNoContent());

        assertThat(customerOrderRepository.findById(orderId)).isEmpty();
        assertThat(productRepository.findById(product.getId()).orElseThrow().getStockQuantity()).isEqualTo(5);
    }

    @Test
    @WithMockUser(username = "buyer2", roles = { "BUYER" })
    void buyer_cannotCancelAnotherUsersOrder() throws Exception {
        Product product = productRepository.save(new Product(
                "Laptop",
                "Lightweight",
                new BigDecimal("1000.00"),
                3));

        User buyer = userRepository.findByUsername("buyer").orElseThrow();
        CustomerOrder order = new CustomerOrder(buyer);
        order.addOrderItem(new OrderItem(product, 2, new BigDecimal("1000.00")));
        CustomerOrder savedOrder = customerOrderRepository.save(order);

        mockMvc.perform(delete("/api/orders/{id}", savedOrder.getId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access denied"));

        assertThat(customerOrderRepository.findById(savedOrder.getId())).isPresent();
    }

    @Test
    @WithMockUser(username = "seller", roles = { "SELLER" })
    void seller_cannotCancelOrdersViaApi() throws Exception {
        mockMvc.perform(delete("/api/orders/{id}", 999L))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void admin_canCancelAnyOrderAndStockIsRestored() throws Exception {
        Product product = productRepository.save(new Product(
                "Laptop",
                "Lightweight",
                new BigDecimal("1000.00"),
                4));
        product.setStockQuantity(2);
        productRepository.save(product);

        User buyer = userRepository.findByUsername("buyer").orElseThrow();
        CustomerOrder order = new CustomerOrder(buyer);
        order.addOrderItem(new OrderItem(product, 2, new BigDecimal("1000.00")));
        CustomerOrder savedOrder = customerOrderRepository.save(order);

        mockMvc.perform(delete("/api/orders/{id}", savedOrder.getId()))
                .andExpect(status().isNoContent());

        assertThat(customerOrderRepository.findById(savedOrder.getId())).isEmpty();
        assertThat(productRepository.findById(product.getId()).orElseThrow().getStockQuantity()).isEqualTo(4);
    }

    @Test
    @WithMockUser(username = "seller", roles = { "SELLER" })
    void seller_cannotPlaceOrdersViaMvcForm() throws Exception {
        mockMvc.perform(post("/orders")
                .with(csrf())
                .param("productId", "1")
                .param("quantity", "1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "seller", roles = { "SELLER" })
    void seller_myOrdersPathRedirectsToAllOrders() throws Exception {
        mockMvc.perform(get("/orders/me"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders"));
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void admin_canAccessUserManagementPage() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-users"));
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void admin_canUpdateUserRolesViaApi() throws Exception {
        Role buyerRole = roleRepository.findByName("BUYER").orElseThrow();

        User targetUser = new User("role-target", "role-target@example.com", "encoded-password");
        targetUser.getRoles().add(buyerRole);
        targetUser = userRepository.save(targetUser);

        String updateBody = """
                {
                  "roles": ["BUYER", "SELLER"]
                }
                """;

        mockMvc.perform(put("/api/users/{id}/roles", targetUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("role-target"))
                .andExpect(jsonPath("$.roles", hasItems("BUYER", "SELLER")));

        User updated = userRepository.findById(targetUser.getId()).orElseThrow();
        assertThat(updated.getRoles())
                .extracting(Role::getName)
                .containsExactlyInAnyOrder("BUYER", "SELLER");
    }

    @Test
    @WithMockUser(username = "seller", roles = { "SELLER" })
    void seller_cannotUpdateUserRolesViaApi() throws Exception {
        Role buyerRole = roleRepository.findByName("BUYER").orElseThrow();

        User targetUser = new User("blocked-target", "blocked-target@example.com", "encoded-password");
        targetUser.getRoles().add(buyerRole);
        targetUser = userRepository.save(targetUser);

        String updateBody = """
                {
                  "roles": ["BUYER", "SELLER"]
                }
                """;

        mockMvc.perform(put("/api/users/{id}/roles", targetUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateBody))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void admin_cannotRemoveLastAdminRole() throws Exception {
        Role adminRole = roleRepository.findByName("ADMIN").orElseThrow();

        User onlyAdmin = new User("only-admin", "only-admin@example.com", "encoded-password");
        onlyAdmin.getRoles().add(adminRole);
        onlyAdmin = userRepository.save(onlyAdmin);

        String updateBody = """
                {
                  "roles": ["BUYER"]
                }
                """;

        mockMvc.perform(put("/api/users/{id}/roles", onlyAdmin.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("At least one ADMIN user must remain in the system"));
    }

    @Test
    @WithMockUser(username = "seller", roles = { "SELLER" })
    void seller_cannotAccessUserManagementPage() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isForbidden());
    }
}
