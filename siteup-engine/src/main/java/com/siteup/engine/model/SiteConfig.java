package com.siteup.engine.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * 网站配置模型 (Biz 服务版 - 兼容修复版)
 * 目的：让旧的 Java 代码能无缝读取数据库里的新 JSON 格式
 */
@JsonIgnoreProperties(ignoreUnknown = true) // 1. 忽略未知的字段(如原JSON里多余的字段)，防止报错
public class SiteConfig {

    private String title;

    // 2. 关键修复：@JsonAlias("root")
    // 意思：当解析 JSON 时，如果发现 "root" 字段，也把值赋给这个 contentJson 变量。
    // 这样，虽然数据库存的是 "root"，但你的旧业务代码调用 getContentJson() 依然能拿到数据！
    @JsonAlias("root")
    @JsonProperty("contentJson") // 序列化时依然叫 contentJson (或者根据需要改)
    private ComponentNode contentJson;

    // 3. 新增 themeConfig，防止丢失主题配置
    private Object themeConfig;

    // --- 手动 Getter/Setter (防止 Lombok 失效) ---

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ComponentNode getContentJson() {
        return contentJson;
    }

    public void setContentJson(ComponentNode contentJson) {
        this.contentJson = contentJson;
    }

    public Object getThemeConfig() {
        return themeConfig;
    }

    public void setThemeConfig(Object themeConfig) {
        this.themeConfig = themeConfig;
    }

    // 4. 新增 getRoot() 方法，保持向后兼容性
    // 实际上返回 contentJson，但方法名保持为 getRoot()
    public ComponentNode getRoot() {
        return contentJson;
    }

    public void setRoot(ComponentNode root) {
        this.contentJson = root;
    }

    // --- 内部类 ---

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ComponentNode {
        private String id;
        private String type;
        private Map<String, Object> props;
        private List<ComponentNode> children;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public Map<String, Object> getProps() { return props; }
        public void setProps(Map<String, Object> props) { this.props = props; }

        public List<ComponentNode> getChildren() { return children; }
        public void setChildren(List<ComponentNode> children) { this.children = children; }
    }
}