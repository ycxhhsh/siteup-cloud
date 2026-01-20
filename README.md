# SiteUp Cloud 网站生成平台

基于Spring Cloud Alibaba微服务架构的低代码网站生成平台

## 🆕 最新版本特性

- ✅ **完整的微服务架构**：4个独立服务，每个服务独立数据库
- ✅ **服务治理**：Sentinel熔断限流 + 优雅降级处理
- ✅ **配置中心**：Nacos Config实现配置热更新
- ✅ **API版本管理**：`/api/v1`版本控制和网关路由
- ✅ **分布式链路追踪**：Spring Cloud Sleuth + Zipkin
- ✅ **企业级工程化实践**：单元测试、集成测试、监控、可观测性
- ✅ **API文档**：完整的Swagger/OpenAPI文档和错误码规范
- ✅ **健康检查**：Spring Boot Actuator监控端点
- ✅ **统一异常处理**：标准化的错误响应格式（包含traceId）
- ✅ **自动化测试**：Postman E2E测试套件
- ✅ **生成历史追踪**：记录每次网站生成的历史、耗时、成功率
- ✅ **数据库初始化脚本**：完整的MySQL数据库初始化脚本（含示例数据）
- ✅ **策略模式组件渲染**：基于Spring注入Map的组件渲染器，支持Button/Card/Text/Image组件
- ✅ **Tailwind CSS集成**：自动注入Tailwind CDN，支持响应式设计和现代化样式

## 技术栈

- Spring Boot 3.2.0
- Spring Cloud 2023.0.0
- Spring Cloud Alibaba 2023.0.1.0
- Nacos 服务注册与配置中心
- Spring Cloud Gateway 网关
- OpenFeign 服务调用
- JPA/Hibernate 数据访问
- MySQL 数据库（微服务独立数据库）
- Lombok 代码简化

### 服务治理与可观测性

- **Sentinel**: 熔断限流和流量控制
- **Nacos Config**: 集中化配置管理
- **Spring Cloud Sleuth**: 分布式链路追踪
- **Zipkin**: 链路追踪可视化
- **Spring Boot Actuator**: 健康检查和监控
- **Micrometer + Prometheus**: 指标收集

### 工程化与运维

- SpringDoc OpenAPI (Swagger) 文档
- JUnit 5 + Mockito 单元测试
- Spring Security Test 集成测试
- 统一异常处理和错误码规范
- API版本控制 (/api/v1)

## 项目结构

```
siteup-cloud/
├── siteup-gateway/          # 网关服务 (8010)
├── siteup-auth/            # 认证服务 (8020) - siteup_auth DB
├── siteup-biz/             # 业务服务 (8030) - siteup_biz DB
├── siteup-engine/          # 引擎服务 (8040) - siteup_engine DB
├── docs/                   # 项目文档
│   └── error-codes.md      # 错误码规范
├── database-init.sql       # 完整数据库初始化脚本
├── DATABASE-SETUP.md       # 数据库初始化详细指南
├── siteup_microservices.json       # Postman E2E测试集合
├── component-renderer-test.json   # 新组件渲染器测试用例
├── pom.xml                 # 父项目POM
└── README.md               # 项目文档
```

### 数据库架构

每个微服务使用独立的MySQL数据库：
- `siteup_auth` - 用户认证和Token数据
- `siteup_biz` - 项目和模板数据
- `siteup_engine` - 网站生成历史记录

## 服务说明

### 1. Gateway Service (端口: 8010)
网关服务,统一入口,负责**全局鉴权**、路由转发和API网关功能

**核心功能:**
- 🔐 **统一鉴权**: 验证JWT Token并注入用户信息头
- 🎯 **智能路由**: 根据路径将请求转发到相应微服务
- 🛡️ **安全防护**: 引擎服务仅允许内部访问
- 📊 **请求追踪**: 生成请求ID并记录访问日志

**路由配置:**
- `/api/auth/**` -> siteup-auth (认证服务，无需鉴权)
- `/api/projects/**` -> siteup-biz (业务服务，需鉴权)
- `/api/templates/**` -> siteup-biz (业务服务，模板列表公开)
- `/api/generate/**` -> siteup-engine (引擎服务，仅内部访问)
- `/api/generated/**` -> siteup-biz (业务服务，公开访问)

### 2. Auth Service (端口: 8020)
认证服务,负责用户注册、登录、Token验证（登录时会持久化 token，并提供 verify 接口供网关/服务校验）

