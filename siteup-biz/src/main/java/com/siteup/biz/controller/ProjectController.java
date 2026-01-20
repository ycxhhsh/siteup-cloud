package com.siteup.biz.controller;

import com.siteup.biz.model.CreateProjectRequest;
import com.siteup.biz.model.Project;
import com.siteup.biz.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Project Management", description = "Project creation, publishing and management APIs")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping("/templates/from-template/{templateId}")
    @Operation(summary = "Create project from template",
               description = "Create a new project based on an existing template")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Project created successfully"),
        @ApiResponse(responseCode = "404", description = "Template not found"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters")
    })
    public ResponseEntity<Project> createProject(
            @Parameter(description = "Template ID to base the project on")
            @PathVariable String templateId,
            @RequestBody CreateProjectRequest request) {

        // Extract userId from authentication token instead of request body
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = (authentication != null) ? authentication.getName() : "anonymousUser";

        Project project = projectService.createProjectFromTemplate(
            templateId,
            userId,
            request.getName()
        );

        return ResponseEntity.ok(project);
    }

    @PostMapping("/projects/{id}/publish")
    @Operation(summary = "Publish project",
               description = "Publish a project by generating HTML from its configuration")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Project published successfully"),
        @ApiResponse(responseCode = "404", description = "Project not found"),
        @ApiResponse(responseCode = "503", description = "Engine service unavailable")
    })
    public ResponseEntity<Map<String, Object>> publishProject(
            @Parameter(description = "Project ID to publish")
            @PathVariable Long id) {
        Project project = projectService.publishProject(id);

        Map<String, Object> response = Map.of(
            "success", true,
            "message", "Project published successfully",
            "projectId", project.getId(),
            "publicUrl", project.getPublicUrl(),
            "publishedAt", project.getPublishedAt()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/projects/{id}")
    @Operation(summary = "Get project by ID",
               description = "Retrieve project details by project ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Project found"),
        @ApiResponse(responseCode = "404", description = "Project not found")
    })
    public ResponseEntity<Project> getProject(
            @Parameter(description = "Project ID")
            @PathVariable Long id) {
        Project project = projectService.getProject(id);
        return ResponseEntity.ok(project);
    }

    @GetMapping("/projects")
    @Operation(summary = "Get all projects",
               description = "Retrieve all projects in the system")
    @ApiResponse(responseCode = "200", description = "Projects retrieved successfully")
    public ResponseEntity<Iterable<Project>> getAllProjects() {
        Iterable<Project> projects = projectService.getAllProjects();
        return ResponseEntity.ok(projects);
    }

    // For demo purposes - direct access to generated HTML
    @GetMapping("/generated/{projectId}")
    @Operation(summary = "Get generated HTML",
               description = "Retrieve the generated HTML content for a published project")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "HTML content retrieved"),
        @ApiResponse(responseCode = "404", description = "Project not found or not published")
    })
    public ResponseEntity<String> getGeneratedHtml(
            @Parameter(description = "Project ID")
            @PathVariable Long projectId) {
        Project project = projectService.getProject(projectId);
        if (project != null && project.getGeneratedHtml() != null) {
            return ResponseEntity.ok()
                .header("Content-Type", "text/html")
                .body(project.getGeneratedHtml());
        }
        return ResponseEntity.notFound().build();
    }
}
