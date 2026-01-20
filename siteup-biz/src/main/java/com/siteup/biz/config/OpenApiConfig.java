package com.siteup.biz.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("SiteUp Biz Service API")
                .description("Business service for SiteUp low-code website platform")
                .version("1.0.0")
                .contact(new Contact()
                    .name("SiteUp Team")
                    .email("support@siteup.com"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")))
            .servers(List.of(
                new Server().url("http://localhost:8030").description("Development server"),
                new Server().url("http://localhost:8010").description("Gateway server")
            ));
    }
}
