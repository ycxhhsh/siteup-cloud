package com.siteup.engine.renderer;

import com.siteup.engine.model.SiteConfig;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

@Component
public class TextRenderer implements ComponentRenderer {

    @Override
    public boolean supports(String type) {
        return "text".equals(type);
    }

    @Override
    public String render(SiteConfig.ComponentNode node, RenderingService service) {
        StringBuilder html = new StringBuilder();

        // Extract properties
        Map<String, Object> props = node.getProps();
        if (props == null) {
            props = Collections.emptyMap();
        }
        String text = (String) props.get("text");
        String cssClass = (String) props.getOrDefault("className", "");

        // Determine tag based on className or use span as default
        String tag = "span";
        if (cssClass.contains("text-6xl") || cssClass.contains("text-5xl") || cssClass.contains("text-4xl")) {
            tag = "h1";
        } else if (cssClass.contains("text-3xl") || cssClass.contains("text-2xl")) {
            tag = "h2";
        } else if (cssClass.contains("text-xl")) {
            tag = "h3";
        } else if (cssClass.contains("text-lg")) {
            tag = "p";
        }

        // Build HTML
        html.append("<").append(tag);
        if (!cssClass.isEmpty()) {
            html.append(" class=\"").append(cssClass).append("\"");
        }
        html.append(">");
        if (text != null) {
            // Convert newlines to <br/> tags
            String processedText = text.replace("\n", "<br/>");
            html.append(processedText);
        }
        html.append("</").append(tag).append(">");

        return html.toString();
    }
}
