CREATE DATABASE IF NOT EXISTS siteup_auth
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS siteup_biz
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS siteup_engine
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE siteup_auth;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(255) NOT NULL UNIQUE COMMENT '用户名，唯一',
    password VARCHAR(255) NOT NULL COMMENT '密码哈希',
    role VARCHAR(255) NOT NULL DEFAULT 'USER' COMMENT '用户角色',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT '用户表';

-- 认证Token表
CREATE TABLE IF NOT EXISTS auth_token (
    token VARCHAR(500) PRIMARY KEY COMMENT 'JWT Token字符串',
    user_id BIGINT NOT NULL COMMENT '关联用户ID',
    issued_at TIMESTAMP NOT NULL COMMENT 'Token发放时间',
    expires_at TIMESTAMP NULL COMMENT 'Token过期时间',
    INDEX idx_user_id (user_id),
    INDEX idx_expires_at (expires_at)
) COMMENT '认证Token表';

-- 插入示例用户数据
INSERT IGNORE INTO users (username, password, role) VALUES
('demo_user', '$2a$10$xHcLpJCJZMJ9X8VzKU8rUe6N6YrO8dKF8qQzU8dKF8qQzU8dKF8qQ', 'USER'),
('admin', '$2a$10$xHcLpJCJZMJ9X8VzKU8rUe6N6YrO8dKF8qQzU8dKF8qQzU8dKF8qQ', 'ADMIN');

USE siteup_biz;

-- 模板表
CREATE TABLE IF NOT EXISTS template (
    id VARCHAR(255) PRIMARY KEY COMMENT '模板ID（字符串，如：template-001）',
    name VARCHAR(255) NOT NULL COMMENT '模板名称',
    description TEXT COMMENT '模板描述',
    category VARCHAR(255) NOT NULL COMMENT '模板分类：Blog, Portfolio, SaaS',
    thumbnail_url VARCHAR(500) COMMENT '缩略图URL',
    config TEXT COMMENT '模板配置JSON（页面结构）',
    theme_config TEXT COMMENT '主题配置JSON（样式配置）',
    active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否激活',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_category (category),
    INDEX idx_active (active)
) COMMENT '网站模板表';

