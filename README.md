# 短视频去水印工具 ✂️

粘贴短视频分享链接，一键获取无水印视频下载地址。

> ⚠️ 本工具仅供学习研究使用，请勿用于商业用途，下载内容版权归原作者所有。

---

## 支持平台

| 平台 | 链接格式 | 说明 |
|------|---------|------|
| 🎵 抖音 | `v.douyin.com/xxx` | 支持短链/完整链接/分享文案 |
| ⚡ 快手 | `v.kuaishou.com/xxx` | 支持短链/完整链接 |
| 📺 B站 | `b23.tv/xxx` 或 BV号链接 | 音视频分离，需分别下载 |
| 🌐 微博 | `weibo.com/xxx` | 支持视频微博 |
| 📕 小红书 | `xhslink.com/xxx` | 支持视频笔记 |

---

## 快速开始

### 环境要求
- Java 17+ (推荐 JDK 17)
- Node.js 18+ (推荐 Node 20)
- MySQL 8.0+

### 后端（Java + Maven）

```bash
cd backend

# 配置数据库（编辑 src/main/resources/application.yml）
# 修改 password 为你的 MySQL 密码

# 启动
mvn spring-boot:run

# 访问 Swagger API 文档
http://localhost:8080/swagger-ui/index.html
```

### 前端（Node.js）

```bash
cd frontend

# 安装依赖（Node 18+）
npm install

# 启动开发服务器
npm run dev

# 访问
http://localhost:3000
```

> 前端会自动代理 `/api` 请求到后端 `http://localhost:8080`

### 一键启动脚本

```powershell
# 启动后端
cd backend
mvn spring-boot:run

# 新开终端，启动前端
cd frontend
npm run dev
```

---

## API 接口

### 解析视频

```
POST /api/video/parse
Content-Type: application/json

{
  "url": "https://v.douyin.com/xxxxx/"
}
```

**响应示例：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "platform": "douyin",
    "title": "视频标题",
    "author": "作者昵称",
    "cover": "https://...",
    "videoUrls": [
      { "quality": "无水印高清", "url": "https://..." }
    ],
    "shareUrl": "https://v.douyin.com/xxxxx/"
  }
}
```

### 代理下载（解决 403 问题）

```
GET /api/video/download?url={视频URL}&filename={可选文件名}
```

> 建议前端通过此接口下载，可自动设置正确的 Referer，避免 403。

### 解析历史

```
GET    /api/parse-history?page=0&size=20   # 列表
POST   /api/parse-history                    # 保存
DELETE /api/parse-history/{id}              # 删除单条
DELETE /api/parse-history                   # 清空
```

### 下载历史

```
GET    /api/history?page=0&size=20           # 列表
POST   /api/history                          # 保存
DELETE /api/history/{id}                    # 删除单条
DELETE /api/history                         # 清空
```

---

## 数据库配置

编辑 `backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/watermark_remover?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false
    username: root
    password: 123456   # ← 修改为你的密码
  jpa:
    hibernate:
      ddl-auto: update   # 首次启动设为 update，自动建表
```

---

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Spring Boot 3.2 + Tomcat |
| HTTP 客户端 | OkHttp 4.12 |
| HTML 解析 | Jsoup 1.17 |
| JSON 处理 | FastJSON2 2.0 |
| 数据库 | MySQL 8 + Spring Data JPA |
| 前端框架 | Vue 3 + Vite 5 |
| UI 组件 | Element Plus |
| 状态管理 | Pinia |
| HTTP 客户端 | Axios |

---

## 部署说明

### 前端构建

```bash
cd frontend
npm run build
# 输出 dist/ 目录，部署到 Nginx 等静态服务器
```

### 反向代理配置（Nginx）

```nginx
location /api/ {
    proxy_pass http://127.0.0.1:8080/api/;
    proxy_set_header Host $host;
}

location / {
    try_files $uri $uri/ /index.html;
}
```

### 外网访问

使用 cpolar 等内网穿透工具：

```bash
# 后端穿透
cpolar http 8080

# 前端穿透
cpolar http 3000
```

---

## 常见问题

### Q: 解析失败/返回 403？
A: 各大平台反爬策略经常变化，请确认网络可以访问目标平台，或稍后重试。

### Q: B站只能下载 480P？
A: B站未登录用户 API 限制为 480P。更高清晰度需要登录态 Cookie。

### Q: 如何抓包获取 Cookie？
A: 在浏览器登录 B站，打开开发者工具 → Network → 找一个 `api.bilibili.com` 请求 → 复制 `Cookie` 头。
