package com.siteup.engine.renderer;

import com.siteup.engine.model.SiteConfig;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

@Component
public class ImageRenderer implements ComponentRenderer {

    @Override
    public boolean supports(String type) {
        return "image".equals(type);
    }

    @Override
    public String render(SiteConfig.ComponentNode node, RenderingService service) {
        StringBuilder html = new StringBuilder();

        // Extract properties
        Map<String, Object> props = node.getProps();
        if (props == null) {
            props = Collections.emptyMap();
        }
        String src = (String) props.get("src");
        String alt = (String) props.getOrDefault("alt", "");
        String cssClass = (String) props.getOrDefault("className", "");

        // Build HTML
        html.append("<img");
        if (src != null) {
            html.append(" src=\"").append(src).append("\"");
        }
        if (!alt.isEmpty()) {
            html.append(" alt=\"").append(alt).append("\"");
        }
        if (!cssClass.isEmpty()) {
            html.append(" class=\"").append(cssClass).append("\"");
        }
        html.append(" />");

        return html.toString();
    }
}
