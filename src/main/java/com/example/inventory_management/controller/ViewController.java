package com.example.inventory_management.controller;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Comparator;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.inventory_management.dto.OrderCreateRequestDto;
import com.example.inventory_management.dto.OrderItemRequestDto;
import com.example.inventory_management.dto.ProductRequestDto;
import com.example.inventory_management.dto.UserRegistrationDto;
import com.example.inventory_management.exception.BadRequestException;
import com.example.inventory_management.model.User;
import com.example.inventory_management.repository.RoleRepository;
import com.example.inventory_management.repository.UserRepository;
import com.example.inventory_management.service.AuthService;
import com.example.inventory_management.service.ImageStorageService;
import com.example.inventory_management.service.OrderService;
import com.example.inventory_management.service.ProductService;

@Controller
public class ViewController {

    private final AuthService authService;
    private final ProductService productService;
    private final OrderService orderService;
    private final ImageStorageService imageStorageService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public ViewController(AuthService authService,
            ProductService productService,
            OrderService orderService,
            ImageStorageService imageStorageService,
            UserRepository userRepository,
            RoleRepository roleRepository) {
        this.authService = authService;
        this.productService = productService;
        this.orderService = orderService;
        this.imageStorageService = imageStorageService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/dashboard";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPasswordRequest(@RequestParam("emailOrUsername") String emailOrUsername) {
        if (emailOrUsername == null || emailOrUsername.isBlank()) {
            return "redirect:/forgot-password?error";
        }
        return "redirect:/login?resetRequested";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam(value = "role", required = false) String role) {
        try {
            authService.registerUser(new UserRegistrationDto(username, email, password, role));
            return "redirect:/login?registered";
        } catch (BadRequestException ex) {
            return "redirect:/register?error=" + URLEncoder.encode(ex.getMessage(), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            return "redirect:/register?error="
                    + URLEncoder.encode("Registration failed. Please try again.", StandardCharsets.UTF_8);
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        model.addAttribute("username", principal.getName());
        try {
            model.addAttribute("productCount", productService.getAllProducts().size());
        } catch (Exception e) {
            model.addAttribute("productCount", 0);
        }
        try {
            model.addAttribute("myOrderCount", orderService.getOrdersForUser(principal.getName()).size());
        } catch (Exception e) {
            model.addAttribute("myOrderCount", 0);
        }
        try {
            model.addAttribute("allOrderCount", orderService.getAllOrders().size());
        } catch (Exception e) {
            model.addAttribute("allOrderCount", 0);
        }
        try {
            model.addAttribute("roleCount", roleRepository.count());
        } catch (Exception e) {
            model.addAttribute("roleCount", 0);
        }
        try {
            model.addAttribute("userCount", userRepository.count());
        } catch (Exception e) {
            model.addAttribute("userCount", 0);
        }
        try {
            model.addAttribute("adminUserCount", userRepository.countByRoles_Name("ADMIN"));
            model.addAttribute("sellerUserCount", userRepository.countByRoles_Name("SELLER"));
            model.addAttribute("buyerUserCount", userRepository.countByRoles_Name("BUYER"));
        } catch (Exception e) {
            model.addAttribute("adminUserCount", 0);
            model.addAttribute("sellerUserCount", 0);
            model.addAttribute("buyerUserCount", 0);
        }
        return "dashboard";
    }

    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminUsers(Model model, Principal principal) {
        List<User> users = userRepository.findAll().stream()
                .sorted(Comparator.comparing(User::getUsername))
                .toList();

        model.addAttribute("username", principal.getName());
        model.addAttribute("users", users);
        model.addAttribute("totalUsers", users.size());
        model.addAttribute("adminUserCount", userRepository.countByRoles_Name("ADMIN"));
        model.addAttribute("sellerUserCount", userRepository.countByRoles_Name("SELLER"));
        model.addAttribute("buyerUserCount", userRepository.countByRoles_Name("BUYER"));
        return "admin-users";
    }

    @GetMapping("/products")
    public String products(Model model, Principal principal,
            @RequestParam(value = "created", required = false) String created,
            @RequestParam(value = "deleted", required = false) String deleted,
            @RequestParam(value = "error", required = false) String error) {
        model.addAttribute("username", principal != null ? principal.getName() : "user");
        try {
            model.addAttribute("products", productService.getAllProducts());
        } catch (Exception ex) {
            model.addAttribute("products", List.of());
            if (error == null || error.isBlank()) {
                error = "Products could not be loaded right now";
            }
        }
        model.addAttribute("created", created != null);
        model.addAttribute("deleted", deleted != null);
        model.addAttribute("error", error);
        return "products";
    }

    @PostMapping("/products")
    @PreAuthorize("hasAnyRole('ADMIN','SELLER')")
    public String createProduct(@RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam BigDecimal price,
            @RequestParam Integer stockQuantity,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestParam(value = "imageUrl", required = false) String imageUrl) {
        try {
            String uploadedImageUrl = imageStorageService.storeProductImage(imageFile);
            String resolvedImageUrl = uploadedImageUrl != null
                    ? uploadedImageUrl
                    : imageStorageService.normalizeExternalImageUrl(imageUrl);
            ProductRequestDto request = new ProductRequestDto(name, description, price, stockQuantity);
            request.setImageUrl(resolvedImageUrl);
            productService.createProduct(request);
            return "redirect:/products?created";
        } catch (Exception ex) {
            return "redirect:/products?error="
                    + URLEncoder.encode("Unable to create product: " + ex.getMessage(), StandardCharsets.UTF_8);
        }
    }

    @PostMapping("/products/delete/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SELLER')")
    public String deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return "redirect:/products?deleted";
        } catch (Exception ex) {
            return "redirect:/products?error=" + URLEncoder.encode("Unable to delete product", StandardCharsets.UTF_8);
        }
    }

