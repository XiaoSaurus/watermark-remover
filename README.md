# 去水印工具 ✂️

粘贴短视频分享链接，一键获取无水印视频下载地址。

## 支持平台

| 平台 | 链接格式 | 说明 |
|------|---------|------|
| 🎵 抖音 | `v.douyin.com/xxx` | 支持短链/完整链接/分享文案 |
| ⚡ 快手 | `v.kuaishou.com/xxx` | 支持短链/完整链接 |
| 📺 B站 | `b23.tv/xxx` 或 BV号链接 | 音视频分离，需分别下载 |
| 🌐 微博 | `weibo.com/xxx` | 支持视频微博 |
| 📕 小红书 | `xhslink.com/xxx` | 支持视频笔记 |

## 技术栈

- **后端**：Spring Boot 3 + OkHttp + Jsoup + FastJSON2
- **前端**：Vue 3 + Vite + Element Plus

## 快速开始

### 后端（需要 Java 17 + Maven）

```bash
cd backend
mvn spring-boot:run
# 启动后访问 http://localhost:8080/swagger-ui.html
```

### 前端（需要 Node.js 16+）

```bash
cd frontend
npm install
npm run dev
# 访问 http://localhost:3000
```

## 使用方法

1. 打开任意支持平台的 App，找到视频
2. 点击「分享」→「复制链接」或「复制文案」
3. 粘贴到输入框，点击「立即解析」
4. 选择清晰度，点击「下载视频」

## API 接口

```
POST /api/video/parse
Content-Type: application/json

{ "url": "https://v.douyin.com/xxxxx/" }
```

## 免责声明

本工具仅供学习研究使用，请勿用于商业用途，下载内容版权归原作者所有。