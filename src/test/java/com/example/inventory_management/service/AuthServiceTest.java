package com.example.inventory_management.service;

import com.example.inventory_management.dto.UserRegistrationDto;
import com.example.inventory_management.exception.BadRequestException;
import com.example.inventory_management.model.Role;
import com.example.inventory_management.model.User;
import com.example.inventory_management.repository.RoleRepository;
import com.example.inventory_management.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerUser_savesEncodedPasswordAndDefaultRole() {
        UserRegistrationDto registration = new UserRegistrationDto("bob", "bob@example.com", "plain-pass");
        Role buyerRole = new Role("BUYER");

        when(userRepository.findByUsername("bob")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("bob@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("plain-pass")).thenReturn("encoded-pass");
        when(roleRepository.findByName("BUYER")).thenReturn(Optional.of(buyerRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User savedUser = authService.registerUser(registration);

        assertThat(savedUser.getPassword()).isEqualTo("encoded-pass");
        assertThat(savedUser.getRoles())
                .extracting(Role::getName)
                .containsExactlyInAnyOrder("BUYER");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("bob@example.com");
    }

    @Test
    void registerUser_throwsWhenUsernameExists() {
        UserRegistrationDto registration = new UserRegistrationDto("bob", "bob@example.com", "plain-pass");
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> authService.registerUser(registration))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Username already exists");
    }

    @Test
    void registerUser_createsDefaultRoleWhenMissing() {
        UserRegistrationDto registration = new UserRegistrationDto("newuser", "newuser@example.com", "plain-pass");

        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("newuser@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("plain-pass")).thenReturn("encoded-pass");
        when(roleRepository.findByName("BUYER")).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User savedUser = authService.registerUser(registration);

        assertThat(savedUser.getRoles())
                .extracting(Role::getName)
                .containsExactlyInAnyOrder("BUYER");
    }
}
