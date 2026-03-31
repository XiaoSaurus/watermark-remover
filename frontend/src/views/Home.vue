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
      <div class="hero">
        <h1>短视频去水印下载</h1>
        <p>支持抖音 · 快手 · B站 · 微博 · 小红书，粘贴分享链接即可下载无水印视频</p>
      </div>

      <div class="input-card">
        <el-input
          v-model="inputUrl" type="textarea" :rows="3"
          placeholder="粘贴视频分享链接或分享文案，例如：&#10;https://v.douyin.com/xxxxx/&#10;3.14 复制打开抖音，看看【xxx】的作品 https://v.douyin.com/xxxxx/"
          resize="none"
        />
        <div class="input-actions">
          <el-button @click="inputUrl = ''" :disabled="!inputUrl" plain>清空</el-button>
          <el-button type="primary" size="large" :loading="loading"
            :disabled="!inputUrl.trim()" @click="handleParse">
            {{ loading ? '解析中...' : '🚀 立即解析' }}
          </el-button>
        </div>
      </div>

      <div class="platforms">
        <span class="platform-tag" v-for="p in platforms" :key="p.name">{{ p.icon }} {{ p.name }}</span>
      </div>

      <transition name="fade">
        <div v-if="result" class="result-card">
          <div class="result-header">
            <img :src="result.cover" class="cover" @error="coverError" />
            <div class="result-meta">
              <div class="platform-badge">{{ platformLabel }}</div>
              <h2 class="video-title">{{ result.title || '无标题' }}</h2>
              <p class="video-author">👤 {{ result.author }}</p>
            </div>
          </div>

          <el-divider />

          <!-- 下载进度条 -->
          <div v-if="downloadStatus" class="progress-wrap">
            <div class="progress-header">
              <span class="progress-label">
                {{ downloadStatus === 'saving' ? '⏳ 保存中...' : `⬇️ 下载中` }}
              </span>
              <span class="progress-pct">{{ downloadStatus === 'saving' ? '100%' : downloadProgress + '%' }}</span>
            </div>
            <el-progress
              :percentage="downloadStatus === 'saving' ? 100 : downloadProgress"
              :striped="downloadStatus === 'downloading'"
              :striped-flow="downloadStatus === 'downloading'"
              :duration="10"
              :stroke-width="10"
              status="success"
            />
          </div>

          <div class="download-section">
            <h3>选择清晰度下载</h3>
            <div class="download-list">
              <div v-for="(v, i) in result.videoUrls" :key="i" class="download-item">
                <span class="quality-label">{{ v.quality }}</span>
                <div class="download-btns">
                  <el-button type="primary"
                    :loading="downloadingIndex === i"
                    :disabled="downloadingIndex !== -1 && downloadingIndex !== i"
                    @click="downloadVideo(v.url, v.quality, i)">
                    {{ downloadingIndex === i ? downloadProgress + '%' : '⬇️ 下载视频' }}
                  </el-button>
                  <el-button @click="copyUrl(v.url)" plain>📋 复制链接</el-button>
                </div>
              </div>
            </div>
            <div v-if="result.audioUrl" class="download-item">
              <span class="quality-label">🎵 音频</span>
              <div class="download-btns">
                <el-button @click="downloadVideo(result.audioUrl, '音频', -1)" plain>⬇️ 下载音频</el-button>
              </div>
            </div>
          </div>

          <el-alert v-if="result.platform === 'bilibili'" type="warning" :closable="false" style="margin-top:12px">
            B站视频音视频分离，需分别下载后用工具合并（如 FFmpeg）
          </el-alert>
        </div>
      </transition>

      <div class="guide-card">
        <h3>📖 使用说明</h3>
        <ol>
          <li>打开抖音/快手/B站等 App，找到想下载的视频</li>
          <li>点击「分享」→「复制链接」或「复制文案」</li>
          <li>粘贴到上方输入框，点击「立即解析」</li>
          <li>选择清晰度，点击「下载视频」即可</li>
        </ol>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { videoApi } from '@/api/watermark'
import { addHistory } from '@/store/history'

const inputUrl = ref('')
const loading = ref(false)
const result = ref(null)
const downloadingIndex = ref(-1)
const downloadProgress = ref(0)
const downloadStatus = ref('') // 'downloading' | 'saving' | ''

const platforms = [
  { name: '抖音', icon: '🎵' }, { name: '快手', icon: '⚡' },
  { name: 'B站', icon: '📺' }, { name: '微博', icon: '🌐' }, { name: '小红书', icon: '📕' }
]

const platformLabel = computed(() => {
  const map = { douyin: '🎵 抖音', kuaishou: '⚡ 快手', bilibili: '📺 B站', weibo: '🌐 微博', xiaohongshu: '📕 小红书' }
  return map[result.value?.platform] || result.value?.platform
})