-- 项目表
CREATE TABLE IF NOT EXISTS project (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '项目ID',
    name VARCHAR(255) NOT NULL COMMENT '项目名称',
    template_id VARCHAR(255) NOT NULL COMMENT '使用的模板ID',
    user_id VARCHAR(255) NOT NULL COMMENT '创建者用户ID',
    config TEXT COMMENT '项目配置JSON（从模板复制并修改）',
    generated_html TEXT COMMENT '生成的HTML内容',
    status VARCHAR(255) NOT NULL DEFAULT 'draft' COMMENT '项目状态：draft, published, archived',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    published_at TIMESTAMP NULL COMMENT '发布时间',
    public_url VARCHAR(500) COMMENT '公开访问URL',
    INDEX idx_user_id (user_id),
    INDEX idx_template_id (template_id),
    INDEX idx_status (status)
) COMMENT '用户项目表';
-- 插入示例模板数据
INSERT IGNORE INTO template (id, name, description, category, thumbnail_url, config, theme_config, active) VALUES
(
    'template-001', 
    '博客', 
    'Blog', 
    'https://images.unsplash.com/photo-1499750310159-5254f4cc157e?auto=format&fit=crop&w=800&q=80',
    '{"themeConfig":{"primaryColor":"#3B82F6","fontFamily":"Inter"},"root":{"id":"root","type":"container","props":{"className":"min-h-screen bg-gray-50 flex flex-col md:flex-row font-sans"},"children":[{"id":"sidebar","type":"container","props":{"className":"w-full md:w-64 bg-slate-900 text-white flex-shrink-0"},"children":[{"id":"sidebar-sticky","type":"container","props":{"className":"sticky top-0 p-6"},"children":[{"id":"logo","type":"text","props":{"text":"DEV.LOG","className":"text-2xl font-bold tracking-widest border-b border-slate-700 pb-4 mb-8 block"}},{"id":"nav-1","type":"button","props":{"text":"🏠 首页推荐","link":"#","className":"block w-full text-left py-2 px-4 rounded hover:bg-slate-800 text-slate-300 hover:text-white transition mb-2"}},{"id":"nav-2","type":"button","props":{"text":"⚡️ 最新动态","link":"#","className":"block w-full text-left py-2 px-4 rounded hover:bg-slate-800 text-slate-300 hover:text-white transition mb-2"}},{"id":"nav-3","type":"button","props":{"text":"📚 技术专栏","link":"#","className":"block w-full text-left py-2 px-4 rounded hover:bg-slate-800 text-slate-300 hover:text-white transition mb-2"}},{"id":"sub-btn","type":"button","props":{"text":"订阅周刊","className":"mt-8 w-full bg-blue-600 hover:bg-blue-500 text-white py-2 rounded text-center text-sm font-bold"}}]}]},{"id":"main-content","type":"container","props":{"className":"flex-1 p-6 md:p-12"},"children":[{"id":"header-sec","type":"container","props":{"className":"mb-12 border-b pb-8"},"children":[{"id":"h-tag","type":"text","props":{"text":"Featured Story","className":"text-blue-600 font-bold text-sm tracking-wide uppercase mb-2"}},{"id":"h-title","type":"text","props":{"text":"微服务架构的未来：Serverless 与边缘计算的融合","className":"text-4xl md:text-5xl font-extrabold text-gray-900 leading-tight mb-4"}},{"id":"h-desc","type":"text","props":{"text":"本文深入探讨了下一代云原生架构的演进方向，以及开发者如何应对这一变革...","className":"text-xl text-gray-500 max-w-2xl"}}]},{"id":"grid-posts","type":"container","props":{"className":"grid md:grid-cols-2 lg:grid-cols-3 gap-8"},"children":[{"id":"card-1","type":"card","props":{"title":"Spring Boot 3.2 新特性解析","content":"虚拟线程正式到来，性能提升显著。","image":"https://images.unsplash.com/photo-1605379399642-870262d3d051?auto=format&fit=crop&w=600&q=80","className":"h-full hover:-translate-y-1 transition duration-300 shadow-sm hover:shadow-xl border-0"}},{"id":"card-2","type":"card","props":{"title":"Rust vs Go：谁是后端之王？","content":"从内存安全到并发模型，深度对比两大热门语言。","image":"https://images.unsplash.com/photo-1555066931-4365d14bab8c?auto=format&fit=crop&w=600&q=80","className":"h-full hover:-translate-y-1 transition duration-300 shadow-sm hover:shadow-xl border-0"}},{"id":"card-3","type":"card","props":{"title":"Kubernetes 故障排查指南","content":"生产环境常见 CrashLoopBackOff 解决方案。","image":"https://images.unsplash.com/photo-1667372393119-c81c0cda0a29?auto=format&fit=crop&w=600&q=80","className":"h-full hover:-translate-y-1 transition duration-300 shadow-sm hover:shadow-xl border-0"}}]}]}]}}',
    '{"primaryColor":"#3B82F6","fontFamily":"Inter"}',
    TRUE
),
(
    'template-002', 
    '个人作品集', 
    'Portfolio', 
    'https://images.unsplash.com/photo-1550745165-9bc0b252726f?auto=format&fit=crop&w=800&q=80',
    '{"themeConfig":{"primaryColor":"#8B5CF6","fontFamily":"Poppins"},"root":{"id":"root","type":"container","props":{"className":"min-h-screen bg-slate-950 text-white p-4 md:p-8 font-sans flex items-center justify-center"},"children":[{"id":"grid-container","type":"container","props":{"className":"max-w-6xl w-full grid grid-cols-1 md:grid-cols-4 md:grid-rows-3 gap-4 h-full md:h-[800px]"},"children":[{"id":"profile-box","type":"container","props":{"className":"md:col-span-2 md:row-span-2 bg-slate-900/50 border border-slate-800 rounded-3xl p-8 relative overflow-hidden group hover:border-purple-500/50 transition duration-500"},"children":[{"id":"glow","type":"container","props":{"className":"absolute -top-20 -right-20 w-64 h-64 bg-purple-600 rounded-full blur-[100px] opacity-30 group-hover:opacity-50 transition duration-500"}},{"id":"my-img","type":"image","props":{"src":"https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=200&h=200","className":"w-24 h-24 rounded-full border-4 border-slate-800 mb-6 object-cover"}},{"id":"my-name","type":"text","props":{"text":"Alex Chen","className":"text-4xl font-bold mb-2"}},{"id":"my-role","type":"text","props":{"text":"全栈开发者 & UI 设计师","className":"text-purple-400 text-lg mb-4"}},{"id":"my-desc","type":"text","props":{"text":"我构建高性能的 Web 应用，并追求极致的用户体验。擅长 React, Java 与云计算技术。","className":"text-slate-400 leading-relaxed max-w-sm"}}]},{"id":"map-box","type":"container","props":{"className":"md:col-span-1 md:row-span-1 bg-slate-800 rounded-3xl overflow-hidden relative border border-slate-700"},"children":[{"id":"map-bg","type":"image","props":{"src":"https://images.unsplash.com/photo-1524661135-423995f22d0b?auto=format&fit=crop&w=600&q=80","className":"w-full h-full object-cover opacity-60 hover:scale-110 transition duration-700"}},{"id":"loc-text","type":"text","props":{"text":"📍 Shanghai, CN","className":"absolute bottom-4 left-4 bg-black/60 backdrop-blur px-3 py-1 rounded-full text-xs font-bold"}}]},{"id":"social-box","type":"container","props":{"className":"md:col-span-1 md:row-span-2 bg-gradient-to-b from-purple-600 to-indigo-700 rounded-3xl p-6 flex flex-col justify-between text-center hover:scale-[1.02] transition"},"children":[{"id":"social-title","type":"text","props":{"text":"Let us Connect","className":"text-2xl font-bold text-white/90"}},{"id":"social-btns","type":"container","props":{"className":"space-y-3"},"children":[{"id":"gh-btn","type":"button","props":{"text":"GitHub","link":"#","className":"block w-full bg-white/10 hover:bg-white/20 py-2 rounded-xl text-sm backdrop-blur"}},{"id":"tw-btn","type":"button","props":{"text":"Twitter","link":"#","className":"block w-full bg-white/10 hover:bg-white/20 py-2 rounded-xl text-sm backdrop-blur"}},{"id":"em-btn","type":"button","props":{"text":"Email Me","link":"#","className":"block w-full bg-white text-purple-600 font-bold py-2 rounded-xl text-sm shadow-lg"}}]}]},{"id":"tech-box","type":"container","props":{"className":"md:col-span-2 md:row-span-1 bg-slate-900 border border-slate-800 rounded-3xl p-6 flex flex-col justify-center"},"children":[{"id":"tech-title","type":"text","props":{"text":"技术栈","className":"text-slate-500 text-xs font-bold uppercase tracking-wider mb-3"}},{"id":"tech-icons","type":"text","props":{"text":"React • Next.js • Tailwind • Spring Cloud • Docker • Nacos","className":"text-xl md:text-2xl font-mono text-slate-200"}}]},{"id":"stats-box","type":"container","props":{"className":"md:col-span-2 md:row-span-1 bg-slate-800 rounded-3xl p-8 flex items-center justify-between border border-slate-700"},"children":[{"id":"stat-1","type":"container","props":{},"children":[{"id":"s1-num","type":"text","props":{"text":"5+","className":"text-4xl font-bold text-white block"}},{"id":"s1-lbl","type":"text","props":{"text":"Years Exp.","className":"text-slate-400 text-sm"}}]},{"id":"stat-2","type":"container","props":{},"children":[{"id":"s2-num","type":"text","props":{"text":"50+","className":"text-4xl font-bold text-white block"}},{"id":"s2-lbl","type":"text","props":{"text":"Projects","className":"text-slate-400 text-sm"}}]},{"id":"stat-3","type":"container","props":{},"children":[{"id":"s3-num","type":"text","props":{"text":"100%","className":"text-4xl font-bold text-white block"}},{"id":"s3-lbl","type":"text","props":{"text":"Commitment","className":"text-slate-400 text-sm"}}]}]}]}]}}',
    '{"primaryColor":"#8B5CF6","fontFamily":"Poppins"}',
    TRUE
),
(
    'template-003', 
    'SiteUp Cloud 官方文档', 
    'SaaS', 
    'https://images.unsplash.com/photo-1460925895917-afdab827c52f?auto=format&fit=crop&w=800&q=80',
    '{"themeConfig":{"primaryColor":"#2563EB","fontFamily":"Inter"},"root":{"id":"root","type":"container","props":{"className":"min-h-screen bg-white font-sans text-slate-900 selection:bg-blue-100 selection:text-blue-700"},"children":[{"id":"nav","type":"container","props":{"className":"fixed top-0 w-full bg-white/90 backdrop-blur-md z-50 border-b border-slate-100"},"children":[{"id":"nav-inner","type":"container","props":{"className":"max-w-7xl mx-auto px-6 h-20 flex items-center justify-between"},"children":[{"id":"logo","type":"text","props":{"text":"SiteUp Cloud.","className":"text-2xl font-black tracking-tighter text-blue-600"}},{"id":"github-btn","type":"button","props":{"text":"GitHub Repo ->","link":"https://github.com/your-repo","className":"hidden md:inline-flex bg-slate-900 text-white px-5 py-2.5 rounded-lg text-sm font-medium hover:bg-slate-800 transition"}}]}]},{"id":"hero","type":"container","props":{"className":"pt-32 pb-20 px-6 max-w-7xl mx-auto text-center"},"children":[{"id":"badge","type":"text","props":{"text":"🚀 基于 Spring Cloud Alibaba 构建","className":"inline-block bg-blue-50 text-blue-700 px-4 py-1.5 rounded-full text-sm font-bold mb-8 border border-blue-100"}},{"id":"h1","type":"text","props":{"text":"为开发者打造的 微服务低代码平台","className":"text-5xl md:text-7xl font-extrabold tracking-tight text-slate-900 mb-8 leading-[1.1]"}},{"id":"desc","type":"text","props":{"text":"无需繁琐的前端工程化配置。SiteUp 引擎内置 Tailwind CSS，结合 Java 21 虚拟线程，提供极致的后端渲染性能。","className":"text-xl text-slate-500 mb-10 max-w-2xl mx-auto leading-relaxed"}},{"id":"actions","type":"container","props":{"className":"flex flex-col sm:flex-row gap-4 justify-center mb-16"},"children":[{"id":"btn-start","type":"button","props":{"text":"立即创建项目","link":"/dashboard","className":"inline-flex justify-center items-center px-8 py-4 bg-blue-600 text-white rounded-xl font-bold hover:bg-blue-700 transition shadow-xl shadow-blue-600/20"}},{"id":"btn-doc","type":"button","props":{"text":"查看架构文档","link":"/docs","className":"inline-flex justify-center items-center px-8 py-4 bg-white text-slate-700 border border-slate-200 rounded-xl font-bold hover:bg-slate-50 transition"}}]},{"id":"mockup-wrap","type":"container","props":{"className":"relative max-w-5xl mx-auto"},"children":[{"id":"glow","type":"container","props":{"className":"absolute -inset-1 bg-gradient-to-r from-blue-500 to-cyan-500 rounded-2xl blur opacity-20"}},{"id":"mockup","type":"image","props":{"src":"https://images.unsplash.com/photo-1460925895917-afdab827c52f?auto=format&fit=crop&w=1200&q=80","className":"relative rounded-xl border border-slate-200 shadow-2xl w-full bg-white"}}]}]},{"id":"features","type":"container","props":{"className":"py-24 bg-slate-50"},"children":[{"id":"ft-inner","type":"container","props":{"className":"max-w-7xl mx-auto px-6"},"children":[{"id":"ft-title","type":"text","props":{"text":"核心技术栈","className":"text-3xl font-bold text-center mb-16 text-slate-900"}},{"id":"ft-grid","type":"container","props":{"className":"grid md:grid-cols-3 gap-8"},"children":[{"id":"c1","type":"card","props":{"title":"⚡️ 极速渲染","content":"Engine 服务基于纯 Java 实现，利用策略模式动态组装组件，毫秒级生成 HTML。","className":"bg-white p-8 rounded-2xl shadow-sm border border-slate-100 hover:shadow-lg transition"}},{"id":"c2","type":"card","props":{"title":"🛡️ 熔断限流","content":"集成 Alibaba Sentinel，自动处理高并发流量，保障服务稳定性与可用性。","className":"bg-white p-8 rounded-2xl shadow-sm border border-slate-100 hover:shadow-lg transition"}},{"id":"c3","type":"card","props":{"title":"☁️ 配置中心","content":"使用 Nacos 管理微服务配置，支持动态刷新，实现真正的云原生架构体验。","className":"bg-white p-8 rounded-2xl shadow-sm border border-slate-100 hover:shadow-lg transition"}}]}]}]},{"id":"footer","type":"container","props":{"className":"bg-slate-900 text-slate-400 py-12 text-center"},"children":[{"id":"copy","type":"text","props":{"text":"© 2026 SiteUp Cloud. Built with ❤️ by Java Developers.","className":"text-sm"}}]}]}}',
    '{"primaryColor":"#2563EB","fontFamily":"Inter"}',
    TRUE
);

