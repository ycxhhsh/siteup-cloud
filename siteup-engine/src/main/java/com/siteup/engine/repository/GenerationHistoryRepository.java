package com.siteup.engine.repository;

import com.siteup.engine.model.GenerationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 生成历史记录数据访问层
 */
@Repository
public interface GenerationHistoryRepository extends JpaRepository<GenerationHistory, Long> {

    /**
     * 根据项目ID查询生成历史
     */
    List<GenerationHistory> findByProjectIdOrderByGeneratedAtDesc(Long projectId);

    /**
     * 根据用户ID查询生成历史
     */
    List<GenerationHistory> findByUserIdOrderByGeneratedAtDesc(String userId);

    /**
     * 查询指定时间范围内的生成记录
     */
    List<GenerationHistory> findByGeneratedAtBetweenOrderByGeneratedAtDesc(
        LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 统计成功率
     */
    @Query("SELECT COUNT(g) FROM GenerationHistory g WHERE g.success = true")
    Long countSuccessfulGenerations();

    @Query("SELECT COUNT(g) FROM GenerationHistory g WHERE g.success = false")
    Long countFailedGenerations();

    /**
     * 计算平均生成时间
     */
    @Query("SELECT AVG(g.durationMs) FROM GenerationHistory g WHERE g.success = true")
    Double getAverageGenerationTime();

    /**
     * 获取最近N次的生成记录
     */
    @Query("SELECT g FROM GenerationHistory g ORDER BY g.generatedAt DESC")
    List<GenerationHistory> findRecentGenerations(
        org.springframework.data.domain.Pageable pageable);

    /**
     * 根据项目ID统计生成次数
     */
    @Query("SELECT COUNT(g) FROM GenerationHistory g WHERE g.projectId = :projectId")
    Long countByProjectId(@Param("projectId") Long projectId);
}
