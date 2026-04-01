<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-header">
        <span class="logo">✂️</span>
        <h1>去水印工具</h1>
        <p>登录或注册账号</p>
      </div>

      <el-tabs v-model="activeTab" class="login-tabs">
        <!-- 登录标签 -->
        <el-tab-pane label="登录" name="login">
          <el-form ref="loginForm" :model="loginData" :rules="loginRules" @submit.prevent="handleLogin">
            <!-- 登录方式 -->
            <el-form-item label="登录方式">
              <el-radio-group v-model="loginType">
                <el-radio label="phone">手机号验证码</el-radio>
                <el-radio label="wechat_web">微信扫码</el-radio>
                <el-radio label="tourist">游客登录</el-radio>
              </el-radio-group>
            </el-form-item>

            <template v-if="loginType === 'phone'">
              <el-form-item label="手机号" prop="phone">
                <el-input v-model="loginData.phone" placeholder="请输入手机号" maxlength="11" clearable />
              </el-form-item>
              <el-form-item label="验证码" prop="code">
                <div class="code-input-group">
                  <el-input v-model="loginData.code" placeholder="请输入6位验证码" maxlength="6" clearable />
                  <el-button 
                    @click="sendSms('login')" 
                    :disabled="smsLoading || smsCooldown > 0"
                    :loading="smsLoading"
                  >
                    {{ smsCooldown > 0 ? `${smsCooldown}s` : '发送验证码' }}
                  </el-button>
                </div>
              </el-form-item>
            </template>

            <template v-if="loginType === 'wechat_web'">
              <div class="wechat-qrcode">
                <p>扫描二维码登录</p>
                <div id="wechat-qrcode" style="width: 200px; height: 200px; margin: 20px auto;"></div>
              </div>
            </template>

            <el-form-item>
              <el-button type="primary" @click="handleLogin" :loading="loading" class="btn-full">
                {{ loginType === 'tourist' ? '游客登录' : '登录' }}
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 注册标签 -->
        <el-tab-pane label="注册" name="register">
          <el-form ref="registerForm" :model="registerData" :rules="registerRules" @submit.prevent="handleRegister">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="registerData.phone" placeholder="请输入手机号" maxlength="11" clearable />
            </el-form-item>
            <el-form-item label="验证码" prop="code">
              <div class="code-input-group">
                <el-input v-model="registerData.code" placeholder="请输入6位验证码" maxlength="6" clearable />
                <el-button 
                  @click="sendSms('register')" 
                  :disabled="smsLoading || smsCooldown > 0"
                  :loading="smsLoading"
                >
                  {{ smsCooldown > 0 ? `${smsCooldown}s` : '发送验证码' }}
                </el-button>
              </div>
            </el-form-item>
            <el-form-item label="用户名（可选）" prop="username">
              <el-input v-model="registerData.username" placeholder="不填将自动生成" maxlength="20" clearable />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input v-model="registerData.password" type="password" placeholder="请输入密码（至少6位）" show-password />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="registerData.confirmPassword" type="password" placeholder="请再次输入密码" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleRegister" :loading="loading" class="btn-full">
                注册
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 忘记密码标签 -->
        <el-tab-pane label="忘记密码" name="reset">
          <el-form ref="resetForm" :model="resetData" :rules="resetRules" @submit.prevent="handleReset">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="resetData.phone" placeholder="请输入手机号" maxlength="11" clearable />
            </el-form-item>
            <el-form-item label="验证码" prop="code">
              <div class="code-input-group">
                <el-input v-model="resetData.code" placeholder="请输入6位验证码" maxlength="6" clearable />
                <el-button 
                  @click="sendSms('reset')" 
                  :disabled="smsLoading || smsCooldown > 0"
                  :loading="smsLoading"
                >
                  {{ smsCooldown > 0 ? `${smsCooldown}s` : '发送验证码' }}
                </el-button>
              </div>
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="resetData.newPassword" type="password" placeholder="请输入新密码（至少6位）" show-password />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="resetData.confirmPassword" type="password" placeholder="请再次输入密码" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleReset" :loading="loading" class="btn-full">
                重置密码
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { authApi } from '@/api/auth'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('login')
const loginType = ref('phone')
const loading = ref(false)
const smsLoading = ref(false)
const smsCooldown = ref(0)

