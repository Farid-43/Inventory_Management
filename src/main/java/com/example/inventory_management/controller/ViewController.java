package com.example.inventory_management.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.inventory_management.dto.UserRegistrationDto;
import com.example.inventory_management.service.AuthService;

@Controller
public class ViewController {

    private final AuthService authService;

    public ViewController(AuthService authService) {
        this.authService = authService;
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
        // Placeholder flow: request accepted and user redirected to login.
        return "redirect:/login?resetRequested";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String username,
            @RequestParam String email,
            @RequestParam String password) {
        authService.registerUser(new UserRegistrationDto(username, email, password));
        return "redirect:/login?registered";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        model.addAttribute("username", principal.getName());
        return "dashboard";
    }
}
