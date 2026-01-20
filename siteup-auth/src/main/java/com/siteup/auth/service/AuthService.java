package com.siteup.auth.service;

import com.siteup.auth.exception.InvalidCredentialsException;
import com.siteup.auth.exception.UserNotFoundException;
import com.siteup.auth.model.User;
import com.siteup.auth.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final com.siteup.auth.repository.AuthTokenRepository authTokenRepository;

    public AuthService(UserRepository userRepository,
                      com.siteup.auth.repository.AuthTokenRepository authTokenRepository) {
        this.userRepository = userRepository;
        this.authTokenRepository = authTokenRepository;
    }

    public Map<String, Object> register(String username, String password) {
        // Check if user already exists
        Optional<User> existingUser = userRepository.findByUsername(username);
        if (existingUser.isPresent()) {
            return Map.of(
                "success", false,
                "message", "Username already exists"
            );
        }

        // Create new user
        User user = new User();
        user.setUsername(username);
        user.setPassword(password); // In production, this should be hashed
        user.setRole("USER");

        User savedUser = userRepository.save(user);

        return Map.of(
            "success", true,
            "message", "User registered successfully",
            "userId", savedUser.getId(),
            "username", savedUser.getUsername()
        );
    }

    public Map<String, Object> login(String username, String password) {
        // Find user by username
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException(username));

        // Check password (simple string comparison for demo)
        if (!password.equals(user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        // Generate token
        String token = "siteup-token-" + UUID.randomUUID().toString();

        // persist token
        com.siteup.auth.model.AuthToken authToken = new com.siteup.auth.model.AuthToken();
        authToken.setToken(token);
        authToken.setUserId(user.getId());
        authToken.setIssuedAt(java.time.LocalDateTime.now());
        authToken.setExpiresAt(java.time.LocalDateTime.now().plusHours(8));
        authTokenRepository.save(authToken);

        return Map.of(
            "success", true,
            "message", "Login successful",
            "token", token,
            "tokenType", "Bearer",
            "userId", user.getId(),
            "username", user.getUsername(),
            "role", user.getRole()
        );
    }
}
