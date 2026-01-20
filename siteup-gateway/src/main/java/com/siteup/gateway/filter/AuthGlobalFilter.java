package com.siteup.gateway.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import jakarta.annotation.PostConstruct;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final WebClient.Builder webClientBuilder;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Value("${gateway.auth.excluded-paths:/api/v1/auth/register,/api/v1/auth/login,/api/v1/auth/verify,/api/v1/generated/**,/api/v1/templates/**}")
    private String excludedPathsStr;

    @Value("${gateway.auth.engine-internal-only:true}")
    private boolean engineInternalOnly;

    private List<String> excludedPaths;

    public AuthGlobalFilter(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @PostConstruct
    public void init() {
        // 在@PostConstruct中@Value已经注入完成，可以安全使用
        this.excludedPaths = Arrays.asList(excludedPathsStr.split(","));
        // 调试：打印加载的配置
        System.out.println("AuthGlobalFilter initialized with excludedPaths: " + excludedPaths);
        System.out.println("AuthGlobalFilter engineInternalOnly: " + engineInternalOnly);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        String method = request.getMethod().name();

        // 生成或透传请求ID
        String rawRequestId = request.getHeaders().getFirst("X-Request-Id");
        final String requestId = (rawRequestId == null || rawRequestId.isEmpty())
            ? UUID.randomUUID().toString()
            : rawRequestId;

        // 更新请求头，添加请求ID
        ServerHttpRequest mutatedRequest = request.mutate()
            .header("X-Request-Id", requestId)
            .build();
        ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();

        // 调试：打印配置信息（只打印一次）
        if (path.equals("/api/v1/templates")) {
            System.out.println("[" + requestId + "] DEBUG - excludedPaths: " + excludedPaths);
            System.out.println("[" + requestId + "] DEBUG - engineInternalOnly: " + engineInternalOnly);
        }

        // 检查是否需要鉴权
        if (!requiresAuthentication(path, method)) {
            System.out.println("[" + requestId + "] Skipping auth for path: " + path);
            return chain.filter(mutatedExchange);
        }

        // 获取Authorization头
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("[" + requestId + "] Missing or invalid Authorization header for path: " + path);
            return unauthorizedResponse(mutatedExchange.getResponse(), "Missing or invalid Authorization header");
        }

        // 使用WebClient进行异步token验证
        return verifyToken(authHeader, requestId)
            .flatMap(verifyResult -> {
                Boolean isValid = (Boolean) verifyResult.getOrDefault("valid", false);
                if (Boolean.TRUE.equals(isValid)) {
                    // token有效，注入用户信息到请求头
                    String userId = String.valueOf(verifyResult.getOrDefault("userId", ""));
                    String username = String.valueOf(verifyResult.getOrDefault("username", ""));
                    String rawRole = String.valueOf(verifyResult.getOrDefault("role", "USER"));

                    // 确保角色以ROLE_开头（使用final变量）
                    final String role = rawRole.startsWith("ROLE_") ? rawRole : "ROLE_" + rawRole;

                    System.out.println("[" + requestId + "] Token valid for user: " + username + " (role: " + role + ")");

                    // 为路由到业务服务添加用户头
                    ServerHttpRequest authRequest = request.mutate()
                        .header("X-User-Id", userId)
                        .header("X-User-Name", username)
                        .header("X-User-Role", role)
                        .header("X-Request-Id", requestId)
                        .build();

                    // 为路由到引擎服务添加内部调用标记（如果启用内部访问策略）
                    if (engineInternalOnly && path.startsWith("/api/v1/generate/")) {
                        authRequest = authRequest.mutate()
                            .header("X-Internal-Call", "true")
                            .build();
                    }

                    ServerWebExchange authExchange = exchange.mutate().request(authRequest).build();
                    return chain.filter(authExchange);

                } else {
                    // token无效
                    String message = (String) verifyResult.getOrDefault("message", "Invalid token");
                    System.out.println("[" + requestId + "] Token invalid: " + message);
                    return unauthorizedResponse(mutatedExchange.getResponse(), message);
                }
            })
            .onErrorResume(error -> {
                System.err.println("[" + requestId + "] Token verification failed: " + error.getClass().getName() + " - " + error.getMessage());
                return unauthorizedResponse(mutatedExchange.getResponse(), "Authentication service unavailable");
            });
    }

    /**
     * 使用WebClient异步验证token
     */
    private Mono<Map<String, Object>> verifyToken(String authHeader, String requestId) {
        String verifyUrl = "http://siteup-auth/api/v1/auth/verify";

        System.out.println("[" + requestId + "] Verifying token with auth service: " + verifyUrl);

        return webClientBuilder.build()
            .post()
            .uri(verifyUrl)
            .header(HttpHeaders.AUTHORIZATION, authHeader)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header("X-Request-Id", requestId)
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    /**
     * 检查路径是否需要鉴权
     */
    private boolean requiresAuthentication(String path, String method) {
        // OPTIONS请求跳过（CORS预检）
        if (HttpMethod.OPTIONS.name().equals(method)) {
            return false;
        }

        // 检查排除路径（支持Ant风格匹配）
        for (String excludedPath : excludedPaths) {
            if (pathMatcher.match(excludedPath, path)) {
                return false;
            }
        }

        // 其他API路径需要鉴权
        return path.startsWith("/api/");
    }

    /**
     * 返回401未授权响应
     */
    private Mono<Void> unauthorizedResponse(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String jsonResponse = String.format(
            "{\"success\": false, \"message\": \"%s\", \"code\": \"UNAUTHORIZED\"}",
            message.replace("\"", "\\\"")
        );

        return response.writeWith(
            Mono.just(response.bufferFactory().wrap(jsonResponse.getBytes()))
        );
    }

    @Override
    public int getOrder() {
        // 设置较高的优先级，确保在其他过滤器之前执行
        return -100;
    }
}
