package com.siteup.engine.renderer.impl;

import com.siteup.engine.model.SiteConfig;
import com.siteup.engine.renderer.ComponentRenderer;
import com.siteup.engine.renderer.RenderingService;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.Map;

@Component("button")
public class ButtonRenderer implements ComponentRenderer {

    @Override
    public boolean supports(String type) {
        return "button".equals(type);
    }

    @Override
    public String render(SiteConfig.ComponentNode node, RenderingService service) {
        Map<String, Object> props = node.getProps();
        if (props == null) props = Collections.emptyMap();

        String text = (String) props.getOrDefault("text", "Button");
        String link = (String) props.getOrDefault("link", "#");

        // 1. 获取 JSON 里的 className (透传)
        String customClass = (String) props.getOrDefault("className", "");

        // 2. 定义默认样式 (防止 JSON 里没写样式时按钮太丑)
        // 如果 JSON 里传了样式，通常我们会追加，或者由 JSON 完全接管。
        // 为了简单起见，这里采用 "默认样式 + 自定义样式" 的拼接方式
        String defaultClass = "inline-block px-6 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 transition-colors duration-200";

        // 如果自定义样式里包含了 "bg-" 或 "text-"，可能想要覆盖默认颜色。
        // 简单策略：直接拼接，让 CSS 的层叠特性去处理（或者完全信任 JSON）
        String finalClass = customClass.isEmpty() ? defaultClass : customClass;

        return String.format("<a href=\"%s\" class=\"%s\">%s</a>", link, finalClass, text);
    }
}