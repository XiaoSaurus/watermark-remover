# 去水印小程序

基于 Vue3 + SpringBoot 的去水印工具，支持图片/视频去水印。

## 技术栈
- **前端**：Vue 3 + Vite + Element Plus + Axios
- **后端**：Spring Boot 3 + FFmpeg + OpenCV

## 功能
- 图片去水印（基于 OpenCV 图像修复）
- 视频去水印（基于 FFmpeg 区域遮盖）
- 批量处理
- 历史记录

## 快速开始

### 后端
```bash
cd backend
mvn spring-boot:run
```

### 前端
```bash
cd frontend
npm install
npm run dev
```

## 接口文档
启动后访问：http://localhost:8080/swagger-ui.html