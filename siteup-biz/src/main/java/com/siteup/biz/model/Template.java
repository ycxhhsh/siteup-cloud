package com.siteup.biz.model;

import jakarta.persistence.*; // 👈 核心修改：这里必须是 jakarta，不能是 javax
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "template")
public class Template {
    @Id
    private String id; // 注意：模板ID通常是字符串（1, 2, 3），不是自增主键

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private String category; // Blog, Portfolio, SaaS

    private String thumbnailUrl;

    @Column(columnDefinition = "TEXT")
    private String config; // Store as JSON string (contentJson)

    @Column(columnDefinition = "TEXT")
    private String themeConfig; // Store theme config

    @Column(nullable = false)
    private boolean active = true;

    private LocalDateTime createdAt = LocalDateTime.now();

    // Explicit getters and setters for compilation
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public String getConfig() { return config; }
    public void setConfig(String config) { this.config = config; }

    public String getThemeConfig() { return themeConfig; }
    public void setThemeConfig(String themeConfig) { this.themeConfig = themeConfig; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}