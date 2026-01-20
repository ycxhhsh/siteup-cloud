# 数据库初始化指南

本文档详细介绍如何使用Navicat或其他MySQL客户端执行SiteUp Cloud项目的数据库初始化脚本。

## 📋 前置要求

- MySQL 8.0+ 已安装并运行
- Navicat或其他MySQL客户端工具
- 数据库root用户权限

## 🔧 使用Navicat执行脚本

### 步骤1：连接MySQL服务器

1. 打开Navicat
2. 点击"连接" → "MySQL"
3. 填写连接信息：
   ```
   连接名: SiteUp Cloud
   主机: localhost
   端口: 3306
   用户名: root
   密码: 123456
   ```
4. 点击"测试连接"确保连接成功
5. 点击"确定"保存连接

### 步骤2：打开查询窗口

1. 在左侧找到刚刚创建的连接
2. 右键点击连接名
3. 选择"新建查询"（或按 `Ctrl+Q`）

### 步骤3：导入并执行脚本

#### 方法一：直接导入文件
1. 点击菜单栏"文件" → "打开文件"
2. 选择项目根目录下的 `database-init.sql` 文件
3. 文件内容会自动加载到查询窗口

#### 方法二：复制粘贴内容
1. 打开 `database-init.sql` 文件
2. 全选所有内容（Ctrl+A）
3. 复制（Ctrl+C）
4. 粘贴到Navicat查询窗口（Ctrl+V）

### 步骤4：执行脚本

1. 确认查询窗口中有完整的SQL脚本
2. 点击工具栏的"运行"按钮（或按 `F5`）
3. 等待执行完成

### 步骤5：验证执行结果

#### 检查数据库创建
```sql
-- 在新查询窗口中执行
SHOW DATABASES LIKE 'siteup_%';
```
预期结果：
```
siteup_auth
siteup_biz
siteup_engine
```

#### 检查表创建
```sql
-- 检查siteup_auth数据库
USE siteup_auth;
SHOW TABLES;

-- 检查siteup_biz数据库
USE siteup_biz;
SHOW TABLES;

-- 检查siteup_engine数据库
USE siteup_engine;
SHOW TABLES;
```

#### 检查示例数据
```sql
-- 检查用户数据
USE siteup_auth;
SELECT id, username, role FROM users;

-- 检查模板数据
USE siteup_biz;
SELECT id, name, category FROM template;
```

## 🐛 常见问题解决

### 问题1：连接失败
**错误信息：** `Access denied for user 'root'@'localhost'`

**解决方案：**
1. 检查MySQL服务是否运行
2. 确认用户名和密码正确
3. 检查用户权限：`GRANT ALL PRIVILEGES ON *.* TO 'root'@'localhost';`

### 问题2：脚本执行失败
**错误信息：** `Database 'siteup_auth' already exists`

**解决方案：**
脚本是幂等的，可以重复执行。如果担心数据丢失，可以先备份：
```sql
mysqldump -u root -p --all-databases > backup.sql
```

### 问题3：中文乱码
**解决方案：**
确保MySQL字符集设置为UTF-8：
```sql
SHOW VARIABLES LIKE 'character_set%';
```

## 📊 脚本内容说明

### 创建的数据库
- `siteup_auth`: 认证服务数据库
  - `users` 表：用户信息
  - `auth_token` 表：认证令牌

- `siteup_biz`: 业务服务数据库
  - `template` 表：网站模板
  - `project` 表：用户项目

- `siteup_engine`: 引擎服务数据库
  - `generation_history` 表：生成历史记录

### 示例数据
- 2个测试用户（demo_user, admin）
- 3个预设模板（博客、作品集、SaaS）
- 完整的表结构和索引

## 🔄 重新初始化

如果需要重新初始化数据库：

```bash
# 方式1：删除并重新创建（会丢失数据）
DROP DATABASE IF EXISTS siteup_auth;
DROP DATABASE IF EXISTS siteup_biz;
DROP DATABASE IF EXISTS siteup_engine;

# 然后重新执行初始化脚本
mysql -u root -p < database-init.sql

# 方式2：使用Navicat删除数据库
# 在Navicat左侧找到数据库，右键"删除数据库"
# 然后重新执行初始化脚本
```

## 🎯 验证启动

数据库初始化完成后，可以启动微服务进行验证：

```bash
# 1. 启动Nacos
# 2. 启动各个服务
cd siteup-gateway && mvn spring-boot:run
cd ../siteup-auth && mvn spring-boot:run
cd ../siteup-biz && mvn spring-boot:run
cd ../siteup-engine && mvn spring-boot:run

# 3. 使用Postman测试
# 导入 siteup_microservices.json 并运行测试
```

## 📞 技术支持

如果遇到问题，请检查：
1. MySQL版本是否为8.0+
2. 用户权限是否正确
3. 端口3306是否被占用
4. Navicat版本是否支持MySQL 8.0

遇到无法解决的问题，请查看项目README.md或联系开发者。
