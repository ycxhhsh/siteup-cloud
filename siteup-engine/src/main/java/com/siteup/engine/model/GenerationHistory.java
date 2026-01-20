package com.siteup.engine.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 网站生成历史记录实体类
 * 记录每次网站生成的操作历史，便于统计和调试
 */
@Data
@Entity
@Table(name = "generation_history")
public class GenerationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long projectId; // 关联项目ID

    private String templateId; // 使用的模板ID

    @Column(nullable = false)
    private LocalDateTime generatedAt = LocalDateTime.now(); // 生成时间

    private Integer durationMs; // 生成耗时（毫秒）

    @Column(nullable = false)
    private Boolean success = true; // 是否成功

    @Column(columnDefinition = "TEXT")
    private String errorMessage; // 错误信息（失败时记录）

    private BigDecimal htmlSizeKb; // 生成的HTML大小（KB）

    private String userId; // 操作用户ID

    // Explicit getters and setters for Lombok compatibility
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public String getTemplateId() { return templateId; }
    public void setTemplateId(String templateId) { this.templateId = templateId; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }

    public Integer getDurationMs() { return durationMs; }
    public void setDurationMs(Integer durationMs) { this.durationMs = durationMs; }

    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public BigDecimal getHtmlSizeKb() { return htmlSizeKb; }
    public void setHtmlSizeKb(BigDecimal htmlSizeKb) { this.htmlSizeKb = htmlSizeKb; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
