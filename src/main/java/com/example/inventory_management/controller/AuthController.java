package com.example.inventory_management.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.inventory_management.dto.UserRegistrationDto;
import com.example.inventory_management.model.User;
import com.example.inventory_management.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody UserRegistrationDto request) {
        User created = authService.registerUser(request);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", created.getId());
        body.put("username", created.getUsername());
        body.put("email", created.getEmail());
        body.put("roles", created.getRoles().stream().map(role -> role.getName()).toList());

        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }
}
