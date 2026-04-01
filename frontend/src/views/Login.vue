<template>
  <div class="login-page">
    <!-- 返回按钮 -->
    <button class="back-btn" @click="router.back()" title="返回">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M19 12H5M12 19l-7-7 7-7"/>
      </svg>
    </button>

    <div class="login-wrapper">
      <div class="login-container">
        <!-- 头部 -->
        <div class="login-header">
          <div class="logo-wrapper">
            <span class="logo">✂️</span>
          </div>
          <h1>去水印工具</h1>
          <p>快速登录或注册</p>
        </div>

        <!-- 登录方式选择器 -->
        <div class="login-mode-selector">
          <button 
            v-for="mode in loginModes" 
            :key="mode.value"
            :class="['mode-btn', { active: loginType === mode.value }]"
            @click="loginType = mode.value"
          >
            <span class="mode-icon">{{ mode.icon }}</span>
            <span class="mode-label">{{ mode.label }}</span>
          </button>
        </div>

        <!-- 登录表单 -->
        <div class="form-wrapper">
          <!-- 手机号登录 -->
          <template v-if="loginType === 'phone'">
            <el-form ref="loginForm" :model="loginData" :rules="loginRules" class="auth-form">
              <el-form-item prop="phone">
                <el-input 
                  v-model="loginData.phone" 
                  placeholder="请输入手机号"
                  maxlength="11"
                  clearable
                  class="form-input"
                >
                  <template #prefix>
                    <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"/>
                    </svg>
                  </template>
                </el-input>
              </el-form-item>

              <el-form-item prop="code">
                <div class="code-input-group">
                  <el-input 
                    v-model="loginData.code" 
                    placeholder="验证码"
                    maxlength="6"
                    clearable
                    class="form-input"
                  />
                  <el-button 
                    @click="sendSms('login')" 
                    :disabled="smsLoading || smsCooldown > 0"
                    :loading="smsLoading"
                    class="send-code-btn"
                  >
                    {{ smsCooldown > 0 ? `${smsCooldown}s` : '发送' }}
                  </el-button>
                </div>
              </el-form-item>

              <el-button 
                type="primary" 
                @click="handleLogin" 
                :loading="loading" 
                class="submit-btn"
              >
                登录
              </el-button>
            </el-form>
          </template>

          <!-- 微信扫码登录 -->
          <template v-if="loginType === 'wechat_web'">
            <div class="wechat-login">
              <div class="qrcode-placeholder">
                <svg viewBox="0 0 24 24" fill="currentColor">
                  <path d="M3 3h8v8H3V3zm10 0h8v8h-8V3zM3 13h8v8H3v-8zm10 0h8v8h-8v-8z"/>
                </svg>
              </div>
              <p class="qrcode-text">扫描二维码登录</p>
              <p class="qrcode-hint">使用微信扫一扫快速登录</p>
            </div>
          </template>

          <!-- 游客登录 -->
          <template v-if="loginType === 'tourist'">
            <div class="guest-login">
              <div class="guest-icon">
                <svg viewBox="0 0 24 24" fill="currentColor">
                  <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
                </svg>
              </div>
              <p class="guest-text">游客登录</p>
              <p class="guest-hint">无需注册，立即体验</p>
              <el-button 
                type="primary" 
                @click="handleLogin" 
                :loading="loading" 
                class="submit-btn"
              >
                游客登录
              </el-button>
            </div>
          </template>
        </div>

        <!-- 底部链接 -->
        <div class="auth-footer">
          <span v-if="loginType !== 'tourist'" class="footer-text">
            没有账号？
            <el-link type="primary" @click="switchToRegister">立即注册</el-link>
          </span>
          <span v-if="loginType === 'phone'" class="footer-text">
            <el-link type="primary" @click="switchToReset">忘记密码？</el-link>
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { authApi } from '@/api/auth'

const router = useRouter()
const userStore = useUserStore()

const loginType = ref('phone')
const loading = ref(false)
const smsLoading = ref(false)
const smsCooldown = ref(0)

const loginData = ref({ phone: '', code: '' })

// 登录方式列表
const loginModes = [
  { value: 'phone', label: '手机号', icon: '📱' },
  { value: 'wechat_web', label: '微信', icon: '🔐' },
  { value: 'tourist', label: '游客', icon: '👤' }
]

// 手机号验证规则
const phoneValidator = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入手机号'))
  } else if (!/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('请输入正确的手机号'))
  } else {
    callback()
  }
}

// 验证码验证规则
const codeValidator = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入验证码'))
  } else if (!/^\d{6}$/.test(value)) {
    callback(new Error('验证码为6位数字'))
  } else {
    callback()
  }
}

const loginRules = {
  phone: [{ validator: phoneValidator, trigger: 'blur' }],
  code: [{ validator: codeValidator, trigger: 'blur' }]
}

