package com.example.inventory_management.service;

import java.util.Locale;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.inventory_management.dto.UserRegistrationDto;
import com.example.inventory_management.exception.BadRequestException;
import com.example.inventory_management.model.Role;
import com.example.inventory_management.model.User;
import com.example.inventory_management.repository.RoleRepository;
import com.example.inventory_management.repository.UserRepository;

@Service
public class AuthService {

    private static final String DEFAULT_ROLE = "BUYER";
    private static final Set<String> ALLOWED_SELF_SIGNUP_ROLES = Set.of("BUYER", "SELLER");

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(UserRegistrationDto registrationDto) {
        if (userRepository.findByUsername(registrationDto.getUsername()).isPresent()) {
            throw new BadRequestException("Username already exists");
        }
        if (userRepository.findByEmail(registrationDto.getEmail()).isPresent()) {
            throw new BadRequestException("Email already exists");
        }

        String requestedRole = registrationDto.getRole();
        String roleToAssign = (requestedRole == null || requestedRole.isBlank())
                ? DEFAULT_ROLE
                : requestedRole.trim().toUpperCase(Locale.ROOT);

        if (!ALLOWED_SELF_SIGNUP_ROLES.contains(roleToAssign)) {
            throw new BadRequestException("Only BUYER or SELLER accounts can be self-registered");
        }

        User user = new User(
                registrationDto.getUsername(),
                registrationDto.getEmail(),
                passwordEncoder.encode(registrationDto.getPassword()));

        Role defaultRole = roleRepository.findByName(roleToAssign)
                .orElseGet(() -> roleRepository.save(new Role(roleToAssign)));

        user.addRole(defaultRole);
        return userRepository.save(user);
    }
}
