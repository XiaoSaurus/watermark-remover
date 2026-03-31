# 去水印小程序 - 微信小程序端

## 目录结构
```
miniprogram/
├── app.js              # 全局逻辑（历史记录管理）
├── app.json            # 全局配置（页面、TabBar）
├── app.wxss            # 全局样式
├── project.config.json # 微信开发者工具配置
├── utils/
│   └── api.js          # API 封装
├── pages/
│   ├── index/          # 解析下载页
│   └── history/        # 历史记录页
└── images/             # TabBar 图标（需自行添加）
```

## 使用前配置

### 1. 配置后端地址
打开 `app.js`，修改 `apiBase` 为你的后端服务器 IP：
```js
apiBase: 'http://192.168.1.100:8080'  // 改成你的局域网 IP 或服务器地址
```
> ⚠️ 微信小程序不支持 localhost，开发时需用局域网 IP

### 2. 配置 AppID
打开 `project.config.json`，将 `appid` 改为你的小程序 AppID：
```json
"appid": "wx你的appid"
```

### 3. 配置合法域名（上线时）
在微信公众平台 → 开发 → 开发设置 → 服务器域名，添加：
- request 合法域名：`https://你的域名`
- downloadFile 合法域名：`https://你的域名`

### 4. 添加 TabBar 图标
在 `images/` 目录放入以下图片（81×81px，PNG）：
- `home.png` / `home_active.png`
- `history.png` / `history_active.png`

## 开发调试
1. 下载 [微信开发者工具](https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html)
2. 导入项目，选择 `miniprogram/` 目录
3. 在开发者工具中勾选「不校验合法域名」（开发阶段）
4. 启动后端：`cd backend && mvn spring-boot:run`
5. 查看本机 IP：`ipconfig`，更新 `app.js` 中的 `apiBase`

## 功能
- ✅ 粘贴分享链接/文案自动解析
- ✅ 支持抖音、快手、B站、微博、小红书
- ✅ 无水印视频下载到相册
- ✅ 复制视频链接
- ✅ 历史记录（本地存储，最多50条）