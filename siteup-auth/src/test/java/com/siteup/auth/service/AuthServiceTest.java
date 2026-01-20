package com.siteup.auth.service;

import com.siteup.auth.exception.InvalidCredentialsException;
import com.siteup.auth.exception.UserNotFoundException;
import com.siteup.auth.model.User;
import com.siteup.auth.repository.AuthTokenRepository;
import com.siteup.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
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
    private AuthTokenRepository authTokenRepository;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
        testUser.setRole("USER");
    }

    @Test
    void register_ShouldCreateNewUser_WhenUsernameNotExists() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        Map<String, Object> result = authService.register("testuser", "password123");

        // Then
        assertThat(result.get("success")).isEqualTo(true);
        assertThat(result.get("message")).isEqualTo("User registered successfully");
        assertThat(result.get("userId")).isEqualTo(1L);
        assertThat(result.get("username")).isEqualTo("testuser");

        // Verify user was saved
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getUsername()).isEqualTo("testuser");
        assertThat(savedUser.getRole()).isEqualTo("USER");
    }

    @Test
    void register_ShouldReturnError_WhenUsernameExists() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When
        Map<String, Object> result = authService.register("testuser", "password123");

        // Then
        assertThat(result.get("success")).isEqualTo(false);
        assertThat(result.get("message")).isEqualTo("Username already exists");
    }

    @Test
    void login_ShouldReturnToken_WhenCredentialsValid() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When
        Map<String, Object> result = authService.login("testuser", "password123");

        // Then
        assertThat(result.get("success")).isEqualTo(true);
        assertThat(result.get("message")).isEqualTo("Login successful");
        assertThat(result.get("token")).isNotNull();
        assertThat(result.get("tokenType")).isEqualTo("Bearer");
        assertThat(result.get("userId")).isEqualTo(1L);
        assertThat(result.get("username")).isEqualTo("testuser");
        assertThat(result.get("role")).isEqualTo("USER");

        // Verify token was saved
        verify(authTokenRepository).save(any());
    }

    @Test
    void login_ShouldThrowException_WhenUserNotFound() {
        // Given
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authService.login("nonexistent", "password"))
            .isInstanceOf(UserNotFoundException.class)
            .hasMessage("User not found: nonexistent");
    }

    @Test
    void login_ShouldThrowException_WhenPasswordInvalid() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When & Then
        assertThatThrownBy(() -> authService.login("testuser", "wrongpassword"))
            .isInstanceOf(InvalidCredentialsException.class)
            .hasMessage("Invalid username or password");
    }
}
