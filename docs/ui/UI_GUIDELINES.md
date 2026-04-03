# 去水印工具 · 简约 UI 规范

本文档与代码中的设计令牌（`frontend/src/assets/styles/theme.css`、`miniprogram/app.wxss`）保持一致，便于后续迭代时对照。

## 1. 设计原则

- **少装饰**：避免大面积紫渐变；页面以浅灰底 + 白卡片为主。
- **单焦点色**：品牌色 `#6c63ff` 仅用于主按钮、链接、选中态与关键强调。
- **层次靠留白与边框**：优先用浅分割线与轻阴影区分区块，而非色块堆叠。
- **圆角统一**：组件级以 **8～12px** 为主（令牌见 `--border-radius-*`）。
- **字体**：中文优先系统字体栈，正文行高约 **1.5～1.6**。

## 2. 颜色

| 用途 | 亮色模式 | 说明 |
|------|----------|------|
| 页面背景 | `#f7f8fa` | `--bg-page` |
| 卡片背景 | `#ffffff` | `--bg-card` |
| 主色 | `#6c63ff` | `--color-primary` |
| 主色浅 | `#8b85ff` | hover / 次要强调 |
| 正文 | `#111827` | `--text-primary` |
| 次要文字 | `#4b5563` | `--text-secondary` |
| 辅助文字 | `#9ca3af` | `--text-muted` |
| 边框 | `#e5e7eb` | `--border-color` |

深色模式沿用 `data-theme="dark"` 下的变量，主色不变，背景与文字对比度以可读为准。

## 3. 阴影与渐变

- **卡片**：使用中性浅阴影 `--shadow-card`，避免紫色光晕。
- **主按钮**：**纯色主色** + 轻微 `--shadow-btn`；不再使用厚重紫渐变投影。
- **全页背景**：使用 `--bg-page` 纯色，不再用 `--gradient-bg` 铺满整页（历史变量名保留时仅作兼容，语义为「页面底色」）。

## 4. Web（Vue + Element Plus）

- **导航栏**：白底 + 底部分割线，文案为深灰；激活项为浅紫底 `--nav-link-active-bg`。
- **Element Plus**：在 `element-plus-overrides.css` 中统一 `--el-color-primary` 与圆角基准。
- **表单页**（登录/注册/重置）：与首页一致，浅灰底 + 白卡片容器。

## 5. 微信小程序

- **全局导航栏**：白底 + 深色标题（`app.json`），与 Web 顶栏气质一致。
- **页面背景**：`#f7f8fa`，与 Web `--bg-page` 对齐。
- **主按钮**：纯色主色 + 轻阴影；进度条等用 `--color-primary` 而非强渐变。

## 6. 相关文件

| 文件 | 作用 |
|------|------|
| `frontend/src/assets/styles/theme.css` | 全局 CSS 变量 |
| `frontend/src/assets/styles/base.css` | 重置、`.card`、滚动条 |
| `frontend/src/assets/styles/element-plus-overrides.css` | Element Plus 主题覆盖 |
| `frontend/src/main.js` | 引入 Element Plus 覆盖样式 |
| `frontend/src/components/Navbar.vue` | 顶栏样式 |
| `frontend/src/views/Home.vue` | 首页 Hero、主按钮 |
| `frontend/src/views/Login.vue` / `Register.vue` / `ResetPassword.vue` | 认证页容器与按钮 |
| `frontend/src/views/ParseHistory.vue` / `History.vue` | 历史列表徽章与下载按钮 |
| `miniprogram/app.js` | 五套主题：`gradBtn` / `gradBg` 为**纯色**页背景 + 主色按钮 |
| `miniprogram/app.wxss` | 小程序全局变量与通用类 |
| `miniprogram/app.json` | 导航栏白底、页面背景 `#f7f8fa` |
| `miniprogram/pages/index/*` | 首页 Hero 浅色文案 |
| `miniprogram/pages/login|register|reset/*.wxss` | 认证页浅灰底与纯色主按钮 |
| `miniprogram/pages/user/user.wxss` | 「我的」头部与会员卡片 |

修改视觉时优先改 **令牌文件**，再按需微调单页 scoped 样式。

## 7. 图标与图形

- **Web（Vue）**：品牌与能力图标统一使用 `@element-plus/icons-vue`（如 `Film` 品牌、`Promotion` 亮点、`VideoPlay`/`Lightning`/`Film`/`Share`/`Notebook` 对应平台胶囊等）；登录方式使用 `Iphone`、`ChatRound`、`User`；等级使用 `Trophy`。避免用剪刀 emoji 表示视频类功能。
- **微信小程序**：无统一图标库时，输入行左侧使用 **单字标签**（机/验/密等）或 **圆角字块** 样式；列表空状态使用 `empty-icon-circle`（`app.wxss`）线条图形；尽量少用装饰性 emoji，避免与简约风格冲突。
