# 更新日志

## v1.1.0 (2026-04-01)

### 新增功能
- **多平台支持**：抖音、快手、B站、微博、小红书
- **智能解析引擎**：自动识别平台、提取无水印链接
- **代理下载接口** `/api/video/download`：解决第三方平台 403 问题
- **后端代理下载**：按平台自动设置正确的 Referer，避免 403 Forbidden
- **历史记录管理**：解析历史 + 下载历史，支持分页、删除、清空
- **双端部署**：Web 前端（Vite + Vue3）+ 微信小程序端

### 性能优化
- 后端 Spring Boot 3.2 升级，Hibernate 警告已消除
- 前端 node_modules 升级至 Node 20 兼容版本

### Bug 修复
- 修复 `/download` 接口 Referer 写死为抖音导致其他平台 403
- 修复 `MySQL8Dialect` 废弃警告

### 技术栈
- **后端**：Spring Boot 3.2 + MySQL 8 + OkHttp 4.12 + Jsoup 1.17
- **前端**：Vue 3 + Vite 5 + Element Plus + Pinia + Axios

---

## v1.0.0 (初始版本)
- 基础抖音去水印功能