**主要接口:**
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/verify` - Token验证

**用户角色:**
- `USER` - 普通用户 (默认)

### 3. Biz Service (端口: 8030)
业务服务,负责项目和模板的核心业务逻辑

**核心功能:**
- 🔐 **信任网关**: 优先使用网关传递的用户信息头，避免重复鉴权
- 📋 **业务处理**: 项目管理和模板操作
- 🔗 **服务协同**: 通过OpenFeign调用引擎服务生成网站

**主要接口:**
- `GET /api/templates` - 获取所有模板（公开访问）
- `GET /api/templates/{id}` - 获取模板详情（公开访问）
- `POST /api/templates/from-template/{templateId}` - 从模板创建项目（需登录）
- `GET /api/projects` - 获取所有项目（需登录）
- `GET /api/projects/{id}` - 获取项目详情（需登录）
- `POST /api/projects/{id}/publish` - 发布项目（需登录）

**鉴权说明:**
业务服务信任来自网关的 `X-User-*` 头信息，无需重复调用认证服务验证token

### 4. Engine Service (端口: 8040)
引擎服务,负责网站配置解析、HTML生成和生成历史记录

**核心功能:**
- 🛡️ **内部访问**: 仅允许通过网关转发且带有内部标记的请求
- 🎨 **HTML渲染**: 将JSON配置转换为完整的HTML页面
- 📈 **历史追踪**: 记录生成统计和性能指标

**主要接口:**
- `POST /api/generate` - 生成网站HTML（仅内部访问）
- `POST /api/generate/with-history` - 生成网站并记录历史（仅内部访问）
- `GET /api/generate/history` - 获取生成历史记录（仅内部访问）
- `GET /api/generate/stats` - 获取生成统计信息（仅内部访问）

**安全策略:**
引擎服务不直接暴露给外部，所有访问必须通过网关并携带 `X-Internal-Call: true` 头

**渲染组件 (策略模式):**
- **TextRenderer** (`@Component("text")`) - 文本段落渲染，支持多行和自定义样式
- **ImageRenderer** (`@Component("image")`) - 图片组件渲染，支持响应式和占位符
- **ButtonRenderer** (`@Component("button")`) - 交互按钮渲染，支持链接和样式变体
- **CardRenderer** (`@Component("card")`) - 卡片组件渲染，支持标题、内容和图片
- **Container/Page兜底渲染** - 递归渲染子组件，支持className样式继承

**历史追踪功能:**
- 生成耗时统计
- 成功/失败率分析
- HTML文件大小记录
- 错误信息记录

**组件渲染器文件结构:**
```
siteup-engine/src/main/java/com/siteup/engine/renderer/
├── ComponentRenderer.java           # 渲染器接口
├── RenderingService.java            # 主渲染服务（策略模式实现）
└── impl/                            # 具体渲染器实现
    ├── ButtonRenderer.java          # 按钮组件渲染器
    ├── CardRenderer.java            # 卡片组件渲染器
    ├── TextRenderer.java            # 文本组件渲染器
    └── ImageRenderer.java           # 图片组件渲染器
