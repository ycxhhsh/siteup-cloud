# SiteUp Cloud 网站生成平台

基于 Spring Cloud Alibaba 微服务架构的低代码网站生成平台。

## 技术栈

- Spring Boot 3.2.0
- Spring Cloud 2023.0.0
- Spring Cloud Alibaba 2023.0.1.0
- Nacos 服务注册与配置中心
- Spring Cloud Gateway 网关
- OpenFeign 服务调用
- JPA / Hibernate 数据访问
- MySQL 8.0
- SpringDoc OpenAPI 3 (Swagger)
- Java 21

## 项目结构

```
siteup-cloud/
├── siteup-gateway/            # 网关服务 (8010)
├── siteup-auth/               # 认证服务 (8020)
├── siteup-biz/                # 业务服务 (8030)
├── siteup-engine/             # 引擎服务 (8040)
├── init.sql                   # 数据库初始化脚本
├── siteup_microservices.json  # Postman业务流程测试集合
├── component-renderer-test.json # Postman组件渲染测试集合
└── pom.xml                    # 父项目POM
```

## 服务说明

### 1. Gateway Service (端口: 8010)

网关服务，统一入口，负责路由转发与全局鉴权。

**路由配置:**

- `/api/v1/auth/**` -> siteup-auth
- `/api/v1/projects/**` -> siteup-biz
- `/api/v1/templates/**` -> siteup-biz
- `/api/v1/generate/**` -> siteup-engine
- `/api/v1/generated/**` -> siteup-biz

**鉴权配置:**

- 白名单（无需鉴权）：
  - `/api/v1/auth/register`
  - `/api/v1/auth/login`
  - `/api/v1/auth/verify`
  - `/api/v1/templates/**`
  - `/api/v1/generated/**`
- 引擎服务访问策略：默认 `gateway.auth.engine-internal-only=true`（引擎接口倾向仅内部调用）。

### 2. Auth Service (端口: 8020)

认证服务，负责用户注册、登录、Token验证。

**主要接口:**

- `POST /api/v1/auth/register` - 用户注册
- `POST /api/v1/auth/login` - 用户登录
- `POST /api/v1/auth/verify` - Token验证（供网关/服务调用）

### 3. Biz Service (端口: 8030)

业务服务，负责模板、项目、发布等业务。

**主要接口:**

- `GET /api/v1/templates` - 获取模板列表（公开）
- `GET /api/v1/templates/{id}` - 获取模板详情（公开）
- `POST /api/v1/templates/from-template/{templateId}` - 从模板创建项目（需登录）
- `GET /api/v1/projects` - 获取项目列表（需登录）
- `GET /api/v1/projects/{id}` - 获取项目详情（需登录）
- `POST /api/v1/projects/{id}/publish` - 发布项目（需登录）
- `GET /api/v1/generated/{projectId}` - 获取生成的HTML（公开）

### 4. Engine Service (端口: 8040)

引擎服务，负责将JSON配置渲染为HTML，并记录生成历史。

**主要接口（经网关转发）:**

- `POST /api/v1/generate`
- `POST /api/v1/generate/with-history`
- `GET /api/v1/generate/history`
- `GET /api/v1/generate/stats`

## 数据库设计

以 `init.sql` 为准，项目采用分库设计：

### siteup_auth 库

#### users 表

- `id` BIGINT 主键自增
- `username` 用户名（唯一）
- `password` 密码哈希
- `role` 角色（默认 USER）

#### auth_token 表

- `token` 主键（JWT字符串）
- `user_id` 关联用户ID
- `issued_at` 发放时间
- `expires_at` 过期时间（可为空）

### siteup_biz 库

#### template 表

- `id` 模板ID（如 template-001）
- `name` 模板名称
- `category` 分类（Blog/Portfolio/SaaS）
- `config` 模板配置JSON
- `theme_config` 主题配置JSON

#### project 表

- `id` 项目ID 主键自增
- `name` 项目名称
- `template_id` 模板ID
- `user_id` 创建者用户ID
- `config` 项目配置JSON
- `generated_html` 生成的HTML
- `status` draft/published/archived
- `public_url` 公开访问URL

### siteup_engine 库

#### generation_history 表

- `id` 记录ID 主键自增
- `project_id` 关联项目ID
- `duration_ms` 耗时
- `success` 是否成功
- `error_message` 失败原因
- `html_size_kb` HTML大小

## 环境准备

### 1. 安装Nacos

下载 Nacos Server 并启动（standalone 模式）：

```bash
sh startup.sh -m standalone
OR startup.cmd -m standalone（Windows开发环境）
```

访问 Nacos 控制台：`http://localhost:8848/nacos`（默认 nacos/nacos）。

### 2. 安装MySQL

确保 MySQL 8.0+ 已安装并运行。

### 3. 初始化数据库（权威脚本）

执行项目根目录下的 `init.sql`：

```bash
mysql -u root -p < init.sql
```

默认连接配置（见各服务 `application.yml`）：

- 用户名：`root`
- 密码：`123456`

## 启动步骤

### 1. 启动Nacos

确保 Nacos 正常运行。

### 2. 编译项目

在项目根目录下执行：

```bash
mvn clean install
```

### 3. 启动各个服务

建议按顺序在不同终端启动：

```bash
mvn -pl siteup-auth spring-boot:run
mvn -pl siteup-biz spring-boot:run
mvn -pl siteup-engine spring-boot:run
mvn -pl siteup-gateway spring-boot:run
```

Windows 下也可以使用脚本：`start-services.ps1`。

### 4. 验证服务注册

访问 Nacos 控制台，预期能看到以下服务：

- siteup-gateway
- siteup-auth
- siteup-biz
- siteup-engine

## API文档

项目集成 Swagger（SpringDoc OpenAPI），可通过各服务端口访问：

- 业务服务：`http://localhost:8030/swagger-ui.html`
- 认证服务：`http://localhost:8020/swagger-ui.html`

## 测试

### 使用Postman测试

- 导入 `siteup_microservices.json`，覆盖注册/登录/创建项目/发布/访问等流程。
- 导入 `component-renderer-test.json`，验证引擎组件渲染输出。

## 初始数据

执行 `init.sql` 后包含示例数据：

- users：`demo_user`、`admin`
- template：3个模板（Blog/Portfolio/SaaS）

## 常见问题

### 1. 服务无法注册到Nacos

- 检查 Nacos 是否启动，以及地址是否为 `127.0.0.1:8848`。
- 检查各服务 `spring.cloud.nacos.discovery.server-addr` 配置。

### 2. 数据库连接失败

- 检查 MySQL 是否启动。
- 确认已执行 `init.sql`。
- 检查 `application.yml` 中的数据库用户名/密码是否正确。

### 3. 端口冲突

如果端口被占用，可修改各服务 `application.yml` 中的 `server.port`。

