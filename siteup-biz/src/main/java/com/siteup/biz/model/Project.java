package com.siteup.biz.model;

import jakarta.persistence.*; // 关键修改：统一使用 jakarta.*，涵盖 Entity, Table, Id 等所有注解
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "project")
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String templateId;

    @Column(nullable = false)
    private String userId;

    @Column(columnDefinition = "TEXT")
    private String config; // JSON string from template

    @Column(columnDefinition = "TEXT")
    private String generatedHtml;

    @Column(nullable = false)
    private String status; // draft, published, archived

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private LocalDateTime publishedAt;

    private String publicUrl; // URL to access the published site

    // Explicit getters and setters for compilation
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTemplateId() { return templateId; }
    public void setTemplateId(String templateId) { this.templateId = templateId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getConfig() { return config; }
    public void setConfig(String config) { this.config = config; }

    public String getGeneratedHtml() { return generatedHtml; }
    public void setGeneratedHtml(String generatedHtml) { this.generatedHtml = generatedHtml; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }

    public String getPublicUrl() { return publicUrl; }
    public void setPublicUrl(String publicUrl) { this.publicUrl = publicUrl; }
}