```

## 监控与可观测性

### 健康检查
```
GET http://localhost:8010/health
GET http://localhost:8030/actuator/health
```

### 应用指标
```
GET http://localhost:8030/actuator/metrics
GET http://localhost:8030/actuator/prometheus
```

### API文档
```
Swagger UI: http://localhost:8030/swagger-ui.html
OpenAPI JSON: http://localhost:8030/v3/api-docs
```

## 数据库设计

### users 表 (用户表)
```sql
id         BIGINT       主键 (自增)
username   VARCHAR(255) 用户名 (唯一)
password   VARCHAR(255) 密码哈希
role       VARCHAR(255) 用户角色 (默认: USER)
```

### template 表 (模板表)
```sql
id          VARCHAR(255) 主键
name        VARCHAR(255) 模板名称
category    VARCHAR(255) 模板分类
description TEXT         模板描述
preview_url VARCHAR(255) 预览图片URL
config      TEXT         模板配置JSON
```

### project 表 (项目表)
```sql
id             BIGINT       主键 (自增)
name           VARCHAR(255) 项目名称
template_id    VARCHAR(255) 模板ID
user_id        VARCHAR(255) 用户ID
config         TEXT         项目配置JSON
generated_html TEXT         生成的HTML内容
status         VARCHAR(255) 项目状态 (draft/published/archived)
created_at     TIMESTAMP    创建时间
updated_at     TIMESTAMP    更新时间
published_at   TIMESTAMP    发布时间
public_url     VARCHAR(255) 公开访问URL
```

### generation_history 表 (生成历史表)
```sql
id             BIGINT         主键 (自增)
project_id     BIGINT         关联项目ID
template_id    VARCHAR(255)   使用的模板ID
generated_at   TIMESTAMP      生成时间
duration_ms    INT            生成耗时（毫秒）
success        BOOLEAN        是否成功
error_message  TEXT           错误信息（失败时记录）
html_size_kb   DECIMAL(10,2)  生成的HTML大小（KB）
user_id        VARCHAR(255)   操作用户ID
```

## 环境准备

### 1. 安装Nacos
下载Nacos Server并启动:
```bash
# 下载地址: https://github.com/alibaba/nacos/releases
# 启动命令(standalone模式)
sh startup.sh -m standalone
```

访问Nacos控制台: http://localhost:8848/nacos
- 默认账号: nacos
- 默认密码: nacos

### 2. 安装Java 21
确保JDK 21已安装并配置环境变量

### 3. 安装Maven
确保Maven 3.6+已安装并配置环境变量

### 4. 安装并配置MySQL
确保MySQL 8.0+已安装并运行:

```bash
# 启动MySQL服务
sudo systemctl start mysql  # Linux
# 或在Windows/Mac上启动MySQL服务

# 运行数据库初始化脚本
mysql -u root -p < database-init.sql
```

这将创建三个独立的数据库：`siteup_auth`, `siteup_biz`, `siteup_engine`，并包含完整的表结构和示例数据。

### 使用Navicat执行数据库脚本

详细的数据库初始化步骤请参考：[`DATABASE-SETUP.md`](DATABASE-SETUP.md)

**快速步骤：**
1. 打开Navicat，连接MySQL（localhost:3306, root/123456）
2. 新建查询窗口（Ctrl+Q）
3. 导入 `database-init.sql` 文件
4. 点击运行按钮（F5）执行
5. 验证三个数据库已创建：siteup_auth, siteup_biz, siteup_engine

## 启动步骤

### 1. 启动Nacos
```bash
cd nacos/bin
sh startup.sh -m standalone
```

### 2. 编译项目
```bash
cd siteup-cloud
mvn clean install
```

### 3. 启动各个服务

**方式一: 使用Maven (推荐)**
```bash
# 启动网关服务
cd siteup-gateway
mvn spring-boot:run

# 启动认证服务 (新终端)
cd siteup-auth
mvn spring-boot:run

# 启动业务服务 (新终端)
cd siteup-biz
mvn spring-boot:run

# 启动引擎服务 (新终端)
cd siteup-engine
mvn spring-boot:run
```

**方式二: 运行JAR包**
```bash
java -jar siteup-gateway/target/siteup-gateway-1.0.0-SNAPSHOT.jar
java -jar siteup-auth/target/siteup-auth-1.0.0-SNAPSHOT.jar
java -jar siteup-biz/target/siteup-biz-1.0.0-SNAPSHOT.jar
java -jar siteup-engine/target/siteup-engine-1.0.0-SNAPSHOT.jar
```

### 4. 验证服务注册
访问Nacos控制台查看服务列表:
http://localhost:8848/nacos

应该看到以下服务:
- siteup-gateway
- siteup-auth
- siteup-biz
- siteup-engine

## 测试

### 单元测试
```bash
# 运行所有单元测试
mvn test

# 运行特定服务的测试
cd siteup-biz && mvn test
cd siteup-auth && mvn test
cd siteup-engine && mvn test
```

### 集成测试
```bash
# 运行所有集成测试
mvn verify

# 运行特定服务的集成测试
cd siteup-biz && mvn verify
```

### E2E测试 (Postman)
1. 导入 `siteup_microservices.json` 到Postman
2. 确保所有服务已启动（网关必须运行）
3. 设置环境变量：
   - `base_url`: `http://localhost:8010` (网关地址)
   - 其他变量会自动设置
4. 按顺序运行测试：
   - 01. 用户注册
   - 02. 用户登录（获取token）
   - 03. 从模板创建项目（需Authorization头）
   - 04. 发布项目
   - 05. 验证网页内容（公开访问）

**测试要点:**
- 🔐 **鉴权测试**: 未登录请求会被网关拦截返回401
- 🎯 **路由测试**: 不同路径正确路由到对应服务
- 🛡️ **安全测试**: 尝试直接访问引擎服务会被拒绝
- 🎨 **组件渲染测试**: 导入 `component-renderer-test.json` 测试新组件渲染器

