<template>
  <header class="navbar">
    <div class="navbar-inner">
      <router-link to="/" class="brand">
        <span class="brand-logo">✂️</span>
        <span class="brand-name">去水印</span>
      </router-link>
      <nav class="nav-links">
        <router-link to="/" class="nav-link">
          <el-icon><VideoCamera /></el-icon><span>解析下载</span>
        </router-link>
        <router-link to="/parse-history" class="nav-link">
          <el-icon><Clock /></el-icon><span>解析历史</span>
        </router-link>
        <router-link to="/history" class="nav-link">
          <el-icon><Download /></el-icon><span>下载历史</span>
        </router-link>
      </nav>

      <div class="navbar-right">
        <!-- 用户已登录：显示头像下拉菜单 -->
        <el-dropdown v-if="userStore.isLoggedIn" trigger="click" @command="handleCommand">
          <div class="user-avatar-wrap">
            <img
              :src="userStore.user?.avatar || 'https://via.placeholder.com/32'"
              class="user-avatar"
            />
            <span class="user-name">{{ userStore.user?.nickname || userStore.user?.username || '用户' }}</span>
            <el-icon class="arrow-icon"><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">
                <el-icon><User /></el-icon> 个人资料
              </el-dropdown-item>
              <el-dropdown-item command="logout" divided>
                <el-icon><SwitchButton /></el-icon> 退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>

        <!-- 未登录：显示登录按钮 -->
        <router-link v-else to="/login" class="login-btn">
          <el-icon><User /></el-icon>
          <span>登录</span>
        </router-link>

        <!-- 主题切换 -->
        <button class="theme-toggle" @click="toggleTheme" :title="isDark ? '切换亮色' : '切换暗色'">
          <span>{{ isDark ? '☀️' : '🌙' }}</span>
        </button>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { VideoCamera, Clock, Download, User, ArrowDown, SwitchButton } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const isDark = ref(false)

onMounted(() => {
  isDark.value = document.documentElement.getAttribute('data-theme') === 'dark'
})

function toggleTheme() {
  isDark.value = !isDark.value
  const theme = isDark.value ? 'dark' : 'light'
  document.documentElement.setAttribute('data-theme', theme)
  localStorage.setItem('wm_theme', theme)
}

function handleCommand(command) {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      userStore.logout()
      ElMessage.success('已退出登录')
      router.push('/')
    }).catch(() => {})
  }
}
</script>

<style scoped>
.navbar {
  position: sticky; top: 0; z-index: 100;
  background: var(--gradient-bg);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(255,255,255,0.1);
  box-shadow: 0 2px 20px rgba(0,0,0,0.15);
}
.navbar-inner {
  max-width: 900px; margin: 0 auto; padding: 0 24px;
  height: 64px; display: flex; align-items: center; gap: 16px;
}
.brand { display: flex; align-items: center; gap: 8px; text-decoration: none; flex-shrink: 0; }
.brand-logo { font-size: 22px; }
.brand-name { font-size: 18px; font-weight: 800; color: #fff; letter-spacing: 0.5px; }
.nav-links { display: flex; align-items: center; gap: 4px; flex: 1; justify-content: center; }
.nav-link {
  display: flex; align-items: center; gap: 5px;
  color: rgba(255,255,255,0.75); text-decoration: none;
  padding: 7px 14px; border-radius: var(--border-radius-full);
  font-size: 14px; font-weight: 500; transition: var(--transition);
}
.nav-link:hover { color: #fff; background: rgba(255,255,255,0.15); }
.nav-link.router-link-active { color: #fff; background: rgba(255,255,255,0.22); box-shadow: 0 2px 8px rgba(0,0,0,0.15); }

/* 右侧区域 */
.navbar-right {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

/* 用户头像区域 */
.user-avatar-wrap {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  padding: 4px 10px;
  border-radius: var(--border-radius-full);
  background: rgba(255,255,255,0.15);
  transition: var(--transition);
}
.user-avatar-wrap:hover { background: rgba(255,255,255,0.25); }
.user-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid rgba(255,255,255,0.5);
}
.user-name {
  color: #fff;
  font-size: 13px;
  font-weight: 500;
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.arrow-icon {
  color: rgba(255,255,255,0.7);
  font-size: 12px;
}

/* 登录按钮 */
.login-btn {
  display: flex;
  align-items: center;
  gap: 5px;
  color: rgba(255,255,255,0.85);
  text-decoration: none;
  padding: 7px 14px;
  border-radius: var(--border-radius-full);
  font-size: 14px;
  font-weight: 500;
  background: rgba(255,255,255,0.15);
  transition: var(--transition);
}
.login-btn:hover { color: #fff; background: rgba(255,255,255,0.28); }

/* 主题切换 */
.theme-toggle {
  flex-shrink: 0; width: 38px; height: 38px; border-radius: var(--border-radius-full);
  border: none; background: rgba(255,255,255,0.15); cursor: pointer;
  display: flex; align-items: center; justify-content: center; transition: var(--transition); font-size: 16px;
}
.theme-toggle:hover { background: rgba(255,255,255,0.28); transform: scale(1.08); }
</style>
