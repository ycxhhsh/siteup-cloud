package com.siteup.engine.controller;

import com.siteup.engine.model.GenerationHistory;
import com.siteup.engine.model.SiteConfig;
import com.siteup.engine.renderer.RenderingService;
import com.siteup.engine.repository.GenerationHistoryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/generate")
@Tag(name = "Site Generation", description = "Website generation and history APIs")
public class GenerateController {

    @Autowired
    private RenderingService renderingService;

    @Autowired
    private GenerationHistoryRepository historyRepository;

    /**
     * 基础网站生成（兼容旧版本）
     */
    @PostMapping
    @Operation(summary = "Generate website HTML",
               description = "Generate HTML content from site configuration")
    @ApiResponse(responseCode = "200", description = "HTML generated successfully")
    public String generateSite(@RequestBody SiteConfig siteConfig) {
        return renderingService.renderSite(siteConfig);
    }

    /**
     * 带历史记录的网站生成
     */
    @PostMapping("/with-history")
    @Operation(summary = "Generate website with history tracking",
               description = "Generate HTML and record generation history for analytics")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "HTML generated and history recorded"),
        @ApiResponse(responseCode = "400", description = "Invalid generation parameters")
    })
    public ResponseEntity<Map<String, Object>> generateSiteWithHistory(
            @RequestBody SiteConfig siteConfig,
            @Parameter(description = "Project ID for history tracking")
            @RequestParam(required = false) Long projectId,
            @Parameter(description = "Template ID used")
            @RequestParam(required = false) String templateId,
            @Parameter(description = "User ID performing the generation")
            @RequestParam(required = false) String userId) {

        try {
            String html = renderingService.renderSiteWithHistory(siteConfig, projectId, templateId, userId);

            Map<String, Object> response = Map.of(
                "success", true,
                "message", "Website generated successfully",
                "html", html,
                "projectId", projectId,
                "generatedAt", java.time.LocalDateTime.now()
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = Map.of(
                "success", false,
                "message", "Failed to generate website: " + e.getMessage(),
                "error", e.getClass().getSimpleName()
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 获取生成历史记录
     */
    @GetMapping("/history")
    @Operation(summary = "Get generation history",
               description = "Retrieve website generation history records")
    @ApiResponse(responseCode = "200", description = "History retrieved successfully")
    public ResponseEntity<Map<String, Object>> getGenerationHistory(
            @Parameter(description = "Project ID to filter by")
            @RequestParam(required = false) Long projectId,
            @Parameter(description = "User ID to filter by")
            @RequestParam(required = false) String userId,
            @Parameter(description = "Maximum number of records to return")
            @RequestParam(defaultValue = "50") int limit) {

        List<GenerationHistory> historyList;

        if (projectId != null) {
            historyList = historyRepository.findByProjectIdOrderByGeneratedAtDesc(projectId);
        } else if (userId != null) {
            historyList = historyRepository.findByUserIdOrderByGeneratedAtDesc(userId);
        } else {
            Pageable pageable = PageRequest.of(0, limit);
            historyList = historyRepository.findRecentGenerations(pageable);
        }

        // 计算统计信息
        Long totalSuccess = historyRepository.countSuccessfulGenerations();
        Long totalFailed = historyRepository.countFailedGenerations();
        Double avgTime = historyRepository.getAverageGenerationTime();

        Map<String, Object> response = Map.of(
            "history", historyList,
            "totalRecords", historyList.size(),
            "statistics", Map.of(
                "totalSuccess", totalSuccess,
                "totalFailed", totalFailed,
                "successRate", totalSuccess + totalFailed > 0 ?
                    (double) totalSuccess / (totalSuccess + totalFailed) * 100 : 0.0,
                "averageGenerationTime", avgTime != null ? avgTime : 0.0
            )
        );

        return ResponseEntity.ok(response);
    }

    /**
     * 获取生成统计信息
     */
    @GetMapping("/stats")
    @Operation(summary = "Get generation statistics",
               description = "Retrieve overall generation statistics")
    @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    public ResponseEntity<Map<String, Object>> getGenerationStats() {
        Long totalSuccess = historyRepository.countSuccessfulGenerations();
        Long totalFailed = historyRepository.countFailedGenerations();
        Double avgTime = historyRepository.getAverageGenerationTime();

        long total = totalSuccess + totalFailed;

        Map<String, Object> stats = Map.of(
            "totalGenerations", total,
            "successfulGenerations", totalSuccess,
            "failedGenerations", totalFailed,
            "successRate", total > 0 ? (double) totalSuccess / total * 100 : 0.0,
            "averageGenerationTimeMs", avgTime != null ? avgTime : 0.0
        );

        return ResponseEntity.ok(stats);
    }
}