#### 新组件渲染器测试
导入 `component-renderer-test.json` 到Postman进行组件渲染测试：

**测试内容:**
- 🖼️ **Image组件**: 图片显示和占位符处理
- 📝 **Text组件**: 多行文本和样式类应用
- 🔘 **Button组件**: 链接跳转和样式变体
- 📄 **Card组件**: 卡片布局和内容展示
- 📦 **Container组件**: 递归渲染和样式继承

**验证方法:**
1. 发送POST请求到 `/api/v1/generate`
2. 在浏览器中打开返回的HTML
3. 检查所有组件是否正确渲染和响应式

### 健康检查
```bash
# 检查网关健康状态
curl http://localhost:8010/health

# 检查各服务健康状态
curl http://localhost:8030/actuator/health
curl http://localhost:8020/actuator/health
curl http://localhost:8040/actuator/health
```

## 核心业务流程

### 网站生成流程
1. **用户注册登录**: 通过认证服务完成用户注册和登录
2. **选择模板**: 浏览和选择预定义的网站模板
3. **创建项目**: 从模板创建新的网站项目
4. **配置内容**: 修改项目配置，定制网站内容和样式
5. **生成网站**: 调用引擎服务将配置转换为HTML
6. **发布项目**: 发布项目并生成公开访问URL

### 数据流转
```
用户请求 → 网关 → 业务服务 → 引擎服务
    ↓         ↓         ↓         ↓
  Token验证  路由转发  业务逻辑  HTML生成
    ↓         ↓         ↓         ↓
 返回结果 ← 响应聚合 ← 数据处理 ← 渲染结果
```

**鉴权流程:**
1. **网关统一验证**: 网关调用 `siteup-auth/verify` 验证token
2. **用户信息注入**: 验证通过后注入 `X-User-Id/Name/Role` 头
3. **业务服务信任**: 业务服务直接使用网关传递的用户信息
4. **引擎安全访问**: 仅允许带有内部标记的请求访问引擎

## 核心功能演示

### 1. 用户注册
```bash
POST http://localhost:8010/api/auth/register
Content-Type: application/json

{
  "username": "demo_user",
  "password": "demo123"
}
```

### 2. 用户登录
```bash
POST http://localhost:8010/api/auth/login
Content-Type: application/json

{
  "username": "demo_user",
  "password": "demo123"
}
```

### 3. 获取模板列表
```bash
GET http://localhost:8010/api/templates
```

### 4. 从模板创建项目
```bash
POST http://localhost:8010/api/templates/from-template/template-001
Content-Type: application/json
Authorization: Bearer <token>

{
  "name": "我的第一个网站"
}
```

### 5. 发布项目
```bash
POST http://localhost:8010/api/projects/1/publish
```

### 6. 访问生成的网站
```bash
GET http://localhost:8010/api/v1/generated/1
```

### 7. 查看生成历史记录
```bash
# 获取所有生成历史
GET http://localhost:8010/api/generate/history

# 获取特定项目的生成历史
GET http://localhost:8010/api/generate/history?projectId=1

# 获取生成统计信息
GET http://localhost:8010/api/generate/stats
```

### 8. 再次发布项目（测试历史记录）
```bash
POST http://localhost:8010/api/projects/1/publish
Authorization: Bearer <token>
```

## API文档与错误处理

### Swagger文档
启动服务后访问：
- Biz Service: http://localhost:8030/swagger-ui.html
- Auth Service: http://localhost:8020/swagger-ui.html (如有API接口)

### 统一错误格式
所有API错误响应都遵循统一格式：

```json
{
  "code": "ERROR_CODE",
  "message": "用户友好的错误信息",
  "timestamp": "2024-01-14T10:30:00Z",
  "detail": "可选的详细信息"
}
```

### 常见错误码
- `RESOURCE_NOT_FOUND` (404): 资源不存在
- `INVALID_REQUEST` (400): 请求参数无效
- `SERVICE_UNAVAILABLE` (503): 服务不可用
- `USER_NOT_FOUND` (404): 用户不存在
- `INVALID_CREDENTIALS` (401): 凭据无效

详细错误码请参考：`docs/error-codes.md`

## 业务逻辑

