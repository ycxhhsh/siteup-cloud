package com.siteup.biz.controller;

import com.siteup.biz.model.CreateProjectRequest;
import com.siteup.biz.model.Project;
import com.siteup.biz.repository.ProjectRepository;
import com.siteup.biz.repository.TemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProjectControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TemplateRepository templateRepository;

    @BeforeEach
    void setUp() {
        // Test data is initialized by DataInitializer
    }

    @Test
    void createProject_ShouldCreateProject_WhenValidRequest() throws Exception {
        // Given
        CreateProjectRequest request = new CreateProjectRequest();
        request.setName("Integration Test Project");

        // When & Then - 模拟网关传递的用户信息头
        mockMvc.perform(post("/api/templates/from-template/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Integration Test Project\"}")
                .header("X-User-Id", "1")      // 模拟网关传递的用户ID
                .header("X-User-Name", "testuser")  // 模拟用户名
                .header("X-User-Role", "ROLE_USER") // 模拟用户角色
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", notNullValue()))
            .andExpect(jsonPath("$.name", is("Integration Test Project")))
            .andExpect(jsonPath("$.templateId", is("1")))
            .andExpect(jsonPath("$.status", is("draft")));
    }

    @Test
    void getProject_ShouldReturnProject_WhenExists() throws Exception {
        // Given - create a project first
        Project project = new Project();
        project.setName("Test Project");
        project.setTemplateId("1");
        project.setUserId("testuser");
        project.setConfig("{}");
        project.setStatus("draft");
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());
        Project savedProject = projectRepository.save(project);

        // When & Then - 模拟网关传递的用户信息头
        mockMvc.perform(get("/api/projects/" + savedProject.getId())
                .header("X-User-Id", "1")      // 模拟网关传递的用户ID
                .header("X-User-Name", "testuser")  // 模拟用户名
                .header("X-User-Role", "ROLE_USER")) // 模拟用户角色
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(savedProject.getId().intValue())))
            .andExpect(jsonPath("$.name", is("Test Project")))
            .andExpect(jsonPath("$.status", is("draft")));
    }

    @Test
    void getProject_ShouldReturn404_WhenNotFound() throws Exception {
        mockMvc.perform(get("/api/projects/99999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code", is("RESOURCE_NOT_FOUND")))
            .andExpect(jsonPath("$.message", containsString("Project not found")));
    }

    @Test
    void getAllProjects_ShouldReturnProjects() throws Exception {
        mockMvc.perform(get("/api/projects")
                .header("X-User-Id", "1")      // 模拟网关传递的用户ID
                .header("X-User-Name", "testuser")  // 模拟用户名
                .header("X-User-Role", "ROLE_USER")) // 模拟用户角色
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", isA(java.util.List.class)));
    }

    @Test
    void getTemplates_ShouldReturnTemplates() throws Exception {
        mockMvc.perform(get("/api/templates"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", isA(java.util.List.class)))
            .andExpect(jsonPath("$", hasSize(greaterThan(0))));
    }

    @Test
    void getTemplate_ShouldReturnTemplate_WhenExists() throws Exception {
        mockMvc.perform(get("/api/templates/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is("1")))
            .andExpect(jsonPath("$.name", notNullValue()));
    }

    @Test
    void getTemplate_ShouldReturn404_WhenNotFound() throws Exception {
        mockMvc.perform(get("/api/templates/999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code", is("RESOURCE_NOT_FOUND")))
            .andExpect(jsonPath("$.message", containsString("Template not found")));
    }
}
