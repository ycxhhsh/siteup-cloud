package com.siteup.engine.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 内部调用过滤器
 * 确保引擎服务只能通过网关访问，拒绝直接外部访问
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // 最高优先级
public class InternalCallFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String requestId = request.getHeader("X-Request-Id");
        String logPrefix = requestId != null ? "[" + requestId + "] " : "";
        String path = request.getRequestURI();

        System.out.println(logPrefix + "Engine request: " + path);

        // 检查是否为生成API路径
        if (path.startsWith("/api/v1/generate/")) {
            String internalCallHeader = request.getHeader("X-Internal-Call");

            if (!"true".equals(internalCallHeader)) {
                System.out.println(logPrefix + "Rejected direct access to engine API: " + path +
                                 " (missing or invalid X-Internal-Call header: " + internalCallHeader + ")");

                // 返回403 Forbidden
                response.setStatus(HttpStatus.FORBIDDEN.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding("UTF-8");

                String jsonResponse = "{\"success\": false, \"message\": \"Direct access to engine service is not allowed. Please use the gateway.\", \"code\": \"FORBIDDEN\"}";
                response.getWriter().write(jsonResponse);
                return;
            }

            System.out.println(logPrefix + "Accepted internal call to engine API: " + path);
        }

        filterChain.doFilter(request, response);
    }
}
