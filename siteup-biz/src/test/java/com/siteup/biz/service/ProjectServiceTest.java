package com.siteup.biz.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.siteup.biz.client.EngineClient;
import com.siteup.biz.exception.ResourceNotFoundException;
import com.siteup.biz.model.Project;
import com.siteup.biz.model.Template;
import com.siteup.biz.repository.ProjectRepository;
import com.siteup.biz.repository.TemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private TemplateRepository templateRepository;

    @Mock
    private EngineClient engineClient;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ProjectService projectService;

    private Template testTemplate;
    private Project testProject;

    @BeforeEach
    void setUp() {
        testTemplate = new Template();
        testTemplate.setId("1");
        testTemplate.setName("Test Template");
        testTemplate.setConfig("{\"themeConfig\":\"{}\",\"contentJson\":\"{}\"}");

        testProject = new Project();
        testProject.setId(1L);
        testProject.setName("Test Project");
        testProject.setTemplateId("1");
        testProject.setUserId("user1");
    }

    @Test
    void createProjectFromTemplate_ShouldCreateProject_WhenTemplateExists() {
        // Given
        when(templateRepository.findById("1")).thenReturn(Optional.of(testTemplate));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project project = invocation.getArgument(0);
            project.setId(1L);
            return project;
        });

        // When
        Project result = projectService.createProjectFromTemplate("1", "user1", "Test Project");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test Project");
        assertThat(result.getTemplateId()).isEqualTo("1");
        assertThat(result.getUserId()).isEqualTo("user1");
        assertThat(result.getStatus()).isEqualTo("draft");
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();
    }

    @Test
    void createProjectFromTemplate_ShouldThrowException_WhenTemplateNotFound() {
        // Given
        when(templateRepository.findById("999")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> projectService.createProjectFromTemplate("999", "user1", "Test Project"))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Template not found: 999");
    }

    @Test
    void createProjectFromTemplate_ShouldUseDefaultName_WhenNameIsNull() {
        // Given
        when(templateRepository.findById("1")).thenReturn(Optional.of(testTemplate));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project project = invocation.getArgument(0);
            project.setId(1L);
            return project;
        });

        // When
        Project result = projectService.createProjectFromTemplate("1", "user1", null);

        // Then
        assertThat(result.getName()).startsWith("Test Template-");
    }

    @Test
    void getProject_ShouldReturnProject_WhenExists() {
        // Given
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));

        // When
        Project result = projectService.getProject(1L);

        // Then
        assertThat(result).isEqualTo(testProject);
    }

    @Test
    void getProject_ShouldThrowException_WhenNotFound() {
        // Given
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> projectService.getProject(999L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Project not found: 999");
    }
}
