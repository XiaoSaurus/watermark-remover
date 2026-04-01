# 微信网页扫码登录功能实现指南

## 📋 功能概述

为去水印小程序完善微信网页扫码登录功能，用户可以通过扫描二维码快速登录，无需输入手机号和验证码。

## 🔧 已完成的实现

### 1. 后端实现 (Java Spring Boot)

#### 创建的文件：

**1. `WechatWebAuthController.java`** - 微信网页授权控制器
- `GET /api/auth/wechat/web/qr-url` - 生成二维码链接
- `GET /api/auth/wechat/web/status?scene={scene}` - 轮询扫码状态
- `GET /api/auth/wechat/callback?code=xxx&state=xxx` - 微信回调处理

**2. `WechatWebAuthService.java` & `WechatWebAuthServiceImpl.java`** - 业务逻辑实现
- 生成微信扫码登录二维码URL
- 管理扫码状态（使用Redis存储）
- 处理微信授权回调
- 自动创建或更新用户信息

#### 核心业务流程：

```
1. 前端请求 /api/auth/wechat/web/qr-url
   ↓
2. 后端生成UUID作为scene，返回微信二维码URL
   ↓
3. 前端使用qrcode库生成二维码显示给用户
   ↓
4. 用户使用微信扫描二维码
   ↓
5. 微信回调 /api/auth/wechat/callback?code=xxx&state=xxx
   ↓
6. 后端获取access_token和用户信息
   ↓
7. 后端创建/更新用户，生成JWT token
   ↓
8. 前端轮询 /api/auth/wechat/web/status?scene=xxx 获取结果
   ↓
9. 前端获取token和用户信息，完成登录
```

#### 状态管理（Redis）：

- `wechat:scan:{scene}` = "pending" - 等待扫码
- `wechat:scan:{scene}` = "scanned" - 已扫码，等待确认
- `wechat:scan:{scene}` = "{json}" - 已确认，包含token和用户信息
- 过期时间：5分钟

#### 配置更新：

**application.yml** 添加了微信开放平台配置：
```yaml
wechat:
  open:
    app-id: YOUR_OPEN_APPID
    app-secret: YOUR_OPEN_APPSECRET
    redirect-uri: http://your-domain.com/api/auth/wechat/callback
```

#### 数据库更新：

User表已包含以下字段：
- `wxWebOpenId` - 微信网页授权openid（新增索引）
- `wxUnionId` - 微信unionid
- `avatar` - 用户头像
- `nickname` - 用户昵称

### 2. 前端实现 (Vue 3)

#### 更新的文件：

**1. `Login.vue`** - 登录页面
- 添加"微信扫码"登录选项
- 集成qrcode库生成二维码
- 实现扫码状态轮询（2秒间隔）
- 显示扫码进度：pending → scanned → confirmed

**2. `auth.js`** - API接口
- `getWechatQrUrl()` - 获取二维码URL
- `checkWechatStatus(scene)` - 检查扫码状态

**3. `user.js`** - Pinia store
- `setToken(token)` - 设置JWT token
- `setUser(userData)` - 设置用户信息

**4. `package.json`** - 依赖管理
- 添加 `qrcode: ^1.5.3` 库

#### 前端UI流程：

```
登录页面
  ↓
选择"微信扫码"
  ↓
显示二维码 (pending状态)
  ↓
用户扫码
  ↓
显示"已扫码，请在手机上确认" (scanned状态)
  ↓
用户在微信中确认
  ↓
显示"登录成功，正在跳转..." (confirmed状态)
  ↓
自动跳转到首页
```

## 🚀 部署步骤

### 1. 申请微信开放平台账号

1. 访问 https://open.weixin.qq.com/
2. 注册开发者账号
3. 创建网站应用
4. 获取 AppID 和 AppSecret
5. 配置授权回调域名（必须是已备案域名）

### 2. 后端配置

1. 更新 `application.yml`：
```yaml
wechat:
  open:
    app-id: 你的AppID
    app-secret: 你的AppSecret
    redirect-uri: https://你的域名/api/auth/wechat/callback
```

2. 确保Redis已启动并配置正确

3. 编译并运行后端：
```bash
cd backend
mvn clean package
java -jar target/watermark-remover-*.jar
```

### 3. 前端配置

1. 安装依赖：
```bash
cd frontend
npm install
```

2. 开发环境运行：
```bash
npm run dev
```