const loginForm = ref(null)

// 发送短信验证码
async function sendSms(scene) {
  const phone = loginData.value.phone
  
  if (!phone) {
    ElMessage.error('请先输入手机号')
    return
  }
  if (!/^1[3-9]\d{9}$/.test(phone)) {
    ElMessage.error('手机号格式不正确')
    return
  }

  smsLoading.value = true
  try {
    const res = await authApi.sendSms(phone, scene)
    if (res.code === 200) {
      ElMessage.success('验证码已发送')
      smsCooldown.value = 60
      const timer = setInterval(() => {
        smsCooldown.value--
        if (smsCooldown.value <= 0) clearInterval(timer)
      }, 1000)
    } else {
      handleSmsError(res)
    }
  } catch (e) {
    ElMessage.error(e.message || '发送失败')
  } finally {
    smsLoading.value = false
  }
}

// 处理短信发送错误
function handleSmsError(res) {
  const msg = res.message || ''
  if (res.code === 400) {
    ElMessage.error('手机号格式不正确')
  } else if (res.code === 429) {
    const match = msg.match(/(\d+)/)
    const sec = match ? match[1] : 60
    ElMessage.error(`请${sec}秒后再试`)
    smsCooldown.value = parseInt(sec)
    const timer = setInterval(() => {
      smsCooldown.value--
      if (smsCooldown.value <= 0) clearInterval(timer)
    }, 1000)
  } else if (res.code === 403) {
    ElMessage.error('短信服务暂不可用')
  } else {
    ElMessage.error(msg || '发送失败')
  }
}

// 登录
async function handleLogin() {
  if (loginType.value === 'tourist') {
    loading.value = true
    try {
      // 游客登录：传递空对象，不需要验证表单
      await userStore.login('tourist', {})
      ElMessage.success('游客登录成功')
      router.push('/')
    } catch (e) {
      ElMessage.error(e.message || '游客登录失败')
    } finally {
      loading.value = false
    }
    return
  }

  try {
    await loginForm.value.validate()
  } catch {
    return
  }

  loading.value = true
  try {
    await userStore.login(loginType.value, loginData.value)
    ElMessage.success('登录成功')
    router.push('/')
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    loading.value = false
  }
}

// 切换到注册页
function switchToRegister() {
  router.push('/register')
}

// 切换到重置密码页
function switchToReset() {
  router.push('/reset-password')
}

// 返回上一页
function goBack() {
  router.back()
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: var(--gradient-bg);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  position: relative;
  overflow: hidden;
}

/* 返回按钮 */
.back-btn {
  position: fixed;
  top: 20px;
  left: 20px;
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.9);
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-primary);
  transition: var(--transition);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  z-index: 100;
}

