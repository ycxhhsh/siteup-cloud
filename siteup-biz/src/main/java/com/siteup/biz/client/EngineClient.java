package com.siteup.biz.client;

import com.siteup.biz.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "siteup-engine", fallback = EngineClientFallback.class, configuration = FeignConfig.class)
public interface EngineClient {

    /**
     * 基础网站生成（兼容旧版本）
     * 使用泛型 Object 类型，避免依赖具体领域模型
     */
    @PostMapping("/api/v1/generate")
    String generate(@RequestBody Object config);

    /**
     * 带历史记录的网站生成
     * 使用泛型 Object 类型，避免依赖具体领域模型
     */
    @PostMapping("/api/v1/generate/with-history")
    Map<String, Object> generateWithHistory(
        @RequestBody Object config,
        @RequestParam("projectId") Long projectId,
        @RequestParam("templateId") String templateId,
        @RequestParam("userId") String userId
    );
}
