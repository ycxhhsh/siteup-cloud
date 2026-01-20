package com.siteup.biz.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            // 为所有Feign请求添加内部调用标识
            requestTemplate.header("X-Internal-Call", "true");
        };
    }
}