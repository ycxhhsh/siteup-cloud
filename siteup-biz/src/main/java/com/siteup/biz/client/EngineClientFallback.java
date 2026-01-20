package com.siteup.biz.client;

import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Fallback implementation for EngineClient when the engine service is unavailable.
 * Provides graceful degradation by returning a simple error message.
 */
@Component
public class EngineClientFallback implements EngineClient {

    @Override
    public String generate(Object config) {
        // Return a simple error message instead of throwing exception
        return "<!-- Service Unavailable: The rendering engine is currently down. " +
               "Please try again later. If the problem persists, contact support. -->";
    }

    @Override
    public Map<String, Object> generateWithHistory(Object config, Long projectId,
                                                 String templateId, String userId) {
        // Return error response for the history-enabled generation
        return Map.of(
            "success", false,
            "message", "Service Unavailable: The rendering engine is currently down. Please try again later.",
            "error", "ENGINE_SERVICE_UNAVAILABLE",
            "projectId", projectId,
            "templateId", templateId,
            "userId", userId,
            "timestamp", java.time.LocalDateTime.now().toString()
        );
    }
}

