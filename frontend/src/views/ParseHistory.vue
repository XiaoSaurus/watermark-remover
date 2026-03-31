<template>
  <div class="page">
    <Navbar />
    <main class="main">
      <div class="page-header">
        <div class="page-title">
          <el-icon><Clock /></el-icon><h2>解析历史</h2>
          <span class="count-badge" v-if="total">{{ total }}</span>
        </div>
        <el-button v-if="list.length" type="danger" plain round size="small" @click="handleClear">
          <el-icon><Delete /></el-icon> 清空
        </el-button>
      </div>
      <div v-if="pageLoading" class="loading-wrap">
        <el-icon class="is-loading" size="36" color="var(--color-primary)"><Loading /></el-icon>
      </div>
      <el-empty v-else-if="!list.length" description="暂无解析记录，去解析一个视频吧～" class="empty-card card" />
      <div v-else class="parse-list">
        <div v-for="item in list" :key="item.id" class="parse-item card">
          <div class="item-top">
            <div class="cover-wrap">
              <img :src="item.cover" class="item-cover" @error="coverError" />
              <span class="platform-badge">{{ platformMap[item.platform] || item.platform }}</span>
            </div>
            <div class="item-info">
              <div class="item-title">{{ item.title || '无标题' }}</div>
              <div class="item-meta">
                <span class="meta-item" v-if="item.author"><el-icon><User /></el-icon>{{ item.author }}</span>
                <span class="meta-item"><el-icon><Clock /></el-icon>{{ formatTime(item.createdAt) }}</span>
              </div>
            </div>
            <el-button size="small" circle plain @click="handleDelete(item.id)"><el-icon><Delete /></el-icon></el-button>
          </div>
          <div class="download-area" v-if="item.videoUrls?.length">
            <div class="download-area-title"><el-icon><Download /></el-icon> 选择清晰度下载</div>
            <div v-for="(v,vi) in item.videoUrls" :key="vi" class="dl-row">
              <div class="dl-quality"><span class="quality-dot"></span><span>{{ v.quality }}</span></div>
              <div class="dl-actions">
                <el-button type="primary" round size="small"
                  :loading="downloadingKey===`${item.id}-${vi}`"
                  :disabled="downloadingKey!==null && downloadingKey!==`${item.id}-${vi}`"
                  @click="downloadVideo(item, v.url, v.quality, vi)" class="btn-dl">
                  <el-icon v-if="downloadingKey!==`${item.id}-${vi}`"><Download /></el-icon>
                  {{ downloadingKey===`${item.id}-${vi}` ? downloadProgress+'%' : '下载' }}
                </el-button>
                <el-button round size="small" plain @click="copyUrl(v.url)"><el-icon><CopyDocument /></el-icon></el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div v-if="total > pageSize" class="pagination">
        <el-pagination v-model:current-page="currentPage" :page-size="pageSize"
          :total="total" layout="prev,pager,next" @current-change="loadList" background />
      </div>
      <transition name="slide-up">
        <div v-if="downloadStatus" class="float-bar">
          <div class="float-bar-inner">
            <el-icon class="is-loading" v-if="downloadStatus==='downloading'"><Loading /></el-icon>
            <el-icon v-else><Timer /></el-icon>
            <span>{{ downloadStatus==='saving'?'保存中...':`下载中 ${downloadProgress}%` }}</span>
            <el-progress :percentage="downloadStatus==='saving'?100:downloadProgress"
              :striped="downloadStatus==='downloading'" :striped-flow="downloadStatus==='downloading'"
              :duration="8" :stroke-width="6" :color="progressColors" style="flex:1;min-width:120px" />
          </div>
        </div>
      </transition>
    </main>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Clock, Delete, Download, User, CopyDocument, Loading, Timer } from '@element-plus/icons-vue'
