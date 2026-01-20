package com.siteup.biz.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(name = "siteup-auth", fallback = AuthClientFallback.class)
public interface AuthClient {

    @PostMapping("/api/v1/auth/verify")
    Map<String, Object> verifyToken(@RequestHeader("Authorization") String token);
}
