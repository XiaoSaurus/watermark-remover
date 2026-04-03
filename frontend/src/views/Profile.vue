<template>
  <div class="profile-page">
    <div class="profile-container">
      <div class="profile-header">
        <div class="avatar-wrapper" @click="showAvatarSelector = true">
          <img :src="profile.avatar || 'https://api.dicebear.com/7.x/bottts/png?seed=default'" class="avatar" />
          <div class="avatar-overlay">
            <span>更换头像</span>
          </div>
        </div>
        <div class="user-info">
          <h2>{{ profile.nickname || profile.username }}</h2>
          <p class="login-type">
            <el-tag size="small" :type="loginTypeTag">{{ loginTypeLabel }}</el-tag>
          </p>
          <p class="phone" v-if="profile.phone">{{ profile.phoneMasked }}</p>
          
          <!-- 用户等级和积分 -->
          <div class="level-points">
            <div class="level-badge" :class="'level-' + profile.level">
              <el-icon class="level-icon"><Trophy /></el-icon>
              <span class="level-text">Lv.{{ profile.level }} {{ profile.levelName }}</span>
            </div>
            <div class="points-info">
              <span class="points-value">{{ profile.points || 0 }}</span>
              <span class="points-label">积分</span>
              <span class="next-level" v-if="profile.level < 10">
                (距下一级还需 {{ (profile.nextLevelPoints || 0) - (profile.points || 0) }} 积分)
              </span>
            </div>
          </div>
          
          <!-- VIP状态 -->
          <div class="vip-status" v-if="profile.vipStatus === 'active'">
            <el-tag type="warning">
              <el-icon><Star /></el-icon>
              VIP会员 · {{ formatDate(profile.vipExpireAt) }} 到期
            </el-tag>
          </div>
        </div>
        <el-button type="primary" @click="enterEditMode">编辑资料</el-button>
      </div>

      <el-divider />

      <!-- 个人简介 -->
      <div class="profile-section" v-if="profile.bio">
        <h3>个人简介</h3>
        <p class="bio-text">{{ profile.bio }}</p>
      </div>

      <div class="profile-section">
        <h3>账号信息</h3>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="用户名">{{ profile.username }}</el-descriptions-item>
          <el-descriptions-item label="昵称">{{ profile.nickname || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="手机号">
            {{ profile.phoneMasked || '未绑定' }}
            <el-button 
              v-if="!profile.phone && (profile.loginType === 'tourist' || profile.loginType === 'wechat')"
              type="primary" 
              link 
              @click="showBindPhone = true"
            >立即绑定</el-button>
          </el-descriptions-item>
          <el-descriptions-item label="性别">{{ profile.genderLabel || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="生日">{{ profile.birthday || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="登录方式">{{ loginTypeLabel }}</el-descriptions-item>
          <el-descriptions-item label="注册时间">{{ formatDate(profile.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="登录次数">{{ profile.loginCount || 0 }} 次</el-descriptions-item>
        </el-descriptions>
      </div>

      <div class="profile-section">
        <h3>安全设置</h3>
        <el-space wrap>
          <el-button @click="showChangePassword = true">修改密码</el-button>
          <el-button 
            v-if="!profile.phone && (profile.loginType === 'tourist' || profile.loginType === 'wechat')"
            type="primary"
            @click="showBindPhone = true"
          >绑定手机号</el-button>
        </el-space>
      </div>

      <div class="profile-section">
        <el-button type="danger" plain @click="handleLogout">退出登录</el-button>
      </div>
    </div>

    <!-- 编辑资料对话框 -->
    <el-dialog v-model="isEditMode" title="编辑资料" width="500px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="昵称">
          <el-input v-model="editForm.nickname" placeholder="请输入昵称" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="个人简介">
          <el-input 
            v-model="editForm.bio" 
            type="textarea" 
            :rows="3" 
            placeholder="介绍一下自己吧"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="editForm.gender">
            <el-radio :value="0">保密</el-radio>
            <el-radio :value="1">男</el-radio>
            <el-radio :value="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="生日">
          <el-date-picker 
            v-model="editForm.birthday" 
            type="date" 
            placeholder="选择生日"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="isEditMode = false">取消</el-button>
        <el-button type="primary" @click="saveProfile" :loading="saving">保存</el-button>
      </template>
    </el-dialog>

    <!-- 头像选择对话框 -->
    <el-dialog v-model="showAvatarSelector" title="选择头像" width="600px">
      <el-tabs v-model="avatarTab">
        <el-tab-pane label="随机头像" name="random">
          <div class="avatar-grid">
            <div
              v-for="avatar in avatarList"
              :key="avatar.id"
              class="avatar-item"
              :class="{ active: selectedAvatar?.id === avatar.id }"
              @click="selectedAvatar = avatar"
            >
              <img :src="avatar.url" alt="avatar" />
            </div>
          </div>
          <div class="load-more">
            <el-button @click="loadAvatars" :loading="loadingAvatars">换一批</el-button>
          </div>
        </el-tab-pane>
        <el-tab-pane label="本地上传" name="upload">
          <el-upload
            class="avatar-uploader"
            :show-file-list="false"
            :before-upload="beforeAvatarUpload"
            :http-request="handleAvatarUpload"
          >
            <img v-if="previewAvatar" :src="previewAvatar" class="preview-avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="upload-tips">
            <p>支持 JPG、PNG、GIF 格式</p>
            <p>文件大小不超过 2MB</p>
          </div>
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <el-button @click="showAvatarSelector = false">取消</el-button>
        <el-button type="primary" @click="confirmAvatarChange" :loading="savingAvatar">确定</el-button>
      </template>
    </el-dialog>

    <!-- 修改密码对话框 -->
    <el-dialog v-model="showChangePassword" title="修改密码" width="400px">
      <el-form :model="passwordForm" label-width="80px">
        <el-form-item label="旧密码">
          <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入旧密码" />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="passwordForm.newPassword" type="password" placeholder="至少6位密码" />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="再次输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showChangePassword = false">取消</el-button>
        <el-button type="primary" @click="handleChangePassword" :loading="changingPassword">确定</el-button>
      </template>
    </el-dialog>

    <!-- 绑定手机号对话框 -->
    <el-dialog v-model="showBindPhone" title="绑定手机号" width="400px">
      <el-form :model="bindPhoneForm" label-width="80px">
        <el-form-item label="手机号">
          <el-input v-model="bindPhoneForm.phone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="验证码">
          <el-input v-model="bindPhoneForm.code" placeholder="请输入验证码" maxlength="6">
            <template #append>
              <el-button 
                @click="sendBindCode" 
                :disabled="countdown > 0"
                :loading="sendingCode"
              >{{ countdown > 0 ? countdown + 's' : '获取验证码' }}</el-button>
            </template>
          </el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showBindPhone = false">取消</el-button>
        <el-button type="primary" @click="handleBindPhone" :loading="bindingPhone">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Star, Trophy } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { avatarApi } from '@/api/auth'
import axios from 'axios'

const router = useRouter()
const userStore = useUserStore()

// 使用全局 axios 实例，复用 main.js 中的拦截器
const http = axios.create({ baseURL: '/api', timeout: 30000 })

// 全局 axios 拦截器会自动注入 token
// 手动设置 Authorization 是为了兼容 Profile 页面的特殊需求
// 实际上全局拦截器已经处理了，这里可以简化
// 但保留手动设置以防止 edge case

const profile = ref({})
const loadingAvatars = ref(false)
const saving = ref(false)
const avatarList = ref([])
const isEditMode = ref(false)
const showAvatarSelector = ref(false)
const showChangePassword = ref(false)
const showBindPhone = ref(false)
const editForm = ref({})
const passwordForm = ref({})
const bindPhoneForm = ref({})
const avatarTab = ref('random')
const selectedAvatar = ref(null)
const previewAvatar = ref('')
const savingAvatar = ref(false)
const changingPassword = ref(false)
const bindingPhone = ref(false)
const countdown = ref(0)
const sendingCode = ref(false)

const loginTypeLabel = computed(() => {
  const map = { phone: '手机号登录', wechat: '微信登录', tourist: '游客登录' }
  return map[profile.value.loginType] || profile.value.loginType
})

const loginTypeTag = computed(() => {
  const map = { phone: 'success', wechat: 'warning', tourist: 'info' }
  return map[profile.value.loginType] || 'info'
})

onMounted(() => {
  loadProfile()
  loadAvatars()
})

async function loadProfile() {
  try {
    http.defaults.headers.common['Authorization'] = 'Bearer ' + userStore.token
    const res = await http.get('/auth/profile')
    profile.value = res.data.data
  } catch (e) {
    console.error('加载用户资料失败', e)
    profile.value = userStore.user || {}
  }
}

async function loadAvatars() {
  loadingAvatars.value = true
  try {
    const res = await avatarApi.getAvatarList('momo')
    avatarList.value = res.data || []
  } catch (e) {
    console.error('加载头像失败', e)
  } finally {
    loadingAvatars.value = false
  }
}

function enterEditMode() {
  editForm.value = {
    nickname: profile.value.nickname || '',
    bio: profile.value.bio || '',
    gender: profile.value.gender || 0,
    birthday: profile.value.birthday || ''
  }
  isEditMode.value = true
}

async function saveProfile() {
  saving.value = true
  try {
    http.defaults.headers.common['Authorization'] = 'Bearer ' + userStore.token
    await http.put('/auth/profile', editForm.value)
    profile.value = { ...profile.value, ...editForm.value }
    
    // 更新全局用户状态
    const user = userStore.user || {}
    userStore.setUser({ ...user, ...editForm.value })
    
    ElMessage.success('保存成功')
    isEditMode.value = false
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

async function handleChangePassword() {
  if (!passwordForm.value.oldPassword) {
    ElMessage.error('请输入旧密码')
    return
  }
  if (!passwordForm.value.newPassword) {
    ElMessage.error('请输入新密码')
    return
  }
  if (passwordForm.value.newPassword.length < 6) {
    ElMessage.error('密码长度至少6位')
    return
  }
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    ElMessage.error('两次密码不一致')
    return
  }

  changingPassword.value = true
  try {
    http.defaults.headers.common['Authorization'] = 'Bearer ' + userStore.token
    await http.put('/auth/password', passwordForm.value)
    ElMessage.success('密码修改成功，请重新登录')
    showChangePassword.value = false
    handleLogout()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '修改失败')
  } finally {
    changingPassword.value = false
  }
}

async function sendBindCode() {
  if (!bindPhoneForm.value.phone || !/^1[3-9]\d{9}$/.test(bindPhoneForm.value.phone)) {
    ElMessage.error('请输入正确的手机号')
    return
  }

  sendingCode.value = true
  try {
    await http.post('/auth/sms/send', { phone: bindPhoneForm.value.phone, scene: 'bind' })
    ElMessage.success('验证码已发送')
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) clearInterval(timer)
    }, 1000)
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '发送失败')
  } finally {
    sendingCode.value = false
  }
}

async function handleBindPhone() {
  if (!bindPhoneForm.value.phone || !/^1[3-9]\d{9}$/.test(bindPhoneForm.value.phone)) {
    ElMessage.error('请输入正确的手机号')
    return
  }
  if (!bindPhoneForm.value.code) {
    ElMessage.error('请输入验证码')
    return
  }

  bindingPhone.value = true
  try {
    http.defaults.headers.common['Authorization'] = 'Bearer ' + userStore.token
    await http.post('/auth/bind-phone', bindPhoneForm.value)
    ElMessage.success('绑定成功')
    showBindPhone.value = false
    loadProfile()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '绑定失败')
  } finally {
    bindingPhone.value = false
  }
}

function beforeAvatarUpload(file) {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB')
    return false
  }

  const reader = new FileReader()
  reader.onload = (e) => {
    previewAvatar.value = e.target.result
  }
  reader.readAsDataURL(file)

  return false
}

async function handleAvatarUpload({ file }) {
  savingAvatar.value = true
  try {
    const res = await avatarApi.uploadAvatar(file)
    profile.value.avatar = res.data.url
    const user = userStore.user || {}
    userStore.setUser({ ...user, avatar: res.data.url })
    ElMessage.success('头像上传成功')
    showAvatarSelector.value = false
  } catch (e) {
    ElMessage.error(e.message || '上传失败')
  } finally {
    savingAvatar.value = false
  }
}

async function confirmAvatarChange() {
  if (avatarTab.value === 'random' && selectedAvatar.value) {
    savingAvatar.value = true
    try {
      http.defaults.headers.common['Authorization'] = 'Bearer ' + userStore.token
      await http.put('/auth/avatar', { avatarId: selectedAvatar.value.id })
      profile.value.avatar = selectedAvatar.value.url
      const user = userStore.user || {}
      userStore.setUser({ ...user, avatar: selectedAvatar.value.url })
      ElMessage.success('头像更换成功')
      showAvatarSelector.value = false
    } catch (e) {
      ElMessage.error(e.response?.data?.message || '更换失败')
    } finally {
      savingAvatar.value = false
    }
  } else if (avatarTab.value === 'upload' && previewAvatar.value) {
    // 上传由 el-upload 处理
  } else {
    ElMessage.warning('请先选择头像')
  }
}

function handleLogout() {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  }).catch(() => {})
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return dateStr.substring(0, 10)
}
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  background: var(--bg-page);
  padding: 40px 20px;
}

