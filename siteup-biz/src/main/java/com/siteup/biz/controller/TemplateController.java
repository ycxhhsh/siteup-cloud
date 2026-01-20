package com.siteup.biz.controller;

import com.siteup.biz.model.Template;
import com.siteup.biz.repository.TemplateRepository; // 👈 关键变化：引入 Repository
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/templates")
@Tag(name = "Template Management", description = "Template browsing and management APIs")
public class TemplateController {

    private final TemplateRepository templateRepository; // 👈 不再找 DataInitializer，直接找数据库

    public TemplateController(TemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    // 获取所有模版
    @GetMapping
    @Operation(summary = "Get all templates",
               description = "Retrieve all available website templates")
    @ApiResponse(responseCode = "200", description = "Templates retrieved successfully")
    public List<Template> getAllTemplates() {
        return templateRepository.findAll();
    }

    // 根据ID获取模版
    @GetMapping("/{id}")
    @Operation(summary = "Get template by ID",
               description = "Retrieve a specific template by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Template found"),
        @ApiResponse(responseCode = "404", description = "Template not found")
    })
    public Template getTemplate(
            @Parameter(description = "Template ID")
            @PathVariable String id) {
        return templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found: " + id));
    }

    // (可选) 根据分类获取模版 - 这里我们简单实现，直接在内存过滤，或者你可以在 Repository 加个 findByCategory
    @GetMapping("/category/{category}")
    @Operation(summary = "Get templates by category",
               description = "Retrieve templates filtered by category")
    @ApiResponse(responseCode = "200", description = "Templates retrieved successfully")
    public List<Template> getTemplatesByCategory(
            @Parameter(description = "Template category (e.g., Blog, Portfolio, SaaS)")
            @PathVariable String category) {
        // 简单做法：先全查出来再过滤 (数据量小这样做没问题)
        return templateRepository.findAll().stream()
                .filter(t -> category.equalsIgnoreCase(t.getCategory()))
                .toList();
    }
}