const loginData = ref({ phone: '', code: '' })
const registerData = ref({ phone: '', code: '', username: '', password: '', confirmPassword: '' })
const resetData = ref({ phone: '', code: '', newPassword: '', confirmPassword: '' })

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
const confirmPwdValidator = (pwdField) => (rule, value, callback) => {
  const pwd = pwdField === 'register' ? registerData.value.password : resetData.value.newPassword
  if (!value) {
    callback(new Error('请再次输入密码'))
  } else if (value !== pwd) {
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

const loginRules = {
  phone: [{ validator: phoneValidator, trigger: 'blur' }],
  code: [{ validator: codeValidator, trigger: 'blur' }]
}

const registerRules = {
  phone: [{ validator: phoneValidator, trigger: 'blur' }],
  code: [{ validator: codeValidator, trigger: 'blur' }],
  username: [{ validator: usernameValidator, trigger: 'blur' }],
  password: [{ validator: passwordValidator, trigger: 'blur' }],
  confirmPassword: [{ validator: confirmPwdValidator('register'), trigger: 'blur' }]
}

const resetRules = {
  phone: [{ validator: phoneValidator, trigger: 'blur' }],
  code: [{ validator: codeValidator, trigger: 'blur' }],
  newPassword: [{ validator: passwordValidator, trigger: 'blur' }],
  confirmPassword: [{ validator: confirmPwdValidator('reset'), trigger: 'blur' }]
}

const loginForm = ref(null)
const registerForm = ref(null)
const resetForm = ref(null)

// 获取当前表单的手机号
const getCurrentPhone = (scene) => {
  if (scene === 'login') return loginData.value.phone
  if (scene === 'register') return registerData.value.phone
  if (scene === 'reset') return resetData.value.phone
  return ''
}

// 发送短信验证码
async function sendSms(scene) {
  const phone = getCurrentPhone(scene)
  
  // 先验证手机号
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
      // 开始60秒倒计时
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
    // 解析剩余秒数
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
      await userStore.login('tourist', {})
      ElMessage.success('游客登录成功')
      router.push('/')
    } catch (e) {
      ElMessage.error(e.message)
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
      registerData.value.phone,
      registerData.value.code,
      registerData.value.password,
      registerData.value.confirmPassword,
      registerData.value.username
    )
    ElMessage.success('注册成功')
    router.push('/')
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    loading.value = false
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
      resetData.value.phone,
      resetData.value.code,
      resetData.value.newPassword,
      resetData.value.confirmPassword
    )
    ElMessage.success('密码重置成功，请重新登录')
    activeTab.value = 'login'
    resetData.value = { phone: '', code: '', newPassword: '', confirmPassword: '' }
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    loading.value = false
  }
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
}

.login-container {
  background: var(--bg-card);
  border-radius: var(--border-radius-lg);
  box-shadow: var(--shadow-lg);
  width: 100%;
  max-width: 420px;
  padding: 40px;
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.logo {
  font-size: 48px;
  display: block;
  margin-bottom: 10px;
}

.login-header h1 {
  font-size: 28px;
  font-weight: 800;
  color: var(--text-primary);
  margin-bottom: 5px;
}

.login-header p {
  color: var(--text-secondary);
  font-size: 14px;
}

.login-tabs :deep(.el-tabs__header) {
  margin-bottom: 20px;
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

.wechat-qrcode {
  text-align: center;
  padding: 20px 0;
}

/* 表单标签样式 */
:deep(.el-form-item__label) {
  font-weight: 500;
}
</style>
