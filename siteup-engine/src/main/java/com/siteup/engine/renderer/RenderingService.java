package com.siteup.engine.renderer;

import com.siteup.engine.model.GenerationHistory;
import com.siteup.engine.model.SiteConfig;
import com.siteup.engine.repository.GenerationHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 渲染服务：使用 Spring 注入的 Map<String, ComponentRenderer> 实现策略分发。
 *
 * 渲染流程：
 *  - 生成 HTML 头部（包含 Tailwind CDN）
 *  - 递归渲染 root 节点（container/page 会递归渲染子节点）
 */
@Service
public class RenderingService {

    private final Map<String, ComponentRenderer> rendererMap;

    @Autowired(required = false)
    private GenerationHistoryRepository historyRepository;

    @Autowired
    public RenderingService(Map<String, ComponentRenderer> rendererMap) {
        this.rendererMap = rendererMap;
    }

    /**
     * 渲染整个站点配置为 HTML 字符串
     */
    public String renderSite(SiteConfig siteConfig) {
        StringBuilder html = new StringBuilder();

        html.append("<!doctype html>\n");
        html.append("<html lang=\"en\">\n");
        html.append("<head>\n");
        html.append("  <meta charset=\"utf-8\" />\n");
        html.append("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\" />\n");
        html.append("  <title>").append(escape(siteConfig.getTitle())).append("</title>\n");
        // Tailwind CDN
        html.append("  <script src=\"https://cdn.tailwindcss.com\"></script>\n");
        html.append("</head>\n");
        html.append("<body class=\"antialiased bg-gray-50 text-gray-900\">\n");

        if (siteConfig != null && siteConfig.getRoot() != null) {
            html.append(renderComponentRecursively(siteConfig.getRoot()));
        }

        html.append("\n</body>\n</html>");
        return html.toString();
    }

    /**
     * 渲染网站并记录生成历史
     */
    public String renderSiteWithHistory(SiteConfig siteConfig, Long projectId,
                                       String templateId, String userId) {
        long startTime = System.currentTimeMillis();
        GenerationHistory history = null;

        try {
            // 创建历史记录（如果提供了项目ID且有历史仓库）
            if (projectId != null && historyRepository != null) {
                history = new GenerationHistory();
                history.setProjectId(projectId);
                history.setTemplateId(templateId);
                history.setUserId(userId);
                history.setSuccess(false); // 默认设为失败，成功后再更新
                history.setGeneratedAt(LocalDateTime.now());
            }

            // 生成HTML
            String html = renderSite(siteConfig);

            // 更新历史记录（如果创建了且有历史仓库）
            if (history != null && historyRepository != null) {
                long duration = System.currentTimeMillis() - startTime;
                double sizeKb = html.length() / 1024.0;

                history.setSuccess(true);
                history.setDurationMs((int) duration);
                history.setHtmlSizeKb(BigDecimal.valueOf(sizeKb));

                historyRepository.save(history);
            }

            return html;

        } catch (Exception e) {
            // 记录失败历史
            if (history != null && historyRepository != null) {
                history.setErrorMessage(e.getMessage());
                historyRepository.save(history);
            }
            throw e; // 重新抛出异常
        }
    }

    /**
     * 渲染单个组件节点（供渲染器内部调用）
     */
    public String render(SiteConfig.ComponentNode node) {
        return renderComponentRecursively(node);
    }

    /**
     * 递归渲染节点：
     * - 如果 rendererMap 中存在对应类型的渲染器，使用之；
     * - 否则，如果类型为 container 或 page，则递归渲染子节点并使用容器 className（如果有）；
     * - 其他未知类型，返回注释占位以便调试（不抛异常）。
     */
    public String renderComponentRecursively(SiteConfig.ComponentNode node) {
        if (node == null) return "";

        String type = node.getType();
        if (type != null) {
            // 大小写容错：统一转小写查找
            ComponentRenderer renderer = rendererMap.get(type.toLowerCase());
            if (renderer != null) {
                return renderer.render(node, this);
            }

            // 策略模式兜底：container/page 递归渲染
            if ("container".equalsIgnoreCase(type) || "page".equalsIgnoreCase(type)) {
                String className = "";
                if (node.getProps() != null && node.getProps().get("className") != null) {
                    className = String.valueOf(node.getProps().get("className"));
                }
                StringBuilder sb = new StringBuilder();
                sb.append("<div");
                if (!className.isBlank()) {
                    sb.append(" class=\"").append(escapeAttr(className)).append("\"");
                }
                sb.append(">");
                List<SiteConfig.ComponentNode> children = node.getChildren();
                if (children != null) {
                    for (SiteConfig.ComponentNode child : children) {
                        sb.append(renderComponentRecursively(child));
                    }
                }
                sb.append("</div>");
                return sb.toString();
            }
        }

        // 未知类型：返回注释（不抛异常）
        return "<!-- Unknown component type: " + escape(type) + " -->";
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static String escapeAttr(String s) {
        return escape(s).replace("\"", "&quot;").replace("\n", "").replace("\r", "");
    }
}