USE siteup_engine;

-- 生成历史表（记录每次网站生成的历史）
CREATE TABLE IF NOT EXISTS generation_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    project_id BIGINT NOT NULL COMMENT '关联项目ID',
    template_id VARCHAR(255) COMMENT '使用的模板ID',
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',
    duration_ms INT COMMENT '生成耗时（毫秒）',
    success BOOLEAN DEFAULT TRUE COMMENT '是否成功',
    error_message TEXT COMMENT '错误信息（失败时记录）',
    html_size_kb DECIMAL(10,2) COMMENT '生成的HTML大小（KB）',
    user_id VARCHAR(255) COMMENT '操作用户ID',
    INDEX idx_project_id (project_id),
    INDEX idx_generated_at (generated_at),
    INDEX idx_success (success)
) COMMENT '网站生成历史记录表';

-- 为root用户授予所有数据库的权限
GRANT ALL PRIVILEGES ON siteup_auth.* TO 'root'@'localhost';
GRANT ALL PRIVILEGES ON siteup_biz.* TO 'root'@'localhost';
GRANT ALL PRIVILEGES ON siteup_engine.* TO 'root'@'localhost';

-- 刷新权限
FLUSH PRIVILEGES;

-- 显示创建的数据库
SELECT 'Database Status Check:' as Info;
SHOW DATABASES LIKE 'siteup_%';

-- 显示各数据库的表
SELECT 'siteup_auth tables:' as Info;
USE siteup_auth;
SHOW TABLES;

SELECT 'siteup_biz tables:' as Info;
USE siteup_biz;
SHOW TABLES;

SELECT 'siteup_engine tables:' as Info;
USE siteup_engine;
SHOW TABLES;