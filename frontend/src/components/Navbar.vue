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
      <button class="theme-toggle" @click="toggleTheme" :title="isDark?'切换亮色':'切换暗色'">
        <span>{{ isDark ? '☀️' : '🌙' }}</span>
      </button>
    </div>
  </header>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { VideoCamera, Clock, Download } from '@element-plus/icons-vue'
const isDark = ref(false)
onMounted(() => { isDark.value = document.documentElement.getAttribute('data-theme') === 'dark' })
function toggleTheme() {
  isDark.value = !isDark.value
  const theme = isDark.value ? 'dark' : 'light'
  document.documentElement.setAttribute('data-theme', theme)
  localStorage.setItem('wm_theme', theme)
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
.theme-toggle {
  flex-shrink: 0; width: 38px; height: 38px; border-radius: var(--border-radius-full);
  border: none; background: rgba(255,255,255,0.15); cursor: pointer;
  display: flex; align-items: center; justify-content: center; transition: var(--transition); font-size: 16px;
}
.theme-toggle:hover { background: rgba(255,255,255,0.28); transform: scale(1.08); }
</style>
