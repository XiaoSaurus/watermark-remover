<template>
  <div class="app">
    <header class="navbar">
      <div class="brand"><span class="brand-icon">✂️</span><span class="brand-name">去水印工具</span></div>
      <nav>
        <router-link to="/" class="nav-link">解析下载</router-link>
        <router-link to="/parse-history" class="nav-link">解析历史</router-link>
        <router-link to="/history" class="nav-link">下载历史</router-link>
      </nav>
    </header>

    <main class="main">
      <div class="page-header">
        <h2>🔍 解析历史</h2>
        <el-button v-if="list.length" type="danger" plain size="small" @click="handleClear">清空</el-button>
      </div>

      <div v-if="pageLoading" style="text-align:center;padding:60px">
        <el-icon class="is-loading" size="32"><Loading /></el-icon>
      </div>
      <el-empty v-else-if="!list.length" description="暂无解析记录"
        style="background:#fff;border-radius:16px;padding:40px" />

      <div v-else class="parse-list">
        <div v-for="item in list" :key="item.id" class="parse-item card">
          <div class="item-top">
            <img :src="item.cover" class="item-cover" @error="coverError" />
            <div class="item-info">
              <div class="item-title">{{ item.title || '无标题' }}</div>
              <div class="item-meta">
                <span class="tag-platform">{{ platformMap[item.platform] || item.platform }}</span>
                <span class="item-author" v-if="item.author">👤 {{ item.author }}</span>
              </div>
              <div class="item-time">{{ formatTime(item.createdAt) }}</div>
            </div>
            <el-button size="small" plain @click="handleDelete(item.id)" class="btn-del">🗑</el-button>
          </div>

          <!-- 下载按钮区 -->
          <div class="download-area" v-if="item.videoUrls && item.videoUrls.length">
            <div v-for="(v,vi) in item.videoUrls" :key="vi" class="dl-row">
              <span class="quality-tag">{{ v.quality }}</span>
              <div class="dl-btns">
                <el-button type="primary" size="small"
                  :loading="downloadingKey===`${item.id}-${vi}`"
                  :disabled="downloadingKey!==null && downloadingKey!==`${item.id}-${vi}`"
                  @click="downloadVideo(item, v.url, v.quality, vi)">
                  {{ downloadingKey===`${item.id}-${vi}` ? downloadProgress+'%' : '⬇️ 下载' }}
                </el-button>
                <el-button size="small" plain @click="copyUrl(v.url)">📋</el-button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-if="total > pageSize" class="pagination">
        <el-pagination v-model:current-page="currentPage" :page-size="pageSize"
          :total="total" layout="prev,pager,next" @current-change="loadList" />
      </div>

      <!-- 进度条 -->
      <transition name="slide-up">
        <div v-if="downloadStatus" class="float-progress">
          <div class="float-progress-inner">
            <span>{{ downloadStatus==='saving'?'⏳ 保存中...':`⬇️ 下载中 ${downloadProgress}%` }}</span>
            <el-progress :percentage="downloadStatus==='saving'?100:downloadProgress"
              :striped="downloadStatus==='downloading'" :striped-flow="downloadStatus==='downloading'"
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
import { Loading } from '@element-plus/icons-vue'
import { parseHistoryApi } from '@/api/watermark'
import { saveDownload, clearParseHistory, removeParseHistory } from '@/store/history'

const list = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = 20
const pageLoading = ref(false)
const downloadingKey = ref(null)
const downloadProgress = ref(0)
const downloadStatus = ref('')

const platformMap = {
  douyin:'🎵 抖音',kuaishou:'⚡ 快手',bilibili:'📺 B站',weibo:'🌐 微博',xiaohongshu:'📕 小红书'
}

onMounted(loadList)

async function loadList() {
  pageLoading.value = true
  try {
    const res = await parseHistoryApi.list(currentPage.value-1, pageSize)
    list.value = res.data.content
    total.value = res.data.totalElements
  } catch { ElMessage.error('加载失败') }
  finally { pageLoading.value = false }
}