async function handleParse() {
  if (!inputUrl.value.trim()) return
  loading.value = true
  result.value = null
  try {
    const res = await videoApi.parse(inputUrl.value.trim())
    if (res.code === 200) {
      result.value = res.data
      ElMessage.success('解析成功！')
    } else {
      ElMessage.error(res.message || '解析失败')
    }
  } catch {
    ElMessage.error('请求失败，请检查后端服务是否启动')
  } finally {
    loading.value = false
  }
}

function downloadVideo(url, quality, idx) {
  if (downloadingIndex.value !== -1) return
  const filename = `video_${quality}_${Date.now()}.mp4`
  const proxyUrl = `/api/video/download?url=${encodeURIComponent(url)}&filename=${encodeURIComponent(filename)}`

  downloadingIndex.value = idx
  downloadProgress.value = 0
  downloadStatus.value = 'downloading'

  // 用 XMLHttpRequest 实现进度监听
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
      // 触发浏览器下载
      const blob = xhr.response
      const a = document.createElement('a')
      a.href = URL.createObjectURL(blob)
      a.download = filename
      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a)
      URL.revokeObjectURL(a.href)
      ElMessage.success('下载完成！')
      // 写入历史记录
      addHistory({
        title: result.value.title,
        platform: result.value.platform,
        cover: result.value.cover,
        quality,
        url
      })
    } else {
      ElMessage.error('下载失败: HTTP ' + xhr.status)
    }
    resetDownload()
  }

  xhr.onerror = () => {
    ElMessage.error('下载失败，请检查网络')
    resetDownload()
  }

  xhr.send()
}

function resetDownload() {
  setTimeout(() => {
    downloadingIndex.value = -1
    downloadProgress.value = 0
    downloadStatus.value = ''
  }, 1000)
}

function copyUrl(url) {
  navigator.clipboard.writeText(url).then(() => ElMessage.success('链接已复制到剪贴板'))
}

function coverError(e) {
  e.target.src = 'https://via.placeholder.com/120x90?text=No+Cover'
}
</script>

<style scoped>
.app { min-height: 100vh; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.navbar { display: flex; align-items: center; justify-content: space-between; padding: 0 32px; height: 60px; background: rgba(255,255,255,0.1); backdrop-filter: blur(10px); }
.brand { display: flex; align-items: center; gap: 8px; color: #fff; font-size: 20px; font-weight: bold; }
.nav-link { color: rgba(255,255,255,0.85); text-decoration: none; margin-left: 24px; font-size: 15px; }
.nav-link:hover, .nav-link.router-link-active { color: #fff; }
.main { max-width: 760px; margin: 0 auto; padding: 40px 16px 60px; }
.hero { text-align: center; color: #fff; margin-bottom: 32px; }
.hero h1 { font-size: 36px; font-weight: 800; margin-bottom: 10px; }
.hero p { font-size: 16px; opacity: 0.85; }
.input-card { background: #fff; border-radius: 16px; padding: 24px; box-shadow: 0 20px 60px rgba(0,0,0,0.15); }
.input-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 14px; }
.platforms { display: flex; flex-wrap: wrap; gap: 10px; justify-content: center; margin: 20px 0; }
.platform-tag { background: rgba(255,255,255,0.2); color: #fff; padding: 6px 16px; border-radius: 20px; font-size: 14px; }
.result-card { background: #fff; border-radius: 16px; padding: 24px; box-shadow: 0 20px 60px rgba(0,0,0,0.15); margin-top: 20px; }
.result-header { display: flex; gap: 16px; align-items: flex-start; }
.cover { width: 120px; height: 90px; object-fit: cover; border-radius: 8px; flex-shrink: 0; }
.result-meta { flex: 1; }
.platform-badge { display: inline-block; background: #f0f0ff; color: #5b5bd6; padding: 2px 10px; border-radius: 10px; font-size: 13px; margin-bottom: 8px; }
.video-title { font-size: 18px; font-weight: bold; margin-bottom: 6px; line-height: 1.4; }
.video-author { color: #888; font-size: 14px; }
.progress-wrap { margin-bottom: 20px; padding: 16px 20px; background: #f8f9ff; border-radius: 12px; }
.progress-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.progress-label { font-size: 14px; color: #667eea; font-weight: 500; }
.progress-pct { font-size: 14px; color: #667eea; font-weight: 700; }
.download-section h3 { font-size: 16px; margin-bottom: 14px; color: #333; }
.download-list { display: flex; flex-direction: column; gap: 12px; }
.download-item { display: flex; align-items: center; justify-content: space-between; padding: 12px 16px; background: #f8f9ff; border-radius: 10px; }
.quality-label { font-weight: 600; color: #333; }
.download-btns { display: flex; gap: 8px; }
.guide-card { background: rgba(255,255,255,0.15); border-radius: 16px; padding: 24px; margin-top: 24px; color: #fff; }
.guide-card h3 { margin-bottom: 12px; font-size: 16px; }
.guide-card ol { padding-left: 20px; line-height: 2; opacity: 0.9; }
.fade-enter-active, .fade-leave-active { transition: all 0.3s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; transform: translateY(10px); }
</style>
