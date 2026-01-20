package com.siteup.biz.config;

import com.siteup.biz.client.AuthClient;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final AuthClient authClient;

    // Use constructor injection to ensure Feign client is available
    public TokenAuthenticationFilter(AuthClient authClient) {
        this.authClient = authClient;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 1) Entrance log: print request URI
        String requestId = request.getHeader("X-Request-Id");
        String logPrefix = requestId != null ? "[" + requestId + "] " : "";
        System.out.println(logPrefix + "Incoming request: " + request.getRequestURI());

        // 2) 优先检查来自网关的用户头（已由网关验证）
        String userId = request.getHeader("X-User-Id");
        String userName = request.getHeader("X-User-Name");
        String userRole = request.getHeader("X-User-Role");

        if (userId != null && !userId.isEmpty() &&
            userName != null && !userName.isEmpty() &&
            userRole != null && !userRole.isEmpty()) {

            // 来自网关的已验证用户信息，直接信任并设置安全上下文
            String role = userRole;
            if (!role.startsWith("ROLE_")) {
                role = "ROLE_" + role;
            }

            System.out.println(logPrefix + "Using gateway-trusted user: " + userName + " (role: " + role + ", id: " + userId + ")");

            UserDetails userDetails = User.withUsername(userId) // 使用userId作为principal
                    .password("") // not used
                    .authorities(role)
                    .build();

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);
            return;
        }

        // 3) 回退到原有逻辑：检查Authorization头并调用认证服务
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                // Call auth service to verify token
                System.out.println(logPrefix + "No gateway headers found, falling back to token verification");
                Map<String, Object> verifyResult = authClient.verifyToken(authHeader);

                Boolean isValid = (Boolean) verifyResult.getOrDefault("valid", false);
                if (Boolean.TRUE.equals(isValid)) {
                    // Prefer userId if present; otherwise fall back to username
                    Object userIdObj = verifyResult.get("userId");
                    String principalName = userIdObj != null ? String.valueOf(userIdObj) :
                                           String.valueOf(verifyResult.getOrDefault("username", "user"));

                    // Fix role prefix: ensure it starts with ROLE_
                    String rawRole = String.valueOf(verifyResult.getOrDefault("role", "USER"));
                    String role = rawRole;
                    if (!role.startsWith("ROLE_")) {
                        role = "ROLE_" + role;
                    }

                    // 4) Print success log before setting security context
                    System.out.println(logPrefix + "Token verification successful, role: " + role);

                    // Build UserDetails using authorities() to avoid double 'ROLE_' prefixing
                    UserDetails userDetails = User.withUsername(principalName)
                            .password("") // not used
                            .authorities(role)
                            .build();

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    // token invalid - make sure no authentication set
                    System.out.println(logPrefix + "Token invalid according to auth service, clearing security context");
                    SecurityContextHolder.clearContext();
                }
            } catch (Exception e) {
                // 5) Enhanced exception logging: print type, message and stack trace
                System.err.println(logPrefix + "Token verification failed: " + e.getClass().getName() + " - " + e.getMessage());
                e.printStackTrace();
                // Ensure no leftover authentication
                SecurityContextHolder.clearContext();
            }
        } else {
            // 没有Authorization头且没有网关头，清除安全上下文
            System.out.println(logPrefix + "No authorization headers found, clearing security context");
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
