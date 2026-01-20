package com.siteup.biz.client;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Fallback implementation for AuthClient when the auth service is unavailable.
 * Provides graceful degradation by returning an invalid token response.
 */
@Component
public class AuthClientFallback implements AuthClient {

    @Override
    public Map<String, Object> verifyToken(String token) {
        // Return invalid token response instead of throwing exception
        Map<String, Object> response = new HashMap<>();
        response.put("valid", false);
        response.put("message", "Authentication service is currently unavailable. Please try again later.");
        response.put("userId", null);
        return response;
    }
}

