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
        <h2>📥 下载历史</h2>
        <el-button v-if="list.length" type="danger" plain size="small" @click="handleClear">清空</el-button>
      </div>

      <div v-if="pageLoading" style="text-align:center;padding:60px">
        <el-icon class="is-loading" size="32"><Loading /></el-icon>
      </div>
      <el-empty v-else-if="!list.length" description="暂无下载记录"
        style="background:#fff;border-radius:16px;padding:40px" />

      <div v-else class="history-list">
        <div v-for="item in list" :key="item.id" class="history-item">
          <img :src="item.cover" class="item-cover" @error="coverError" />
          <div class="item-info">
            <div class="item-title">{{ item.title || '无标题' }}</div>
            <div class="item-meta">
              <span class="tag-platform">{{ platformMap[item.platform] || item.platform }}</span>
              <span class="tag-quality">{{ item.quality }}</span>
              <span class="tag-status tag-success" v-if="item.status==='success'">✅ 下载成功</span>
              <span class="tag-status tag-fail" v-else-if="item.status==='fail'" :title="item.errMsg">❌ 下载失败</span>
            </div>
            <div class="item-bottom">
              <span class="item-time">{{ formatTime(item.createdAt) }}</span>
              <span class="item-errmsg" v-if="item.status==='fail' && item.errMsg">{{ item.errMsg }}</span>
            </div>
          </div>
          <el-button size="small" plain @click="handleDelete(item.id)">🗑</el-button>
        </div>
      </div>

      <div v-if="total > pageSize" class="pagination">
        <el-pagination v-model:current-page="currentPage" :page-size="pageSize"
          :total="total" layout="prev,pager,next" @current-change="loadList" />
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { historyApi } from '@/api/watermark'
import { clearDownloadHistory, removeDownloadHistory } from '@/store/history'

const list = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = 20
const pageLoading = ref(false)

const platformMap = {
  douyin:'🎵 抖音',kuaishou:'⚡ 快手',bilibili:'📺 B站',weibo:'🌐 微博',xiaohongshu:'📕 小红书'
}

onMounted(loadList)

async function loadList() {
  pageLoading.value = true
  try {
    const res = await historyApi.list(currentPage.value-1, pageSize)
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
  await ElMessageBox.confirm('确定清空所有下载历史？','确认',{type:'warning'})
  await clearDownloadHistory()
  list.value = []; total.value = 0
  ElMessage.success('已清空')
}

async function handleDelete(id) {
  await removeDownloadHistory(id)
  list.value = list.value.filter(i => i.id !== id)
  total.value--
}

function coverError(e) { e.target.src='https://via.placeholder.com/80x60?text=No+Cover' }
</script>

<style scoped>
.app{min-height:100vh;background:linear-gradient(135deg,#667eea 0%,#764ba2 100%)}
.navbar{display:flex;align-items:center;justify-content:space-between;padding:0 32px;height:60px;background:rgba(255,255,255,0.1);backdrop-filter:blur(10px)}
.brand{display:flex;align-items:center;gap:8px;color:#fff;font-size:20px;font-weight:bold}
.nav-link{color:rgba(255,255,255,0.85);text-decoration:none;margin-left:24px;font-size:15px}
.nav-link:hover,.nav-link.router-link-active{color:#fff}
.main{max-width:760px;margin:0 auto;padding:40px 16px 60px}
.page-header{display:flex;align-items:center;justify-content:space-between;margin-bottom:20px}
.page-header h2{color:#fff;font-size:24px;font-weight:700;margin:0}
.history-list{display:flex;flex-direction:column;gap:12px}
.history-item{display:flex;gap:14px;align-items:center;background:#fff;border-radius:14px;padding:16px 20px;box-shadow:0 4px 20px rgba(0,0,0,.08)}
.item-cover{width:80px;height:60px;object-fit:cover;border-radius:8px;flex-shrink:0;background:#f0f0f0}
.item-info{flex:1;overflow:hidden;min-width:0}
.item-title{font-size:14px;font-weight:600;color:#222;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;margin-bottom:6px}
.item-meta{display:flex;flex-wrap:wrap;gap:6px;margin-bottom:4px;align-items:center}
.tag-platform{font-size:11px;color:#667eea;background:#f0f0ff;padding:2px 8px;border-radius:10px}
.tag-quality{font-size:11px;color:#888;background:#f5f5f5;padding:2px 8px;border-radius:10px}
.tag-status{font-size:11px;padding:2px 8px;border-radius:10px;font-weight:500}
.tag-success{color:#18a058;background:#f0faf0}
.tag-fail{color:#d03050;background:#fff0f0;cursor:help}
.item-bottom{display:flex;align-items:center;gap:10px}
.item-time{font-size:11px;color:#bbb}
.item-errmsg{font-size:11px;color:#d03050}
.pagination{display:flex;justify-content:center;margin-top:24px}
</style>