### 网站生成流程
1. **模板选择**: 用户从预定义模板中选择网站模板
2. **项目创建**: 系统基于模板创建新的项目，复制模板配置
3. **内容配置**: 用户可以修改项目配置，定制网站内容和样式
4. **HTML生成**: 调用引擎服务将JSON配置转换为HTML页面，并记录生成历史
5. **项目发布**: 生成公开访问URL，网站正式上线
6. **历史追踪**: 系统自动记录生成耗时、成功率等统计信息

### 组件渲染机制

#### 策略模式渲染器
系统采用Spring依赖注入的策略模式实现组件渲染：

- **TextRenderer** (`@Component("text")`): 渲染文本段落，支持多行文本和自定义样式类
- **ImageRenderer** (`@Component("image")`): 渲染图片，支持URL、alt属性和响应式样式
- **ButtonRenderer** (`@Component("button")`): 渲染交互按钮，支持链接跳转和样式变体
- **CardRenderer** (`@Component("card")`): 渲染卡片组件，支持标题、内容和图片

#### 渲染流程
1. **策略注入**: Spring自动将所有`ComponentRenderer`注入为`Map<String, ComponentRenderer>`
2. **大小写容错**: 使用`node.getType().toLowerCase()`查找渲染器
3. **递归渲染**: Container/Page类型自动递归渲染子组件
4. **兜底处理**: 未知组件类型返回注释而不抛异常
5. **样式集成**: 自动注入Tailwind CDN，支持现代化响应式设计

#### 支持的组件属性
- **text**: `text`(字符串), `className`(样式类)
- **image**: `src`(图片URL), `alt`(替代文本), `className`(样式类)
- **button**: `text`(按钮文本), `link`(跳转链接), `variant`(样式变体), `target`(链接目标)
- **card**: `title`(标题), `content`(内容), `image`(图片URL)

### 生成历史追踪
系统会自动记录每次网站生成的历史信息，用于分析和优化：
- **性能监控**: 记录生成耗时，计算平均响应时间
- **成功率统计**: 跟踪生成成功/失败的比例
- **错误分析**: 记录失败时的具体错误信息
- **使用分析**: 统计用户生成行为和偏好

### 数据持久化
- 用户信息存储在users表
- 模板配置存储在template表
- 项目数据存储在project表，包括配置和生成的HTML
- 生成历史记录存储在generation_history表，包括生成耗时、成功率等统计信息

## 配置说明

### Nacos配置
所有服务都配置了Nacos服务发现:
```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848
```

### 数据库配置
各服务使用独立的数据库配置，默认为H2内存数据库:
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: update
```

### 网关路由与鉴权配置
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: auth-route
          uri: lb://siteup-auth
          predicates:
            - Path=/api/v1/auth/**
        - id: biz-route
          uri: lb://siteup-biz
          predicates:
            - Path=/api/v1/projects/**,/api/v1/templates/**,/api/v1/sites/**
        - id: engine-route
          uri: lb://siteup-engine
          predicates:
            - Path=/api/v1/generate/**
        - id: sites-route
          uri: lb://siteup-biz
          predicates:
            - Path=/api/v1/generated/**

# 网关鉴权配置
gateway:
  auth:
    excluded-paths:
      - "/api/v1/auth/**"
      - "/api/v1/generated/**"
      - "/api/v1/templates/**"
    engine-internal-only: true  # 引擎服务仅内部访问
```

## 常见问题

