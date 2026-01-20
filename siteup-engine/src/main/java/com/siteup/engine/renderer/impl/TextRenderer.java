package com.siteup.engine.renderer.impl; // 👈 修改包名

import com.siteup.engine.model.SiteConfig;
import com.siteup.engine.renderer.ComponentRenderer;
import com.siteup.engine.renderer.RenderingService;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.Map;

@Component("text") // 👈 确保这个注解存在
public class TextRenderer implements ComponentRenderer {

    @Override
    public boolean supports(String type) {
        return "text".equals(type);
    }

    @Override
    public String render(SiteConfig.ComponentNode node, RenderingService service) {
        // ... 保持原有逻辑不变，或者复制下面的完整代码 ...
        StringBuilder html = new StringBuilder();
        Map<String, Object> props = node.getProps();
        if (props == null) props = Collections.emptyMap();

        String text = (String) props.get("text");
        String cssClass = (String) props.getOrDefault("className", "");

        // 智能标签选择
        String tag = "span";
        if (cssClass.contains("text-4xl") || cssClass.contains("text-5xl") || cssClass.contains("text-6xl") || cssClass.contains("font-bold")) {
            tag = "h2"; // 稍微优化语义
        } else if (cssClass.contains("text-xl") || cssClass.contains("text-2xl")) {
            tag = "h3";
        } else if (cssClass.contains("block")) {
            tag = "p";
        } else if (cssClass.contains("text-gray-500") || cssClass.contains("text-sm")) {
            tag = "p";
        }

        html.append("<").append(tag);
        if (!cssClass.isEmpty()) {
            html.append(" class=\"").append(cssClass).append("\"");
        }
        html.append(">");
        if (text != null) {
            html.append(text.replace("\n", "<br/>"));
        }
        html.append("</").append(tag).append(">");
        return html.toString();
    }
}