3. 生产环境构建：
```bash
npm run build
```

### 4. 数据库迁移

由于使用了JPA的 `ddl-auto: update`，数据库表会自动更新。如需手动执行SQL：

```sql
-- 添加微信网页openid字段（如果不存在）
ALTER TABLE `user` ADD COLUMN wechat_web_openid VARCHAR(128) COMMENT '微信网页授权openid';

-- 创建索引
CREATE UNIQUE INDEX idx_wechat_web_openid ON `user`(wechat_web_openid);
```

## 🔐 安全考虑

1. **HTTPS必须** - 微信回调必须使用HTTPS
2. **State参数验证** - 已在代码中实现
3. **Token过期** - JWT token有过期时间
4. **Redis过期** - 扫码状态5分钟自动过期
5. **用户隐私** - 只存储必要的用户信息

## 🧪 测试步骤

### 本地测试

1. 启动后端服务
2. 启动前端开发服务
3. 访问登录页面
4. 选择"微信扫码"
5. 使用微信扫描二维码
6. 在微信中确认登录
7. 验证是否成功跳转到首页

### 生产环境测试

1. 确保域名已备案
2. 配置微信开放平台的授权回调域名
3. 使用真实微信账号测试
4. 验证用户信息是否正确保存

## 📊 API响应格式

### 获取二维码

```json
{
  "code": 200,
  "data": {
    "qrUrl": "https://open.weixin.qq.com/connect/qrconnect?appid=...",
    "scene": "uuid-string"
  }
}
```

### 检查状态 - pending

```json
{
  "code": 200,
  "data": {
    "status": "pending"
  }
}
```

### 检查状态 - scanned

```json
{
  "code": 200,
  "data": {
    "status": "scanned"
  }
}
```

### 检查状态 - confirmed

```json
{
  "code": 200,
  "data": {
    "status": "confirmed",
    "token": "jwt-token-string",
    "user": {
      "id": 123456,
      "username": "用户abc1234",
      "phone": null,
      "avatar": "https://...",
      "nickname": "微信昵称",
      "loginType": "wechat",
      "token": "jwt-token-string"
    }
  }
}
```

### 检查状态 - expired

```json
{
  "code": 200,
  "data": {
    "status": "expired"
  }
}
```

## 🐛 常见问题

### 1. 二维码无法扫描

**原因**：可能是qrcode库未正确安装或Canvas不支持
**解决**：
```bash
npm install qrcode --save
```

### 2. 微信回调失败

**原因**：
- 域名未备案
- 授权回调域名配置错误
- HTTPS证书问题

**解决**：
- 确保域名已备案
- 在微信开放平台正确配置回调域名
- 使用有效的HTTPS证书

### 3. 用户信息未保存

**原因**：数据库连接问题或JPA配置错误
**解决**：
- 检查数据库连接
- 查看后端日志
- 确保User表已创建

### 4. 轮询无法获取结果

**原因**：Redis连接问题或状态未正确保存
**解决**：
- 检查Redis是否运行
- 查看后端日志中的Redis错误
- 验证Redis配置

## 📝 后续优化建议

1. **二维码刷新** - 添加手动刷新按钮
2. **超时提示** - 显示倒计时和超时提示
3. **错误处理** - 更详细的错误提示
4. **用户体验** - 添加加载动画和过渡效果
5. **日志记录** - 记录所有登录事件用于审计
6. **限流保护** - 防止恶意扫码请求
7. **多设备支持** - 支持多个设备同时扫码

## 📚 相关文档

- [微信开放平台文档](https://developers.weixin.qq.com/doc/offiaccount/OA_Web_Apps/Wechat_webpage_authorization.html)
- [微信网页授权](https://developers.weixin.qq.com/doc/offiaccount/OA_Web_Apps/Wechat_webpage_authorization.html)
- [QRCode.js文档](https://davidshimjs.github.io/qrcodejs/)

## ✅ 检查清单

- [ ] 微信开放平台账号已创建
- [ ] AppID和AppSecret已获取
- [ ] 域名已备案
- [ ] 授权回调域名已配置
- [ ] application.yml已更新
- [ ] 后端已编译并运行
- [ ] 前端依赖已安装
- [ ] Redis已启动
- [ ] 数据库已更新
- [ ] 本地测试通过
- [ ] 生产环境部署完成

---

**实现完成时间**：2026-04-01
**版本**：1.0.0