import Navbar from '@/components/Navbar.vue'
import { parseHistoryApi } from '@/api/watermark'
import { saveDownload, clearParseHistory, removeParseHistory } from '@/store/history'
const list = ref([]), total = ref(0), currentPage = ref(1), pageSize = 20, pageLoading = ref(false)
const downloadingKey = ref(null), downloadProgress = ref(0), downloadStatus = ref('')
const platformMap = {douyin:'🎵 抖音',kuaishou:'⚡ 快手',bilibili:'📺 B站',weibo:'🌐 微博',xiaohongshu:'📕 小红书'}
const progressColors = [{color:'#6c63ff',percentage:50},{color:'#764ba2',percentage:80},{color:'#18a058',percentage:100}]
onMounted(loadList)
async function loadList() {
  pageLoading.value = true
  try { const res = await parseHistoryApi.list(currentPage.value-1,pageSize); list.value=res.data.content; total.value=res.data.totalElements }
  catch { ElMessage.error('加载失败') } finally { pageLoading.value=false }
}
function formatTime(ts) {
  if(!ts) return ''; const d=new Date(ts),pad=n=>String(n).padStart(2,'0')
  return `${d.getMonth()+1}/${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}
async function handleClear() {
  await ElMessageBox.confirm('确定清空所有解析历史？','确认清空',{type:'warning',confirmButtonText:'清空',cancelButtonText:'取消'})
  await clearParseHistory(); list.value=[]; total.value=0; ElMessage({message:'已清空',type:'success',plain:true})
}
async function handleDelete(id) {
  await removeParseHistory(id); list.value=list.value.filter(i=>i.id!==id); total.value--
  ElMessage({message:'已删除',type:'success',plain:true})
}
function downloadVideo(item, url, quality, vi) {
  if(downloadingKey.value!==null) return
  const key=`${item.id}-${vi}`, filename=`video_${quality}_${Date.now()}.mp4`
  const proxyUrl=`/api/video/download?url=${encodeURIComponent(url)}&filename=${encodeURIComponent(filename)}`
  const base={platform:item.platform,title:item.title,cover:item.cover,quality,videoUrl:url}
  downloadingKey.value=key; downloadProgress.value=0; downloadStatus.value='downloading'
  const xhr=new XMLHttpRequest(); xhr.open('GET',proxyUrl,true); xhr.responseType='blob'
  xhr.onprogress=e=>{if(e.lengthComputable)downloadProgress.value=Math.round(e.loaded/e.total*100)}
  xhr.onload=async()=>{
    if(xhr.status===200){
      downloadStatus.value='saving'; downloadProgress.value=100
      const a=document.createElement('a'); a.href=URL.createObjectURL(xhr.response); a.download=filename
      document.body.appendChild(a); a.click(); document.body.removeChild(a); URL.revokeObjectURL(a.href)
      ElMessage({message:'下载完成！🎉',type:'success',plain:true}); await saveDownload({...base,status:'success'})
    } else { ElMessage.error('下载失败: HTTP '+xhr.status); await saveDownload({...base,status:'fail',errMsg:'HTTP '+xhr.status}) }
    resetDownload()
  }
  xhr.onerror=async()=>{ ElMessage.error('下载失败'); await saveDownload({...base,status:'fail',errMsg:'网络错误'}); resetDownload() }
  xhr.send()
}
function resetDownload(){setTimeout(()=>{downloadingKey.value=null;downloadProgress.value=0;downloadStatus.value=''},1200)}
function copyUrl(url){navigator.clipboard.writeText(url).then(()=>ElMessage({message:'已复制 📋',type:'success',plain:true}))}
function coverError(e){e.target.src='https://via.placeholder.com/100x75?text=No+Cover'}
</script>
<style scoped>
.page{min-height:100vh;background:var(--bg-page)}
.main{max-width:800px;margin:0 auto;padding:32px 20px 100px}
.page-header{display:flex;align-items:center;justify-content:space-between;margin-bottom:24px}
.page-title{display:flex;align-items:center;gap:8px}
.page-title h2{font-size:22px;font-weight:800;color:var(--text-primary)}
.count-badge{background:var(--gradient-btn);color:#fff;font-size:11px;font-weight:700;padding:2px 8px;border-radius:var(--border-radius-full)}
.loading-wrap{display:flex;justify-content:center;padding:80px}
.empty-card{padding:60px 40px!important}
.parse-list{display:flex;flex-direction:column;gap:14px}
.parse-item{padding:20px 22px}
.item-top{display:flex;gap:14px;align-items:flex-start;margin-bottom:14px}
.cover-wrap{position:relative;flex-shrink:0}
.item-cover{width:100px;height:75px;object-fit:cover;border-radius:var(--border-radius-md);display:block}
.platform-badge{position:absolute;bottom:5px;left:5px;background:rgba(0,0,0,0.65);color:#fff;font-size:9px;padding:1px 6px;border-radius:var(--border-radius-full)}
.item-info{flex:1;overflow:hidden}
.item-title{font-size:14px;font-weight:600;color:var(--text-primary);white-space:nowrap;overflow:hidden;text-overflow:ellipsis;margin-bottom:8px;line-height:1.4}
.item-meta{display:flex;flex-wrap:wrap;gap:12px}
.meta-item{display:flex;align-items:center;gap:4px;font-size:12px;color:var(--text-muted)}
.download-area{border-top:1px solid var(--border-color);padding-top:14px}
.download-area-title{display:flex;align-items:center;gap:5px;font-size:12px;font-weight:600;color:var(--text-secondary);margin-bottom:10px}
.dl-row{display:flex;align-items:center;justify-content:space-between;padding:9px 12px;background:var(--bg-input);border-radius:var(--border-radius-md);margin-bottom:8px;border:1px solid var(--border-color);transition:var(--transition)}
.dl-row:last-child{margin-bottom:0}
.dl-row:hover{border-color:var(--color-primary)}
.dl-quality{display:flex;align-items:center;gap:8px;font-size:13px;font-weight:600;color:var(--text-primary)}
.quality-dot{width:7px;height:7px;border-radius:50%;background:var(--gradient-btn);flex-shrink:0}
.dl-actions{display:flex;gap:6px}
.btn-dl{background:var(--gradient-btn)!important;border:none!important;font-weight:600!important}
.pagination{display:flex;justify-content:center;margin-top:28px}
.float-bar{position:fixed;bottom:0;left:0;right:0;background:var(--bg-card);padding:14px 24px;box-shadow:0 -4px 24px rgba(0,0,0,0.12);border-top:1px solid var(--border-color);z-index:200}
.float-bar-inner{display:flex;align-items:center;gap:10px;max-width:800px;margin:0 auto;font-size:13px;color:var(--color-primary);font-weight:500}
.slide-up-enter-active,.slide-up-leave-active{transition:transform 0.3s ease}
.slide-up-enter-from,.slide-up-leave-to{transform:translateY(100%)}
</style>
