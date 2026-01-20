package com.siteup.auth.controller;

import com.siteup.auth.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final ObjectMapper objectMapper;
    private final com.siteup.auth.repository.AuthTokenRepository authTokenRepository;
    private final com.siteup.auth.repository.UserRepository userRepository;

    public AuthController(AuthService authService,
                         ObjectMapper objectMapper,
                         com.siteup.auth.repository.AuthTokenRepository authTokenRepository,
                         com.siteup.auth.repository.UserRepository userRepository) {
        this.authService = authService;
        this.objectMapper = objectMapper;
        this.authTokenRepository = authTokenRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Username and password are required"
            ));
        }

        Map<String, Object> result = authService.register(username, password);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Username and password are required"
            ));
        }

        Map<String, Object> result = authService.login(username, password);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyToken(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(Map.of(
                "valid", false,
                "message", "Invalid token format"
            ));
        }

        String token = authHeader.substring(7); // Remove "Bearer " prefix

        // Check DB for token
        var opt = authTokenRepository.findByToken(token);
        if (opt.isEmpty()) {
            return ResponseEntity.ok(Map.of("valid", false, "message", "Invalid token"));
        }

        var authToken = opt.get();
        if (authToken.getExpiresAt() != null && authToken.getExpiresAt().isBefore(java.time.LocalDateTime.now())) {
            return ResponseEntity.ok(Map.of("valid", false, "message", "Token expired"));
        }

        var user = userRepository.findById(authToken.getUserId()).orElse(null);
        if (user == null) {
            return ResponseEntity.ok(Map.of("valid", false, "message", "User not found"));
        }

        return ResponseEntity.ok(Map.of(
            "valid", true,
            "message", "Token is valid",
            "userId", user.getId(),
            "username", user.getUsername(),
            "role", user.getRole()
        ));
    }
}
