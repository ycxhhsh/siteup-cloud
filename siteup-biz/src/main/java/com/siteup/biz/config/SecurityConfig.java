package com.siteup.biz.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.siteup.biz.client.AuthClient;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter(AuthClient authClient) {
        return new TokenAuthenticationFilter(authClient);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, AuthClient authClient) throws Exception {
        http
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeHttpRequests(authz -> authz
                // 模板列表与详情允许匿名访问
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/templates/**").permitAll()
                // 从模板创建项目需要登录
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/templates/from-template/**").authenticated()
                // 项目相关操作需要认证
                .requestMatchers("/api/projects/**").authenticated()
                .requestMatchers("/api/generated/**").permitAll() // 生成的HTML公开访问
                .anyRequest().permitAll()
            )
            // Add our token filter into the security filter chain before the username/password auth filter
            .addFilterBefore(tokenAuthenticationFilter(authClient), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
