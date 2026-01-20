package com.siteup.engine.renderer;

import com.siteup.engine.model.SiteConfig;

public interface ComponentRenderer {
    boolean supports(String type);
    String render(SiteConfig.ComponentNode node, RenderingService service);
}