.back-btn:hover {
  background: white;
  transform: translateX(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
}

.back-btn svg {
  width: 20px;
  height: 20px;
}

/* 登录包装器 */
.login-wrapper {
  width: 100%;
  max-width: 420px;
  animation: slideUp 0.6s cubic-bezier(0.34, 1.56, 0.64, 1);
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 登录容器 */
.login-container {
  background: var(--bg-card);
  border-radius: 24px;
  box-shadow: 0 20px 60px rgba(108, 99, 255, 0.2);
  padding: 48px 32px;
  backdrop-filter: blur(10px);
}

/* 头部 */
.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.logo-wrapper {
  width: 80px;
  height: 80px;
  margin: 0 auto 20px;
  background: linear-gradient(135deg, #6c63ff, #764ba2);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 24px rgba(108, 99, 255, 0.3);
  animation: bounce 0.6s cubic-bezier(0.34, 1.56, 0.64, 1);
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.logo {
  font-size: 48px;
  display: block;
}

.login-header h1 {
  font-size: 32px;
  font-weight: 800;
  color: var(--text-primary);
  margin-bottom: 8px;
  letter-spacing: -0.5px;
}

.login-header p {
  color: var(--text-secondary);
  font-size: 14px;
  font-weight: 500;
}

/* 登录方式选择器 */
.login-mode-selector {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-bottom: 32px;
}

.mode-btn {
  padding: 16px 12px;
  border: 2px solid var(--border-color);
  border-radius: 16px;
  background: var(--bg-card);
  cursor: pointer;
  transition: var(--transition);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: var(--text-secondary);
  font-weight: 600;
}

.mode-btn:hover {
  border-color: var(--color-primary-light);
  background: var(--bg-card-hover);
}

.mode-btn.active {
  border-color: var(--color-primary);
  background: linear-gradient(135deg, rgba(108, 99, 255, 0.1), rgba(118, 75, 162, 0.1));
  color: var(--color-primary);
  box-shadow: 0 4px 12px rgba(108, 99, 255, 0.15);
}

.mode-icon {
  font-size: 24px;
}

.mode-label {
  font-size: 12px;
}

/* 表单包装器 */
.form-wrapper {
  margin-bottom: 24px;
  min-height: 200px;
}

.auth-form {
  animation: fadeIn 0.3s ease-in;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

/* 表单输入框 */
.form-input {
  --el-input-border-color: var(--border-color);
  --el-input-border-radius: 12px;
  --el-input-bg-color: var(--bg-input);
  --el-input-text-color: var(--text-primary);
  --el-input-placeholder-color: var(--text-muted);
  --el-input-hover-border-color: var(--color-primary-light);
  --el-input-focus-border-color: var(--color-primary);
}

:deep(.form-input .el-input__wrapper) {
  background: var(--bg-input);
  border-radius: 12px;
  transition: var(--transition);
}

:deep(.form-input .el-input__wrapper:hover) {
  background: var(--bg-card-hover);
}

:deep(.form-input.is-focus .el-input__wrapper) {
  box-shadow: 0 0 0 3px rgba(108, 99, 255, 0.1);
}

.input-icon {
  width: 18px;
  height: 18px;
  color: var(--text-muted);
  margin-right: 8px;
}

/* 验证码输入组 */
.code-input-group {
  display: flex;
  gap: 12px;
}

.code-input-group :deep(.el-input) {
  flex: 1;
}

.send-code-btn {
  width: 100px;
  flex-shrink: 0;
  --el-button-bg-color: var(--color-primary);
  --el-button-border-color: var(--color-primary);
  --el-button-text-color: white;
  border-radius: 12px;
  font-weight: 600;
  font-size: 13px;
  transition: var(--transition);
}

.send-code-btn:hover:not(:disabled) {
  background: var(--color-primary-light);
  border-color: var(--color-primary-light);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(108, 99, 255, 0.3);
}

.send-code-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 提交按钮 */
.submit-btn {
  width: 100%;
  height: 48px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 0.5px;
  background: linear-gradient(135deg, var(--color-primary), #764ba2);
  border: none;
  color: white;
  cursor: pointer;
  transition: var(--transition);
  box-shadow: 0 8px 24px rgba(108, 99, 255, 0.3);
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 12px 32px rgba(108, 99, 255, 0.4);
}

.submit-btn:active:not(:disabled) {
  transform: translateY(0);
}

/* 微信登录 */
.wechat-login {
  text-align: center;
  padding: 40px 20px;
}

.qrcode-placeholder {
  width: 200px;
  height: 200px;
  margin: 0 auto 24px;
  background: var(--bg-input);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px dashed var(--border-color);
  color: var(--text-muted);
}

.qrcode-placeholder svg {
  width: 80px;
  height: 80px;
  opacity: 0.5;
}

.qrcode-text {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.qrcode-hint {
  font-size: 13px;
  color: var(--text-secondary);
}

/* 游客登录 */
.guest-login {
  text-align: center;
  padding: 40px 20px;
}

.guest-icon {
  width: 100px;
  height: 100px;
  margin: 0 auto 24px;
  background: linear-gradient(135deg, rgba(108, 99, 255, 0.1), rgba(118, 75, 162, 0.1));
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-primary);
}

.guest-icon svg {
  width: 50px;
  height: 50px;
}

.guest-text {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.guest-hint {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 24px;
}

/* 底部链接 */
.auth-footer {
  text-align: center;
  display: flex;
  justify-content: center;
  gap: 16px;
  flex-wrap: wrap;
}

.footer-text {
  font-size: 13px;
  color: var(--text-secondary);
}

:deep(.auth-footer .el-link) {
  --el-link-text-color: var(--color-primary);
  --el-link-hover-text-color: var(--color-primary-light);
  font-weight: 600;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .login-container {
    padding: 32px 20px;
    border-radius: 20px;
  }

  .login-header h1 {
    font-size: 28px;
  }

  .login-mode-selector {
    grid-template-columns: repeat(3, 1fr);
    gap: 10px;
  }

  .mode-btn {
    padding: 12px 8px;
    font-size: 11px;
  }

  .mode-icon {
    font-size: 20px;
  }

  .submit-btn {
    height: 44px;
    font-size: 15px;
  }

  .send-code-btn {
    width: 80px;
    font-size: 12px;
  }

  .back-btn {
    top: 16px;
    left: 16px;
    width: 40px;
    height: 40px;
  }

  .back-btn svg {
    width: 18px;
    height: 18px;
  }
}

/* 深色模式适配 */
@media (prefers-color-scheme: dark) {
  .back-btn {
    background: rgba(26, 26, 46, 0.9);
    color: var(--text-primary);
  }

  .back-btn:hover {
    background: rgba(26, 26, 46, 1);
  }
}
</style>
