package com.example.inventory_management.config;

import java.math.BigDecimal;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.inventory_management.model.Product;
import com.example.inventory_management.model.Role;
import com.example.inventory_management.model.User;
import com.example.inventory_management.repository.ProductRepository;
import com.example.inventory_management.repository.RoleRepository;
import com.example.inventory_management.repository.UserRepository;

@Component
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DemoDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoDataSeeder(UserRepository userRepository,
            RoleRepository roleRepository,
            ProductRepository productRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.productRepository = productRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Role adminRole = ensureRole("ADMIN");
        Role sellerRole = ensureRole("SELLER");
        Role buyerRole = ensureRole("BUYER");

        ensureUserWithRole("admin_demo", "admin_demo@example.com", "Admin@123", adminRole);
        ensureUserWithRole("seller_demo", "seller_demo@example.com", "Seller@123", sellerRole);
        ensureUserWithRole("buyer_demo", "buyer_demo@example.com", "Buyer@123", buyerRole);

        ensureProduct("Wireless Mouse", "Ergonomic 2.4G wireless mouse", new BigDecimal("29.99"), 40,
                "https://images.unsplash.com/photo-1527814050087-3793815479db?auto=format&fit=crop&w=800&q=80");
        ensureProduct("Mechanical Keyboard", "RGB backlit mechanical keyboard", new BigDecimal("89.00"), 25,
                "https://images.unsplash.com/photo-1511467687858-23d96c32e4ae?auto=format&fit=crop&w=800&q=80");
        ensureProduct("USB-C Hub", "7-in-1 USB-C adapter hub", new BigDecimal("49.50"), 30,
                "https://images.unsplash.com/photo-1588872657578-7efd1f1555ed?auto=format&fit=crop&w=800&q=80");
    }

    private Role ensureRole(String name) {
        return roleRepository.findByName(name).orElseGet(() -> roleRepository.save(new Role(name)));
    }

    private void ensureUserWithRole(String username, String email, String rawPassword, Role role) {
        User user = userRepository.findByUsername(username)
                .orElseGet(() -> new User(username, email, passwordEncoder.encode(rawPassword)));

        user.setEmail(email);

        // Keep seeded account passwords consistent across reruns.
        user.setPassword(passwordEncoder.encode(rawPassword));

        if (user.getRoles().stream().noneMatch(existingRole -> existingRole.getName().equals(role.getName()))) {
            user.addRole(role);
        }

        userRepository.save(user);
    }

    private void ensureProduct(String name, String description, BigDecimal price, Integer stockQuantity,
            String imageUrl) {
        Product product = productRepository.findByName(name)
                .orElseGet(() -> new Product(name, description, price, stockQuantity));

        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setStockQuantity(stockQuantity);
        product.setImageUrl(imageUrl);

        productRepository.save(product);
    }
}
