package com.siteup.gateway.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class GatewayConfig {

    /**
     * 创建支持负载均衡的WebClient，用于调用其他微服务
     * 主要用于网关调用认证服务的token验证
     * 使用WebClient替代RestTemplate，因为在WebFlux环境中不能使用阻塞操作
     */
    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    /**
     * 保留RestTemplate以防其他地方需要，但不推荐在网关过滤器中使用
     */
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate;
    }
}
