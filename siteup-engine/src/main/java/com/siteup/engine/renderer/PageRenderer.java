package com.siteup.engine.renderer;

import com.siteup.engine.model.SiteConfig;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class PageRenderer implements ComponentRenderer {

    @Override
    public boolean supports(String type) {
        return "page".equals(type);
    }

    @Override
    public String render(SiteConfig.ComponentNode node, RenderingService service) {
        StringBuilder html = new StringBuilder();

        // Extract properties
        Map<String, Object> props = node.getProps();
        if (props == null) {
            props = Collections.emptyMap();
        }
        String cssClass = (String) props.getOrDefault("className", "");

        // Build opening tag
        html.append("<div");
        if (!cssClass.isEmpty()) {
            html.append(" class=\"").append(cssClass).append("\"");
        }
        html.append(">\n");

        // Render children
        List<SiteConfig.ComponentNode> children = node.getChildren();
        if (children != null) {
            for (SiteConfig.ComponentNode child : children) {
                html.append(service.render(child)).append("\n");
            }
        }

        // Build closing tag
        html.append("</div>\n");

        return html.toString();
    }
}
