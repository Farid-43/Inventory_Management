package com.example.inventory_management.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.inventory_management.dto.UserRolesUpdateRequestDto;
import com.example.inventory_management.model.User;
import com.example.inventory_management.service.AuthService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final AuthService authService;

    public UserController(AuthService authService) {
        this.authService = authService;
    }

    @PutMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> updateUserRoles(@PathVariable Long id, @RequestBody UserRolesUpdateRequestDto request) {
        User updated = authService.updateUserRoles(id, request.getRoles());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", updated.getId());
        body.put("username", updated.getUsername());
        body.put("roles", updated.getRoles().stream().map(role -> role.getName()).toList());
        return body;
    }
}