### 1. 服务无法注册到Nacos
- 检查Nacos是否正常启动 (访问 http://localhost:8848/nacos)
- 检查各服务的nacos配置地址是否正确
- 检查防火墙和网络连接

### 2. 服务间调用失败
- 检查所有服务是否都已启动
- 检查服务是否成功注册到Nacos
- 查看服务日志排查具体错误

### 3. 数据库连接问题
- 检查H2数据库配置是否正确
- 如果使用MySQL，确保数据库已创建并可访问

### 4. 端口冲突
如果端口被占用，可修改各服务的application.yml中的server.port配置

## 扩展功能建议

- 添加JWT本地验证（替换当前的网关调用认证服务模式）
- 添加用户权限管理系统
- 添加网站模板市场
- 添加网站分析和统计功能
- 添加多语言支持
- 添加网站备份和版本管理
- 添加Redis缓存提升性能
- 添加消息队列处理异步任务
- 添加CI/CD自动化部署
- 添加监控和日志系统

## 项目特点

### 架构特性
- ✅ **微服务架构**：4个独立服务，每个服务独立数据库
- ✅ **服务隔离**：数据库层面的完全隔离
- ✅ **Spring Cloud Alibaba生态**：完整的云原生微服务栈
- ✅ **Nacos服务治理**：服务注册发现和配置中心
- ✅ **Spring Cloud Gateway**：统一网关和路由
- ✅ **OpenFeign调用**：声明式服务间通信

### 工程化特性
- ✅ **企业级监控**：Spring Boot Actuator + Micrometer
- ✅ **API文档**：完整的Swagger/OpenAPI文档
- ✅ **统一异常处理**：标准化的错误响应
- ✅ **全面测试覆盖**：单元测试 + 集成测试 + E2E测试
- ✅ **错误码规范**：详细的错误码文档和处理指南

### 业务特性
- ✅ **低代码网站生成**：模板化拖拽式网站构建
- ✅ **组件化渲染引擎**：灵活可扩展的渲染系统
- ✅ **RESTful API设计**：标准化的接口设计
- ✅ **多租户支持**：用户隔离的项目管理
- ✅ **生成历史追踪**：完整的生成历史记录和性能分析

## 开发环境

- JDK 21
- Maven 3.6+
- Nacos 2.x
- IDE: IntelliJ IDEA / VS Code

## 架构优势

- **高可用**: 微服务架构，单服务故障不影响整体
- **易扩展**: 组件化设计，新功能模块独立开发
- **高性能**: 分布式部署，支持水平扩展
- **易维护**: 服务职责清晰，代码结构规范
- **技术先进**: 基于Spring Cloud最新版本

## 快速开始

### 环境准备

确保以下基础设施服务正在运行：

1. **MySQL数据库** (端口3306)
   ```bash
   # 运行数据库初始化脚本
   mysql -u root -p < database-init.sql

   # 或者使用Navicat执行（推荐）
   # 参考 DATABASE-SETUP.md 获取详细步骤
   ```

2. **Nacos服务发现与配置中心** (端口8848)
   ```bash
   # 下载并启动Nacos
   # https://nacos.io/zh-cn/docs/quick-start.html
   sh startup.sh -m standalone
   ```

3. **Sentinel控制台** (端口8080，可选)
   ```bash
   # 下载并启动Sentinel Dashboard
   # https://github.com/alibaba/Sentinel/releases
   java -Dserver.port=8080 -Dcsp.sentinel.dashboard.server=localhost:8080 -Dproject.name=sentinel-dashboard -jar sentinel-dashboard.jar
   ```

4. **Zipkin链路追踪** (端口9411，可选)
   ```bash
   # 下载并启动Zipkin
   # https://zipkin.io/pages/quickstart.html
   curl -sSL https://zipkin.io/quickstart.sh | bash -s
   java -jar zipkin.jar
   ```

### 服务启动

按以下顺序启动微服务：

```bash
# 1. 启动网关服务
cd siteup-gateway && mvn spring-boot:run

# 2. 启动认证服务
cd siteup-auth && mvn spring-boot:run

# 3. 启动业务服务
cd siteup-biz && mvn spring-boot:run

# 4. 启动引擎服务
cd siteup-engine && mvn spring-boot:run
```

### 验证启动

1. **服务注册验证**
   - 访问 Nacos控制台: http://localhost:8848/nacos
   - 检查所有4个服务是否已注册

2. **API测试**
   - 网关地址: http://localhost:8010
   - API文档: http://localhost:8010/swagger-ui.html
   - 健康检查: http://localhost:8010/actuator/health

3. **监控验证** (可选)
   - Sentinel控制台: http://localhost:8080
   - Zipkin界面: http://localhost:9411

### 创建第一个网站

```bash
# 使用Postman导入测试集合
# 文件: siteup_microservices.json
# 运行"用户注册"和"用户登录"请求
# 然后创建项目并发布网站
```

---

## 🛠️ 架构特性演示

### 熔断限流演示
```bash
# 快速连续请求触发限流
for i in {1..20}; do
  curl -X POST http://localhost:8010/api/v1/projects \
    -H "Content-Type: application/json" \
    -d '{"templateId":"1","userId":"demo","projectName":"test"}' &
done
```

### 配置热更新演示
```bash
# 在Nacos配置中心修改sentinel规则
# 服务会自动应用新配置无需重启
```

### 链路追踪演示
```bash
# 发送API请求后查看Zipkin界面
# 观察完整的请求链路: Gateway -> Biz -> Auth/Engine
```

---

如有问题请提Issue或联系开发者

