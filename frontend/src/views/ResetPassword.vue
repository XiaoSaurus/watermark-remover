<template>
  <div class="reset-page">
    <!-- 返回按钮 -->
    <button class="back-btn" @click="router.back()" title="返回">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M19 12H5M12 19l-7-7 7-7"/>
      </svg>
    </button>
    
    <div class="reset-container">
      <div class="reset-header">
        <span class="logo">✂️</span>
        <h1>重置密码</h1>
        <p>找回您的账号</p>
      </div>

      <el-form ref="resetForm" :model="formData" :rules="rules" @submit.prevent="handleReset">
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="formData.phone" placeholder="请输入手机号" maxlength="11" clearable />
        </el-form-item>

        <el-form-item label="验证码" prop="code">
          <div class="code-input-group">
            <el-input v-model="formData.code" placeholder="请输入6位验证码" maxlength="6" clearable />
            <el-button 
              @click="sendSms" 
              :disabled="smsLoading || smsCooldown > 0"
              :loading="smsLoading"
            >
              {{ smsCooldown > 0 ? `${smsCooldown}s` : '发送验证码' }}
            </el-button>
          </div>
        </el-form-item>

        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="formData.newPassword" type="password" placeholder="请输入新密码（至少6位）" show-password />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="formData.confirmPassword" type="password" placeholder="请再次输入密码" show-password />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleReset" :loading="loading" class="btn-full">
            确认重置
          </el-button>
        </el-form-item>

        <div class="bottom-links">
          <el-link type="primary" @click="goLogin">想起密码了？去登录</el-link>
        </div>
      </el-form>
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
  newPassword: '',
  confirmPassword: ''
})

const resetForm = ref(null)

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
  } else if (value !== formData.value.newPassword) {
    callback(new Error('两次密码输入不一致'))
  } else {
    callback()
  }
}

const rules = {
  phone: [{ validator: phoneValidator, trigger: 'blur' }],
  code: [{ validator: codeValidator, trigger: 'blur' }],
  newPassword: [{ validator: passwordValidator, trigger: 'blur' }],
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
    const res = await authApi.sendSms(formData.value.phone, 'reset')
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

// 重置密码
async function handleReset() {
  try {
    await resetForm.value.validate()
  } catch {
    return
  }

  loading.value = true
  try {
    await userStore.resetPassword(
      formData.value.phone,
      formData.value.code,
      formData.value.newPassword,
      formData.value.confirmPassword
    )
    ElMessage.success('密码重置成功，请重新登录')
    router.push('/login')
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
</script>

<style scoped>
.reset-page {
  min-height: 100vh;
  background: var(--gradient-bg);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.reset-container {
  background: var(--bg-card);
  border-radius: var(--border-radius-lg);
  box-shadow: var(--shadow-lg);
  width: 100%;
  max-width: 420px;
  padding: 40px;
}

.reset-header {
  text-align: center;
  margin-bottom: 30px;
}

.logo {
  font-size: 48px;
  display: block;
  margin-bottom: 10px;
}

.reset-header h1 {
  font-size: 28px;
  font-weight: 800;
  color: var(--text-primary);
  margin-bottom: 5px;
}

.reset-header p {
  color: var(--text-secondary);
  font-size: 14px;
}

.code-input-group {
  display: flex;
  gap: 10px;
}

.code-input-group :deep(.el-input) {
  flex: 1;
}

.code-input-group :deep(.el-button) {
  width: 120px;
  flex-shrink: 0;
}

.btn-full {
  width: 100%;
}

.bottom-links {
  text-align: center;
  margin-top: 20px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
}

/* 返回按钮 */
.back-btn {
  position: fixed;
  top: 20px;
  left: 20px;
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: white;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
  z-index: 100;
}

.back-btn:hover {
  background: #f5f5f5;
  transform: translateX(-2px);
}

.back-btn svg {
  width: 20px;
  height: 20px;
  color: #333;
}
</style>
