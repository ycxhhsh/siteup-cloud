package com.siteup.engine.renderer;

import com.siteup.engine.model.SiteConfig;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class ContainerRenderer implements ComponentRenderer {

    @Override
    public boolean supports(String type) {
        return "container".equals(type);
    }

    @Override
    public String render(SiteConfig.ComponentNode node, RenderingService service) {
        StringBuilder html = new StringBuilder();

        // Extract properties
        Map<String, Object> props = node.getProps();
        if (props == null) {
            props = Collections.emptyMap();
        }
        String tag = (String) props.getOrDefault("tagName", "div");
        // Validate tag for security - only allow known safe tags
        if (!Set.of("div", "header", "main", "section", "article", "footer", "nav", "aside").contains(tag)) {
            tag = "div";
        }
        String cssClass = (String) props.getOrDefault("className", "");

        // Build opening tag
        html.append("<").append(tag);
        if (!cssClass.isEmpty()) {
            html.append(" class=\"").append(cssClass).append("\"");
        }
        html.append(">\n");

        // Render children recursively
        List<SiteConfig.ComponentNode> children = node.getChildren();
        if (children != null) {
            for (SiteConfig.ComponentNode child : children) {
                html.append(service.render(child)).append("\n");
            }
        }

        // Build closing tag
        html.append("</").append(tag).append(">");

        return html.toString();
    }
}
