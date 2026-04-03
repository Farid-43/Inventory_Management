package com.example.inventory_management.service;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.inventory_management.dto.UserRegistrationDto;
import com.example.inventory_management.exception.BadRequestException;
import com.example.inventory_management.exception.ResourceNotFoundException;
import com.example.inventory_management.model.Role;
import com.example.inventory_management.model.User;
import com.example.inventory_management.repository.RoleRepository;
import com.example.inventory_management.repository.UserRepository;

@Service
public class AuthService {

    private static final String DEFAULT_ROLE = "BUYER";
    private static final Set<String> ALLOWED_SELF_SIGNUP_ROLES = Set.of("BUYER", "SELLER");
    private static final Set<String> MANAGEABLE_ROLES = Set.of("ADMIN", "SELLER", "BUYER");

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

    @Transactional
    public User updateUserRoles(Long userId, Set<String> requestedRoles) {
        Set<String> normalizedRoles = requestedRoles == null
                ? Set.of()
                : requestedRoles.stream()
                        .filter(role -> role != null && !role.isBlank())
                        .map(role -> role.trim().toUpperCase(Locale.ROOT))
                        .collect(Collectors.toSet());

        if (normalizedRoles.isEmpty()) {
            throw new BadRequestException("At least one role must be selected");
        }

        Set<String> invalidRoles = normalizedRoles.stream()
                .filter(role -> !MANAGEABLE_ROLES.contains(role))
                .collect(Collectors.toSet());

        if (!invalidRoles.isEmpty()) {
            throw new BadRequestException("Unsupported role(s): " + String.join(", ", invalidRoles));
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        boolean wasAdmin = user.getRoles().stream().anyMatch(role -> "ADMIN".equals(role.getName()));
        boolean willRemainAdmin = normalizedRoles.contains("ADMIN");

        if (wasAdmin && !willRemainAdmin && userRepository.countByRoles_Name("ADMIN") <= 1) {
            throw new BadRequestException("At least one ADMIN user must remain in the system");
        }

        Set<Role> existingRoles = Set.copyOf(user.getRoles());
        for (Role role : existingRoles) {
            role.getUsers().remove(user);
        }
        user.getRoles().clear();

        for (String roleName : normalizedRoles) {
            Role role = roleRepository.findByName(roleName)
                    .orElseGet(() -> roleRepository.save(new Role(roleName)));
            user.addRole(role);
        }

        return userRepository.save(user);
    }
}