.profile-container {
  max-width: 800px;
  margin: 0 auto;
  background: var(--bg-card);
  border-radius: var(--border-radius-lg);
  padding: 40px;
  box-shadow: var(--shadow-card);
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 30px;
}

.avatar-wrapper {
  position: relative;
  cursor: pointer;
  border-radius: 50%;
  overflow: hidden;
}

.avatar {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  object-fit: cover;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}

.avatar-overlay span {
  color: #fff;
  font-size: 14px;
}

.user-info {
  flex: 1;
}

.user-info h2 {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.login-type {
  margin-bottom: 8px;
}

.phone {
  color: var(--text-secondary);
  font-size: 14px;
}

.level-points {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-top: 12px;
}

.level-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}

.level-badge.level-1 { background: #e0e0e0; color: #424242; }
.level-badge.level-2 { background: #e3f2fd; color: #1565c0; }
.level-badge.level-3 { background: #e8f5e9; color: #2e7d32; }
.level-badge.level-4 { background: #fff3e0; color: #e65100; }
.level-badge.level-5 { background: #fce4ec; color: #c2185b; }
.level-badge.level-6, .level-badge.level-7, .level-badge.level-8,
.level-badge.level-9, .level-badge.level-10 {
  background: #f59e0b;
  color: #fff;
}

.level-icon {
  font-size: 14px;
  margin-right: 2px;
}

.points-info {
  font-size: 14px;
  color: var(--text-secondary);
}

.points-value {
  font-weight: 700;
  color: var(--color-primary);
  font-size: 16px;
}

.points-label {
  margin-left: 4px;
}

.next-level {
  color: var(--text-tertiary);
  font-size: 12px;
}

.vip-status {
  margin-top: 8px;
}

.bio-text {
  color: var(--text-secondary);
  line-height: 1.6;
  padding: 10px 0;
}

.profile-section {
  margin-bottom: 30px;
}

.profile-section h3 {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 15px;
}

.avatar-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 10px;
  max-height: 300px;
  overflow-y: auto;
}

.avatar-item {
  width: 70px;
  height: 70px;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
  border: 3px solid transparent;
  transition: all 0.3s;
}

.avatar-item:hover {
  border-color: var(--color-primary-light);
}

.avatar-item.active {
  border-color: var(--color-primary);
}

.avatar-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.load-more {
  text-align: center;
  margin-top: 15px;
}

.avatar-uploader {
  display: flex;
  justify-content: center;
}

.avatar-uploader-icon {
  font-size: 40px;
  color: #8c939d;
  width: 150px;
  height: 150px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px dashed #d9d9d9;
  border-radius: 50%;
}

.preview-avatar {
  width: 150px;
  height: 150px;
  border-radius: 50%;
  object-fit: cover;
}

.upload-tips {
  text-align: center;
  color: var(--text-secondary);
  font-size: 12px;
  margin-top: 10px;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .profile-page {
    padding: 20px 12px;
  }

  .profile-container {
    padding: 20px 16px;
    border-radius: var(--border-radius-md);
  }

  .profile-header {
    flex-direction: column;
    align-items: center;
    text-align: center;
    gap: 16px;
  }

  .avatar-wrapper {
    order: -1;
  }

  .avatar {
    width: 100px;
    height: 100px;
  }

  .user-info {
    width: 100%;
  }

  .user-info h2 {
    font-size: 20px;
  }

  .level-points {
    flex-direction: column;
    gap: 10px;
    align-items: center;
  }

  .level-badge {
    font-size: 11px;
    padding: 3px 10px;
  }

  .points-info {
    text-align: center;
  }

  .profile-section {
    margin-bottom: 24px;
  }

  .profile-section h3 {
    font-size: 15px;
    margin-bottom: 12px;
  }

  :deep(.el-descriptions) {
    --el-descriptions-table-border: 1px solid var(--border-color);
  }

  :deep(.el-descriptions__label) {
    font-size: 13px;
  }

  :deep(.el-descriptions__content) {
    font-size: 13px;
  }

  /* 对话框适配 */
  :deep(.el-dialog) {
    width: 90% !important;
    margin: 10vh auto !important;
  }

  .avatar-grid {
    grid-template-columns: repeat(4, 1fr);
    gap: 8px;
  }

  .avatar-item {
    width: 60px;
    height: 60px;
  }

  .avatar-uploader-icon,
  .preview-avatar {
    width: 120px;
    height: 120px;
  }
}
</style>
