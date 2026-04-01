<template>
  <div class="register-page">
    <!-- 返回按钮 -->
    <button class="back-btn" @click="router.back()" title="返回">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M19 12H5M12 19l-7-7 7-7"/>
      </svg>
    </button>

    <div class="register-wrapper">
      <div class="register-container">
        <!-- 头部 -->
        <div class="register-header">
          <div class="logo-wrapper">
            <span class="logo">✂️</span>
          </div>
          <h1>创建账号</h1>
          <p>开始使用去水印工具</p>
        </div>

        <!-- 注册表单 -->
        <el-form ref="registerForm" :model="formData" :rules="rules" class="auth-form">
          <!-- 手机号 -->
          <el-form-item prop="phone">
            <el-input 
              v-model="formData.phone" 
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

          <!-- 验证码 -->
          <el-form-item prop="code">
            <div class="code-input-group">
              <el-input 
                v-model="formData.code" 
                placeholder="验证码"
                maxlength="6"
                clearable
                class="form-input"
              />
              <el-button 
                @click="sendSms" 
                :disabled="smsLoading || smsCooldown > 0"
                :loading="smsLoading"
                class="send-code-btn"
              >
                {{ smsCooldown > 0 ? `${smsCooldown}s` : '发送' }}
              </el-button>
            </div>
          </el-form-item>

          <!-- 用户名 -->
          <el-form-item prop="username">
            <el-input 
              v-model="formData.username" 
              placeholder="用户名（可选，不填自动生成）"
              maxlength="20"
              clearable
              class="form-input"
            >
              <template #prefix>
                <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2M12 3a4 4 0 1 0 0 8 4 4 0 0 0 0-8z"/>
                </svg>
              </template>
            </el-input>
          </el-form-item>

          <!-- 密码 -->
          <el-form-item prop="password">
            <el-input 
              v-model="formData.password" 
              type="password" 
              placeholder="密码（至少6位）"
              show-password
              class="form-input"
            >
              <template #prefix>
                <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                  <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
                </svg>
              </template>
            </el-input>
          </el-form-item>

          <!-- 确认密码 -->
          <el-form-item prop="confirmPassword">
            <el-input 
              v-model="formData.confirmPassword" 
              type="password" 
              placeholder="确认密码"
              show-password
              class="form-input"
            >
              <template #prefix>
                <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                  <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
                </svg>
              </template>
            </el-input>
          </el-form-item>

          <!-- 提交按钮 -->
          <el-button 
            type="primary" 
            @click="handleRegister" 
            :loading="loading" 
            class="submit-btn"
          >
            创建账号
          </el-button>
        </el-form>

        <!-- 底部链接 -->
        <div class="auth-footer">
          <span class="footer-text">
            已有账号？
            <el-link type="primary" @click="goLogin">立即登录</el-link>
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

const loading = ref(false)
const smsLoading = ref(false)
const smsCooldown = ref(0)

const formData = ref({
  phone: '',
  code: '',
  username: '',
  password: '',
  confirmPassword: ''
})

const registerForm = ref(null)

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

// 密码验证规则
const passwordValidator = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入密码'))
  } else if (value.length < 6) {
    callback(new Error('密码至少6位'))
  } else {
    callback()
  }
}

// 确认密码验证
const confirmPwdValidator = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请再次输入密码'))
  } else if (value !== formData.value.password) {
    callback(new Error('两次密码输入不一致'))
  } else {
    callback()
  }
}

// 用户名验证（可选）
const usernameValidator = (rule, value, callback) => {
  if (!value) {
    callback() // 可选字段，不填通过
  } else if (value.length < 3 || value.length > 20) {
    callback(new Error('用户名长度需在3-20位之间'))
  } else if (!/^[a-zA-Z0-9_\u4e00-\u9fa5]+$/.test(value)) {
    callback(new Error('用户名只能包含字母、数字、下划线和中文'))
  } else {
    callback()
  }
}

const rules = {
  phone: [{ validator: phoneValidator, trigger: 'blur' }],
  code: [{ validator: codeValidator, trigger: 'blur' }],
  username: [{ validator: usernameValidator, trigger: 'blur' }],
  password: [{ validator: passwordValidator, trigger: 'blur' }],
  confirmPassword: [{ validator: confirmPwdValidator, trigger: 'blur' }]
}

// 发送短信验证码
async function sendSms() {
  if (!formData.value.phone) {
    ElMessage.error('请先输入手机号')
    return
  }
  if (!/^1[3-9]\d{9}$/.test(formData.value.phone)) {
    ElMessage.error('手机号格式不正确')
    return
  }

  smsLoading.value = true
  try {
    const res = await authApi.sendSms(formData.value.phone, 'register')
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

// 注册
async function handleRegister() {
  try {
    await registerForm.value.validate()
  } catch {
    return
  }

  loading.value = true
  try {
    await userStore.register(
      formData.value.phone,
      formData.value.code,
      formData.value.password,
      formData.value.confirmPassword,
      formData.value.username
    )
    ElMessage.success('注册成功')
    router.push('/')
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    loading.value = false
  }
}

// 返回登录
function goLogin() {
  router.push('/login')
}

// 返回上一页
function goBack() {
  router.back()
}
</script>

<style scoped>
.register-page {
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

/* 注册包装器 */
.register-wrapper {
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

/* 注册容器 */
.register-container {
  background: var(--bg-card);
  border-radius: 24px;
  box-shadow: 0 20px 60px rgba(108, 99, 255, 0.2);
  padding: 48px 32px;
  backdrop-filter: blur(10px);
}

/* 头部 */
.register-header {
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

.register-header h1 {
  font-size: 32px;
  font-weight: 800;
  color: var(--text-primary);
  margin-bottom: 8px;
  letter-spacing: -0.5px;
}

.register-header p {
  color: var(--text-secondary);
  font-size: 14px;
  font-weight: 500;
}

/* 表单 */
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
  margin-top: 8px;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 12px 32px rgba(108, 99, 255, 0.4);
}

.submit-btn:active:not(:disabled) {
  transform: translateY(0);
}

/* 底部链接 */
.auth-footer {
  text-align: center;
  margin-top: 24px;
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
  .register-container {
    padding: 32px 20px;
    border-radius: 20px;
  }

  .register-header h1 {
    font-size: 28px;
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