function formatTime(ts) {
  if (!ts) return ''
  const d = new Date(ts)
  const pad = n => String(n).padStart(2,'0')
  return `${d.getMonth()+1}/${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

async function handleClear() {
  await ElMessageBox.confirm('确定清空所有解析历史？','确认',{type:'warning'})
  await clearParseHistory()
  list.value = []; total.value = 0
  ElMessage.success('已清空')
}

async function handleDelete(id) {
  await removeParseHistory(id)
  list.value = list.value.filter(i => i.id !== id)
  total.value--
}

function downloadVideo(item, url, quality, vi) {
  if (downloadingKey.value !== null) return
  const key = `${item.id}-${vi}`
  const filename = `video_${quality}_${Date.now()}.mp4`
  const proxyUrl = `/api/video/download?url=${encodeURIComponent(url)}&filename=${encodeURIComponent(filename)}`
  const base = { platform:item.platform, title:item.title, cover:item.cover, quality, videoUrl:url }

  downloadingKey.value = key; downloadProgress.value = 0; downloadStatus.value = 'downloading'

  const xhr = new XMLHttpRequest()
  xhr.open('GET', proxyUrl, true); xhr.responseType = 'blob'
  xhr.onprogress = e => { if (e.lengthComputable) downloadProgress.value = Math.round(e.loaded/e.total*100) }
  xhr.onload = async () => {
    if (xhr.status === 200) {
      downloadStatus.value = 'saving'; downloadProgress.value = 100
      const a = document.createElement('a')
      a.href = URL.createObjectURL(xhr.response); a.download = filename
      document.body.appendChild(a); a.click(); document.body.removeChild(a); URL.revokeObjectURL(a.href)
      ElMessage.success('下载完成！')
      await saveDownload({ ...base, status:'success' })
    } else {
      ElMessage.error('下载失败: HTTP '+xhr.status)
      await saveDownload({ ...base, status:'fail', errMsg:'HTTP '+xhr.status })
    }
    resetDownload()
  }
  xhr.onerror = async () => {
    ElMessage.error('下载失败')
    await saveDownload({ ...base, status:'fail', errMsg:'网络错误' })
    resetDownload()
  }
  xhr.send()
}

function resetDownload() {
  setTimeout(() => { downloadingKey.value=null; downloadProgress.value=0; downloadStatus.value='' }, 1000)
}
function copyUrl(url) {
  navigator.clipboard.writeText(url).then(() => ElMessage.success('已复制'))
}
function coverError(e) { e.target.src='https://via.placeholder.com/80x60?text=No+Cover' }
</script>

<style scoped>
.app{min-height:100vh;background:linear-gradient(135deg,#667eea 0%,#764ba2 100%)}
.navbar{display:flex;align-items:center;justify-content:space-between;padding:0 32px;height:60px;background:rgba(255,255,255,0.1);backdrop-filter:blur(10px)}
.brand{display:flex;align-items:center;gap:8px;color:#fff;font-size:20px;font-weight:bold}
.nav-link{color:rgba(255,255,255,0.85);text-decoration:none;margin-left:24px;font-size:15px}
.nav-link:hover,.nav-link.router-link-active{color:#fff}
.main{max-width:800px;margin:0 auto;padding:40px 16px 100px}
.page-header{display:flex;align-items:center;justify-content:space-between;margin-bottom:20px}
.page-header h2{color:#fff;font-size:24px;font-weight:700;margin:0}
.parse-list{display:flex;flex-direction:column;gap:14px}
.parse-item{padding:20px 24px}
.item-top{display:flex;gap:14px;align-items:flex-start;margin-bottom:14px}
.item-cover{width:90px;height:68px;object-fit:cover;border-radius:8px;flex-shrink:0;background:#f0f0f0}
.item-info{flex:1;overflow:hidden}
.item-title{font-size:14px;font-weight:600;color:#222;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;margin-bottom:6px}
.item-meta{display:flex;gap:8px;align-items:center;margin-bottom:4px}
.tag-platform{font-size:11px;color:#667eea;background:#f0f0ff;padding:2px 8px;border-radius:10px}
.item-author{font-size:12px;color:#888}
.item-time{font-size:11px;color:#bbb}
.btn-del{flex-shrink:0}
.download-area{border-top:1px solid #f0f0f0;padding-top:12px;display:flex;flex-direction:column;gap:8px}
.dl-row{display:flex;align-items:center;justify-content:space-between;padding:8px 12px;background:#f8f9ff;border-radius:8px}
.quality-tag{font-size:13px;font-weight:600;color:#333}
.dl-btns{display:flex;gap:6px}
.pagination{display:flex;justify-content:center;margin-top:24px}
.float-progress{position:fixed;bottom:0;left:0;right:0;background:#fff;padding:12px 24px;box-shadow:0 -4px 20px rgba(0,0,0,.1);z-index:100}
.float-progress-inner{display:flex;align-items:center;max-width:800px;margin:0 auto;gap:12px;font-size:14px;color:#667eea;font-weight:500}
.slide-up-enter-active,.slide-up-leave-active{transition:transform .3s ease}
.slide-up-enter-from,.slide-up-leave-to{transform:translateY(100%)}
</style>
