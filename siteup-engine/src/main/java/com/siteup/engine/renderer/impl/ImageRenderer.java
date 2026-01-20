package com.siteup.engine.renderer.impl; // 👈 修改包名

import com.siteup.engine.model.SiteConfig;
import com.siteup.engine.renderer.ComponentRenderer;
import com.siteup.engine.renderer.RenderingService;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.Map;

@Component("image")
public class ImageRenderer implements ComponentRenderer {

    @Override
    public boolean supports(String type) {
        return "image".equals(type);
    }

    @Override
    public String render(SiteConfig.ComponentNode node, RenderingService service) {
        StringBuilder html = new StringBuilder();
        Map<String, Object> props = node.getProps();
        if (props == null) props = Collections.emptyMap();

        String src = (String) props.get("src");
        String alt = (String) props.getOrDefault("alt", "");
        String cssClass = (String) props.getOrDefault("className", "");

        html.append("<img");
        if (src != null) html.append(" src=\"").append(src).append("\"");
        if (!alt.isEmpty()) html.append(" alt=\"").append(alt).append("\"");

        // 默认加上 lazy load 和 decoding async，性能优化
        html.append(" loading=\"lazy\" decoding=\"async\"");

        if (!cssClass.isEmpty()) {
            html.append(" class=\"").append(cssClass).append("\"");
        }
        html.append(" />");
        return html.toString();
    }
}