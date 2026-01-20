package com.siteup.biz.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.siteup.biz.client.EngineClient;
import com.siteup.biz.exception.InvalidRequestException;
import com.siteup.biz.exception.ResourceNotFoundException;
import com.siteup.biz.exception.ServiceUnavailableException;
import com.siteup.biz.model.Project;
import com.siteup.biz.model.Template;
import com.siteup.biz.repository.ProjectRepository;
import com.siteup.biz.repository.TemplateRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final TemplateRepository templateRepository;
    private final EngineClient engineClient;
    private final ObjectMapper objectMapper;

    public ProjectService(ProjectRepository projectRepository,
                         TemplateRepository templateRepository,
                         EngineClient engineClient,
                         ObjectMapper objectMapper) {
        this.projectRepository = projectRepository;
        this.templateRepository = templateRepository;
        this.engineClient = engineClient;
        this.objectMapper = objectMapper;
    }

    // 创建项目（已加固：防空指针，防时间戳缺失）
    public Project createProjectFromTemplate(String templateId, String userId, String projectName) {
        // 1. 找模版
        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Template", templateId));

        // 2. 验证输入参数
        if (userId == null || userId.trim().isEmpty()) {
            throw new InvalidRequestException("User ID is required");
        }

        // 3. 名字保底逻辑：如果没传名字，就用模版名+时间
        String finalName = (projectName == null || projectName.trim().isEmpty())
                ? template.getName() + "-" + System.currentTimeMillis()
                : projectName;

        // 4. 创建实体
        Project project = new Project();
        project.setName(finalName);
        project.setTemplateId(templateId);
        project.setUserId(userId);
        project.setConfig(template.getConfig()); // 复制配置
        project.setStatus("draft");

        // 5. 关键：补全时间戳（防止 500 错误）
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());

        return projectRepository.save(project);
    }

    // 发布项目（已修复：使用泛型处理，不依赖具体领域模型）
    public Project publishProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", projectId));

        try {
            // 将配置解析为泛型 Map，不依赖具体领域模型
            Map<String, Object> configMap = objectMapper.readValue(project.getConfig(), Map.class);

            // 调用引擎（带历史记录）
            Map<String, Object> generationResult = engineClient.generateWithHistory(
                configMap,
                project.getId(),
                project.getTemplateId(),
                project.getUserId()
            );

            // 提取生成的HTML
            String generatedHtml;
            if (generationResult.containsKey("html")) {
                generatedHtml = (String) generationResult.get("html");
            } else {
                // 回退到旧的API调用
                generatedHtml = engineClient.generate(configMap);
            }

            // 保存文件 (演示用)
            String fileName = "sites/" + project.getId() + "/index.html";
            saveHtmlToFile(fileName, generatedHtml);

            // 更新状态
            project.setGeneratedHtml(generatedHtml);
            project.setStatus("published");
            project.setPublishedAt(LocalDateTime.now());
            project.setUpdatedAt(LocalDateTime.now());
            project.setPublicUrl("/api/v1/generated/" + project.getId());

            return projectRepository.save(project);

        } catch (JsonProcessingException e) {
            throw new InvalidRequestException("Invalid JSON format in project configuration: " + e.getMessage());
        } catch (Exception e) {
            // 处理引擎服务调用异常
            if (e.getCause() != null && e.getCause().getMessage() != null &&
                e.getCause().getMessage().contains("engine")) {
                throw new ServiceUnavailableException("siteup-engine", e);
            }
            throw new InvalidRequestException("Failed to publish project: " + e.getMessage());
        }
    }


    private void saveHtmlToFile(String fileName, String htmlContent) {
        try {
            Path path = Paths.get(fileName);
            Files.createDirectories(path.getParent());
            Files.write(path, htmlContent.getBytes());
        } catch (IOException e) {
            System.err.println("Failed to save HTML file: " + e.getMessage());
        }
    }

    public Project getProject(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", id));
    }

    public Iterable<Project> getAllProjects() {
        return projectRepository.findAll();
    }
}