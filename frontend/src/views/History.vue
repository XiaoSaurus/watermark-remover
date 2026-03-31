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

      <el-empty v-if="!list.length"
        description="暂无下载记录，去解析一个视频吧～"
        style="background:#fff;border-radius:16px;padding:40px" />

      <div v-else class="history-list">
        <div v-for="(item, i) in list" :key="item.time" class="history-item">
          <!-- 封面 -->
          <img :src="item.cover" class="item-cover" @error="coverError" />

          <!-- 信息 -->
          <div class="item-info">
            <div class="item-title">{{ item.title || '无标题' }}</div>
            <div class="item-meta">
              <span class="tag-platform">{{ platformMap[item.platform] || item.platform }}</span>
              <span class="tag-quality">{{ item.quality }}</span>
              <!-- 状态标签 -->
              <span class="tag-status tag-success" v-if="item.status === 'success'">✅ 下载成功</span>
              <span class="tag-status tag-fail" v-else-if="item.status === 'fail'" :title="item.errMsg">❌ 下载失败</span>
              <span class="tag-status tag-unknown" v-else>⏺ 未知</span>
            </div>
            <div class="item-bottom">
              <span class="item-time">{{ formatTime(item.time) }}</span>
              <!-- 失败原因 -->
              <span class="item-errmsg" v-if="item.status === 'fail' && item.errMsg">{{ item.errMsg }}</span>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="item-actions">
            <el-button
              type="primary" size="small"
              :loading="downloadingKey === item.time"
              :disabled="downloadingKey !== null && downloadingKey !== item.time"
              @click="reDownload(item)"
            >
              {{ downloadingKey === item.time ? downloadProgress + '%' : '⬇️ 再次下载' }}
            </el-button>
          </div>
        </div>
      </div>

      <!-- 再次下载进度条（悬浮在底部） -->
      <transition name="slide-up">
        <div v-if="downloadStatus" class="float-progress">
          <div class="float-progress-inner">
            <span>{{ downloadStatus === 'saving' ? '⏳ 保存中...' : `⬇️ 下载中 ${downloadProgress}%` }}</span>
            <el-progress
              :percentage="downloadStatus === 'saving' ? 100 : downloadProgress"
              :striped="downloadStatus === 'downloading'"
              :striped-flow="downloadStatus === 'downloading'"
              :duration="10" :stroke-width="8" style="flex:1;margin-left:12px" />
          </div>
        </div>
      </transition>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getHistory, addHistory, clearHistory } from '@/store/history'

const list = ref([])
const downloadingKey = ref(null)   // 当前下载的 item.time
const downloadProgress = ref(0)
const downloadStatus = ref('')

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

function reDownload(item) {
  if (!item.url) {
    ElMessage.warning('该记录缺少视频地址，无法再次下载')
    return
  }
  if (downloadingKey.value !== null) return

  const filename = `video_${item.quality}_${Date.now()}.mp4`
  const proxyUrl = `/api/video/download?url=${encodeURIComponent(item.url)}&filename=${encodeURIComponent(filename)}`

  downloadingKey.value = item.time
  downloadProgress.value = 0
  downloadStatus.value = 'downloading'

  const xhr = new XMLHttpRequest()
  xhr.open('GET', proxyUrl, true)
  xhr.responseType = 'blob'

  xhr.onprogress = (e) => {
    if (e.lengthComputable) {
      downloadProgress.value = Math.round((e.loaded / e.total) * 100)
    }
  }

  xhr.onload = () => {
    if (xhr.status === 200) {
      downloadStatus.value = 'saving'
      downloadProgress.value = 100
      const a = document.createElement('a')
      a.href = URL.createObjectURL(xhr.response)
      a.download = filename
      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a)
      URL.revokeObjectURL(a.href)
      ElMessage.success('下载完成！')
      // 更新该条记录状态为成功
      updateItemStatus(item.time, 'success', '')
    } else {
      ElMessage.error('下载失败: HTTP ' + xhr.status)
      updateItemStatus(item.time, 'fail', 'HTTP ' + xhr.status)
    }
    resetDownload()
  }

  xhr.onerror = () => {
    ElMessage.error('下载失败，请检查网络')
    updateItemStatus(item.time, 'fail', '网络错误')
    resetDownload()
  }

  xhr.send()
}

function updateItemStatus(time, status, errMsg) {
  // 更新列表显示
  const idx = list.value.findIndex(i => i.time === time)
  if (idx !== -1) {
    list.value[idx] = { ...list.value[idx], status, errMsg }
    list.value = [...list.value]
  }
  // 同步到 localStorage
  const all = getHistory()
  const hi = all.findIndex(i => i.time === time)
  if (hi !== -1) {
    all[hi] = { ...all[hi], status, errMsg }
    localStorage.setItem('wm_download_history', JSON.stringify(all))
  }
}

function resetDownload() {
  setTimeout(() => {
    downloadingKey.value = null
    downloadProgress.value = 0
    downloadStatus.value = ''
  }, 1000)
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
.main { max-width: 760px; margin: 0 auto; padding: 40px 16px 100px; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
.page-header h2 { color: #fff; font-size: 24px; font-weight: 700; margin: 0; }
.history-list { display: flex; flex-direction: column; gap: 12px; }
.history-item { display: flex; gap: 14px; align-items: center; background: #fff; border-radius: 14px; padding: 16px 20px; box-shadow: 0 4px 20px rgba(0,0,0,0.08); }
.item-cover { width: 80px; height: 60px; object-fit: cover; border-radius: 8px; flex-shrink: 0; background: #f0f0f0; }
.item-info { flex: 1; overflow: hidden; min-width: 0; }
.item-title { font-size: 14px; font-weight: 600; color: #222; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; margin-bottom: 6px; }
.item-meta { display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 4px; align-items: center; }
.tag-platform { font-size: 11px; color: #667eea; background: #f0f0ff; padding: 2px 8px; border-radius: 10px; }
.tag-quality { font-size: 11px; color: #888; background: #f5f5f5; padding: 2px 8px; border-radius: 10px; }
.tag-status { font-size: 11px; padding: 2px 8px; border-radius: 10px; font-weight: 500; }
.tag-success { color: #18a058; background: #f0faf0; }
.tag-fail { color: #d03050; background: #fff0f0; cursor: help; }
.tag-unknown { color: #888; background: #f5f5f5; }
.item-bottom { display: flex; align-items: center; gap: 10px; }
.item-time { font-size: 11px; color: #bbb; }
.item-errmsg { font-size: 11px; color: #d03050; }
.item-actions { flex-shrink: 0; }

/* 悬浮进度条 */
.float-progress { position: fixed; bottom: 0; left: 0; right: 0; background: #fff; padding: 12px 24px; box-shadow: 0 -4px 20px rgba(0,0,0,0.1); z-index: 100; }
.float-progress-inner { display: flex; align-items: center; max-width: 760px; margin: 0 auto; gap: 12px; font-size: 14px; color: #667eea; font-weight: 500; }
.slide-up-enter-active, .slide-up-leave-active { transition: transform 0.3s ease; }
.slide-up-enter-from, .slide-up-leave-to { transform: translateY(100%); }
</style>
