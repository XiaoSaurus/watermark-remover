# watermark-remover 项目全量扫描报告

- 生成时间：2026-04-03
- 扫描范围：`backend`、`frontend`、`miniprogram`、根目录配置与说明文件
- 报告版本：v2026-04-03

---

## 1. 项目定位

该项目是一个“短视频去水印解析与下载”系统，提供：

- Web 前端（Vue）
- 微信小程序端（原生小程序）
- Java 后端 API（解析、下载代理、用户系统、历史记录）

目标是把各平台分享链接解析为可下载资源，并提供统一的历史与用户能力。

---

## 2. 语言与框架

## 2.1 主要编程语言

- Java 17（后端）
- JavaScript（Web 前端与小程序）
- SQL（数据库迁移与初始化脚本）
- YAML（后端配置）

## 2.2 后端技术栈

- 核心框架：Spring Boot 3.2
- 安全认证：Spring Security + JWT
- 数据访问：Spring Data JPA + MySQL 8
- 缓存/验证码频控：Redis
- HTTP 抓取：OkHttp
- 内容解析：FastJSON2、Jsoup
- API 文档：springdoc-openapi（Swagger UI）
- 其他：Lombok、Hutool、阿里云短信 SDK

## 2.3 Web 前端技术栈

- Vue 3
- Vue Router 4
- Pinia
- Axios
- Element Plus
- Vite 5

## 2.4 小程序端

- 微信小程序原生框架（`app.json` + `wxml/wxss/js`）
- 封装统一 API 请求工具，复用后端接口

---

## 3. 总体架构设计

## 3.1 分层与职责

- **接入层（Controller）**：接收请求、参数校验与响应封装
- **业务层（Service）**：平台解析、用户认证、历史记录、资料管理
- **解析器层（Parser）**：按平台拆分实现（抖音/快手/B站/微博/小红书）
- **数据层（Repository）**：JPA 仓储访问 MySQL
- **安全层（Filter + Security）**：JWT 鉴权过滤、路由放行策略

## 3.2 核心设计方案

1. **插件式解析器方案**
   - 定义统一接口 `VideoParser`
   - `WatermarkServiceImpl` 注入 `List<VideoParser>`，按 `supports()` 动态选择解析器
   - 优点：新增平台时只需新增解析器类，扩展性好

2. **下载代理方案**
   - 提供 `/api/video/download` 代理下载
   - 按目标平台动态设置 `Referer` 与 `User-Agent`
   - 解决浏览器直接下载被第三方平台 403 拦截问题

3. **多端统一后端方案**
   - Web 与小程序均调用同一套 REST API
   - 历史记录、鉴权、用户资料等业务逻辑集中在后端，降低端侧重复实现

4. **多登录方式统一收敛**
   - 登录入口统一为 `/api/auth/login`
   - 通过 `type` 分发手机号/微信网页/微信小程序/游客登录流程

---

## 4. 模块拆解

## 4.1 后端模块

- 视频解析与下载：`/api/video/*`
- 认证与用户：`/api/auth/*`
- 下载历史：`/api/history/*`
- 解析历史：`/api/parse-history/*`
- 头像与资料管理：`/api/avatar/*`、`/api/auth/profile` 等

## 4.2 Web 前端模块

- 页面：首页解析、解析历史、下载历史、登录注册找回、个人资料
- 路由守卫：对需要登录的页面执行前置校验
- API 层：`watermark.js` 与 `auth.js` 分模块封装

## 4.3 小程序模块

- Tab 页面：解析、解析历史、下载历史、我的
- 认证页面：登录、注册、重置密码、绑定手机、编辑资料
- `utils/api.js` 提供统一 `request` 封装，自动带 `Authorization`

---

## 5. 数据与状态

## 5.1 数据库与实体（观察结果）

- 用户：`User`
- 下载历史：`DownloadHistory`
- 解析历史：`ParseHistory`
- 头像：`Avatar`
- 包含 SQL 脚本与 migration 文件（初始化头像、用户资料增强）

## 5.2 鉴权与会话

- JWT 作为无状态令牌（`Authorization: Bearer <token>`）
- `JwtAuthFilter` 解析令牌并把 `userId` 写入认证上下文
- 安全策略中对公开 API 放行，其余接口需认证

---

## 6. 关键接口能力（归纳）

- 视频解析：`POST /api/video/parse`
- 代理下载：`GET /api/video/download`
- 登录注册：`/api/auth/login`、`/api/auth/register`
- 短信验证码：`/api/auth/sms/send`、`/api/auth/sms/verify`
- 历史记录：`/api/history`、`/api/parse-history`
- 用户资料：`/api/auth/profile`、`/api/auth/password`、`/api/auth/bind-phone`

---

## 7. 工程结构与运行方式

## 7.1 工程组织

- `backend`：Maven 单体后端
- `frontend`：Vite + Vue Web 应用
- `miniprogram`：微信小程序工程

## 7.2 本地运行依赖

- JDK 17+
- Node.js 18+/20
- MySQL 8
- Redis（用于短信与频控等能力）

---

## 8. 当前方案的优点与注意点

## 8.1 优点

- 解析器抽象清晰，可持续扩展平台
- 多端共享同一业务后端，维护成本较低
- 下载代理有效应对第三方反爬场景
- 认证与业务模块边界相对明确

## 8.2 注意点

- `application.yml` 中含示例密钥与数据库密码占位，建议分环境配置并改为环境变量
- 平台解析高度依赖第三方页面/API 结构，需持续跟进策略变化
- 若并发增长，建议补充接口限流、熔断与任务队列
- 文档中部分历史描述与现状可能不完全一致，应以代码实现为准

---

## 9. 版本化文档管理建议

- 本报告已落在：`docs/project-audit/versions/2026-04-03/PROJECT_OVERVIEW.md`
- 后续每次扫描生成新目录，例如：
  - `docs/project-audit/versions/2026-04-10/PROJECT_OVERVIEW.md`
  - `docs/project-audit/versions/2026-04-17/PROJECT_OVERVIEW.md`
- 若一天多次扫描，可使用带时间目录名（如 `2026-04-03-0930`）

这样可直接对比不同版本文档，追踪技术选型、模块和方案的变化轨迹。
