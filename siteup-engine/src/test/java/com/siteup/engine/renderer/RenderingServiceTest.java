package com.siteup.engine.renderer;

import com.siteup.engine.model.SiteConfig;
import com.siteup.engine.repository.GenerationHistoryRepository; // 1. 导入 Repository
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RenderingServiceTest {

    @Mock
    private ComponentRenderer textRenderer;

    @Mock
    private ComponentRenderer imageRenderer;

    @Mock
    private GenerationHistoryRepository historyRepository; // 2. Mock 历史记录仓库

    private RenderingService renderingService;

    @BeforeEach
    void setUp() {
        // 3. 修正构造函数调用，传入 historyRepository
        renderingService = new RenderingService(List.of(textRenderer, imageRenderer), historyRepository);
    }

    @Test
    void render_ShouldReturnRendererOutput_WhenSupported() {
        // Given
        SiteConfig.ComponentNode node = new SiteConfig.ComponentNode();
        node.setType("text");

        when(textRenderer.supports("text")).thenReturn(true);
        // 注意：这里用 eq(renderingService) 或 any() 会更稳健，直接传实例也可以
        when(textRenderer.render(eq(node), any(RenderingService.class))).thenReturn("<p>Hello World</p>");

        // When
        String result = renderingService.render(node);

        // Then
        assertThat(result).isEqualTo("<p>Hello World</p>");
    }

    @Test
    void render_ShouldReturnUnsupportedComment_WhenNoRendererSupports() {
        // Given
        SiteConfig.ComponentNode node = new SiteConfig.ComponentNode();
        node.setType("unknown");

        when(textRenderer.supports("unknown")).thenReturn(false);
        when(imageRenderer.supports("unknown")).thenReturn(false);

        // When
        String result = renderingService.render(node);

        // Then
        assertThat(result).isEqualTo("");
    }

    @Test
    void renderSite_ShouldGenerateCompleteHTML() {
        // Given
        SiteConfig siteConfig = new SiteConfig();
        siteConfig.setTitle("Test Site");
        // 这里的 ThemeConfig 现在是 Map 类型，保持原样即可测试
        siteConfig.setThemeConfig(Map.of("primaryColor", "#3b82f6"));

        SiteConfig.ComponentNode rootNode = new SiteConfig.ComponentNode();
        rootNode.setType("page");
        siteConfig.setRoot(rootNode);

        when(textRenderer.supports("page")).thenReturn(true);
        when(textRenderer.render(eq(rootNode), any(RenderingService.class))).thenReturn("<div>Welcome</div>");

        // When
        String result = renderingService.renderSite(siteConfig);

        // Then
        assertThat(result).contains("<!DOCTYPE html>");
        assertThat(result).contains("<title>Test Site</title>");
        assertThat(result).contains("tailwind.config");
        assertThat(result).contains("primaryColor");
        assertThat(result).contains("<div>Welcome</div>");
        assertThat(result).contains("</html>");
    }

    @Test
    void renderSite_ShouldUseDefaultTitle_WhenTitleIsNull() {
        // Given
        SiteConfig siteConfig = new SiteConfig();
        siteConfig.setTitle(null);

        SiteConfig.ComponentNode rootNode = new SiteConfig.ComponentNode();
        rootNode.setType("container");
        siteConfig.setRoot(rootNode);

        when(textRenderer.supports("container")).thenReturn(true);
        when(textRenderer.render(eq(rootNode), any(RenderingService.class))).thenReturn("<div></div>");

        // When
        String result = renderingService.renderSite(siteConfig);

        // Then
        assertThat(result).contains("<title>SiteUp Generated Site</title>");
    }
}