package com.siteup.biz.service;

import com.siteup.biz.model.Template;
import com.siteup.biz.repository.TemplateRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class DataInitializer {

    private final TemplateRepository templateRepository;

    public DataInitializer(TemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    @Bean
    public ApplicationRunner initializeData() {
        return args -> {
            // Templates are now initialized via database-init.sql
            // Removed code-based template initialization to avoid conflicts
            System.out.println("Data initialization completed (templates loaded from database-init.sql)");
        };
    }
}