    @GetMapping("/orders")
    @PreAuthorize("hasAnyRole('ADMIN','SELLER')")
    public String allOrders(Model model, Principal principal,
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "placed", required = false) String placed) {
        model.addAttribute("username", principal.getName());
        model.addAttribute("orders", orderService.getAllOrders());
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("title", "All Orders");
        model.addAttribute("placed", placed != null);
        model.addAttribute("error", error);
        return "orders";
    }

    @GetMapping("/orders/me")
    @PreAuthorize("hasAnyRole('ADMIN','SELLER','BUYER')")
    public String myOrders(Model model, Principal principal,
            Authentication authentication,
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "placed", required = false) String placed,
            @RequestParam(value = "productId", required = false) Long productId,
            @RequestParam(value = "quantity", required = false) Integer quantity) {
        boolean isBuyer = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_BUYER".equals(authority.getAuthority()));

        if (!isBuyer) {
            return "redirect:/orders";
        }

        model.addAttribute("username", principal.getName());
        model.addAttribute("orders", orderService.getOrdersForUser(principal.getName()));
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("title", "My Orders");
        model.addAttribute("placed", placed != null);
        model.addAttribute("error", error);
        model.addAttribute("preselectedProductId", productId);
        model.addAttribute("preselectedQuantity", quantity);
        return "orders";
    }

    @PostMapping("/orders")
    @PreAuthorize("hasRole('BUYER')")
    public String placeOrder(@RequestParam Long productId,
            @RequestParam Integer quantity,
            Principal principal) {
        try {
            OrderCreateRequestDto request = new OrderCreateRequestDto();
            request.getItems().add(new OrderItemRequestDto(productId, quantity));
            orderService.placeOrder(principal.getName(), request);
            return "redirect:/orders/me?placed";
        } catch (BadRequestException ex) {
            return "redirect:/orders/me?error=" + URLEncoder.encode(ex.getMessage(), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            return "redirect:/orders/me?error=" + URLEncoder.encode("Unable to place order", StandardCharsets.UTF_8);
        }
    }
}
