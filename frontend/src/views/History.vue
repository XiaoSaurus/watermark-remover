<template>
  <div class="app">
    <header class="navbar">
      <div class="brand">
        <span class="brand-icon">✂️</span>
        <span class="brand-name">去水印工具</span>
      </div>
      <nav>
        <router-link to="/" class="nav-link">解析下载</router-link>
        <router-link to="/history" class="nav-link">历史记录</router-link>
      </nav>
    </header>

    <main class="main">
      <div class="page-header">
        <h2>📋 下载历史</h2>
        <el-button v-if="list.length" type="danger" plain size="small" @click="handleClear">清空记录</el-button>
      </div>

      <el-empty v-if="!list.length" description="暂无下载记录，去解析一个视频吧～" style="background:#fff;border-radius:16px;padding:40px" />

      <div v-else class="history-list">
        <div v-for="(item, i) in list" :key="item.time" class="history-item">
          <img :src="item.cover" class="item-cover" @error="coverError" />
          <div class="item-info">
            <div class="item-title">{{ item.title || '无标题' }}</div>
            <div class="item-meta">
              <span class="item-platform">{{ platformMap[item.platform] || item.platform }}</span>
              <span class="item-quality">{{ item.quality }}</span>
            </div>
            <div class="item-time">{{ formatTime(item.time) }}</div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getHistory, clearHistory } from '@/store/history'

const list = ref([])
const platformMap = {
  douyin: '🎵 抖音', kuaishou: '⚡ 快手',
  bilibili: '📺 B站', weibo: '🌐 微博', xiaohongshu: '📕 小红书'
}

onMounted(() => { list.value = getHistory() })

function formatTime(ts) {
  const d = new Date(ts)
  const pad = n => String(n).padStart(2, '0')
  return `${d.getMonth()+1}/${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

async function handleClear() {
  await ElMessageBox.confirm('确定要清空所有历史记录吗？', '确认清空', { type: 'warning' })
  clearHistory()
  list.value = []
  ElMessage.success('已清空')
}

function coverError(e) {
  e.target.src = 'https://via.placeholder.com/80x60?text=No+Cover'
}
</script>

<style scoped>
.app { min-height: 100vh; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.navbar { display: flex; align-items: center; justify-content: space-between; padding: 0 32px; height: 60px; background: rgba(255,255,255,0.1); backdrop-filter: blur(10px); }
.brand { display: flex; align-items: center; gap: 8px; color: #fff; font-size: 20px; font-weight: bold; }
.nav-link { color: rgba(255,255,255,0.85); text-decoration: none; margin-left: 24px; font-size: 15px; }
.nav-link:hover, .nav-link.router-link-active { color: #fff; }
.main { max-width: 760px; margin: 0 auto; padding: 40px 16px 60px; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
.page-header h2 { color: #fff; font-size: 24px; font-weight: 700; margin: 0; }
.history-list { display: flex; flex-direction: column; gap: 12px; }
.history-item { display: flex; gap: 16px; align-items: center; background: #fff; border-radius: 14px; padding: 16px 20px; box-shadow: 0 4px 20px rgba(0,0,0,0.08); }
.item-cover { width: 80px; height: 60px; object-fit: cover; border-radius: 8px; flex-shrink: 0; background: #f0f0f0; }
.item-info { flex: 1; overflow: hidden; }
.item-title { font-size: 15px; font-weight: 600; color: #222; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; margin-bottom: 6px; }
.item-meta { display: flex; gap: 8px; margin-bottom: 4px; }
.item-platform { font-size: 12px; color: #667eea; background: #f0f0ff; padding: 2px 8px; border-radius: 10px; }
.item-quality { font-size: 12px; color: #888; background: #f5f5f5; padding: 2px 8px; border-radius: 10px; }
.item-time { font-size: 12px; color: #bbb; }
</style>
