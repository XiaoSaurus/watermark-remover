<template>
  <div class="page">
    <Navbar />
    <main class="main">
      <section class="hero">
        <div class="hero-badge"><el-icon><MagicStick /></el-icon><span>免费 · 无广告 · 无水印</span></div>
        <h1 class="hero-title">短视频去水印下载</h1>
        <p class="hero-sub">支持抖音 · 快手 · B站 · 微博 · 小红书，粘贴链接即可下载</p>
        <div class="platform-chips">
          <span v-for="p in platforms" :key="p.name" class="chip">{{ p.icon }} {{ p.name }}</span>
        </div>
      </section>

      <div class="input-card card">
        <div class="input-label"><el-icon><Link /></el-icon><span>粘贴分享链接</span></div>
        <el-input v-model="inputUrl" type="textarea" :rows="3"
          placeholder="粘贴视频分享链接或分享文案，例如：&#10;https://v.douyin.com/xxxxx/"
          resize="none" class="url-input" />
        <div class="input-actions">
          <el-button @click="inputUrl=''" :disabled="!inputUrl" round>
            <el-icon><Delete /></el-icon> 清空
          </el-button>
          <el-button type="primary" :loading="loading" :disabled="!inputUrl.trim()"
            @click="handleParse" round size="large" class="btn-parse">
            <el-icon v-if="!loading"><Search /></el-icon>
            {{ loading ? '解析中...' : '立即解析' }}
          </el-button>
        </div>
      </div>

      <transition name="slide-fade">
        <div v-if="result" class="result-card card">
          <div class="result-header">
            <div class="cover-wrap">
              <img :src="result.cover" class="cover-img" @error="coverError" />
              <span class="platform-badge-cover">{{ platformLabel }}</span>
            </div>
            <div class="result-meta">
              <h2 class="result-title">{{ result.title || '无标题' }}</h2>
              <div class="result-author"><el-icon><User /></el-icon><span>{{ result.author || '未知作者' }}</span></div>
            </div>
          </div>
          <el-divider style="margin:16px 0" />
          <transition name="fade">
            <div v-if="downloadStatus" class="progress-block">
              <div class="progress-info">
                <span class="progress-text">
                  <el-icon class="is-loading" v-if="downloadStatus==='downloading'"><Loading /></el-icon>
                  <el-icon v-else><Timer /></el-icon>
                  {{ downloadStatus==='saving'?'保存中...':`下载中 ${downloadProgress}%` }}
                </span>
                <span class="progress-pct-badge">{{ downloadStatus==='saving'?'100%':downloadProgress+'%' }}</span>
              </div>
              <el-progress :percentage="downloadStatus==='saving'?100:downloadProgress"
                :striped="downloadStatus==='downloading'" :striped-flow="downloadStatus==='downloading'"
                :duration="8" :stroke-width="8" :color="progressColors" />
            </div>
          </transition>
          <div class="download-section">
            <div class="section-title"><el-icon><Download /></el-icon><span>选择清晰度下载</span></div>
            <div class="quality-list">
              <div v-for="(v,i) in result.videoUrls" :key="i" class="quality-row">
                <div class="quality-info">
                  <span class="quality-dot"></span>
                  <span class="quality-name">{{ v.quality }}</span>
                </div>
                <div class="quality-actions">
                  <el-button type="primary" round
                    :loading="downloadingIndex===i"
                    :disabled="downloadingIndex!==-1 && downloadingIndex!==i"
                    @click="downloadVideo(v.url, v.quality, i)" class="btn-dl">
                    <el-icon v-if="downloadingIndex!==i"><Download /></el-icon>
                    {{ downloadingIndex===i ? downloadProgress+'%' : '下载视频' }}
                  </el-button>
                  <el-button round plain @click="copyUrl(v.url)" class="btn-copy">
                    <el-icon><CopyDocument /></el-icon>
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </transition>

      <div class="guide-card card">
        <div class="guide-title"><el-icon><InfoFilled /></el-icon><span>使用说明</span></div>
        <div class="guide-steps">
          <div v-for="(step,i) in steps" :key="i" class="guide-step">
            <span class="step-num">{{ i+1 }}</span>
            <span class="step-text">{{ step }}</span>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>
<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { MagicStick, Link, Delete, Search, User, Download, CopyDocument, InfoFilled, Loading, Timer } from '@element-plus/icons-vue'
import Navbar from '@/components/Navbar.vue'
import { videoApi } from '@/api/watermark'
import { saveParse, saveDownload } from '@/store/history'
const inputUrl = ref(''), loading = ref(false), result = ref(null)
const downloadingIndex = ref(-1), downloadProgress = ref(0), downloadStatus = ref('')
const platforms = [{name:'抖音',icon:'🎵'},{name:'快手',icon:'⚡'},{name:'B站',icon:'📺'},{name:'微博',icon:'🌐'},{name:'小红书',icon:'📕'}]
const steps = ['打开抖音/快手/B站等 App，找到想下载的视频','点击「分享」→「复制链接」或「复制文案」','粘贴到上方输入框，点击「立即解析」','在解析结果中选择清晰度，点击「下载视频」']
const progressColors = [{color:'#6c63ff',percentage:50},{color:'#764ba2',percentage:80},{color:'#18a058',percentage:100}]
const platformLabel = computed(() => {
  const map = {douyin:'🎵 抖音',kuaishou:'⚡ 快手',bilibili:'📺 B站',weibo:'🌐 微博',xiaohongshu:'📕 小红书'}
  return map[result.value?.platform] || result.value?.platform
})
async function handleParse() {
  if (!inputUrl.value.trim()) return
  loading.value = true; result.value = null
  try {
    const res = await videoApi.parse(inputUrl.value.trim())
    if (res.code === 200) {
      result.value = res.data
      ElMessage({message:'解析成功！',type:'success',plain:true})
      await saveParse({platform:res.data.platform,title:res.data.title,cover:res.data.cover,author:res.data.author,shareUrl:inputUrl.value.trim(),videoUrls:res.data.videoUrls})
    } else { ElMessage({message:res.message||'解析失败',type:'error',plain:true}) }
  } catch { ElMessage({message:'请求失败，请检查后端服务',type:'error',plain:true}) }
  finally { loading.value = false }
}
function downloadVideo(url, quality, idx) {
  if (downloadingIndex.value !== -1) return
  const filename = `video_${quality}_${Date.now()}.mp4`
  const proxyUrl = `/api/video/download?url=${encodeURIComponent(url)}&filename=${encodeURIComponent(filename)}`
  const base = {platform:result.value?.platform,title:result.value?.title,cover:result.value?.cover,quality,videoUrl:url}
  downloadingIndex.value = idx; downloadProgress.value = 0; downloadStatus.value = 'downloading'
  const xhr = new XMLHttpRequest()
  xhr.open('GET',proxyUrl,true); xhr.responseType = 'blob'
  xhr.onprogress = e => { if(e.lengthComputable) downloadProgress.value = Math.round(e.loaded/e.total*100) }
  xhr.onload = async () => {
    if (xhr.status === 200) {
      downloadStatus.value = 'saving'; downloadProgress.value = 100
      const a = document.createElement('a'); a.href = URL.createObjectURL(xhr.response); a.download = filename
      document.body.appendChild(a); a.click(); document.body.removeChild(a); URL.revokeObjectURL(a.href)
      ElMessage({message:'下载完成！🎉',type:'success',plain:true})
      await saveDownload({...base,status:'success'})
    } else {
      ElMessage({message:'下载失败: HTTP '+xhr.status,type:'error',plain:true})
      await saveDownload({...base,status:'fail',errMsg:'HTTP '+xhr.status})
    }
    resetDownload()
  }
  xhr.onerror = async () => {
    ElMessage({message:'下载失败，请检查网络',type:'error',plain:true})
    await saveDownload({...base,status:'fail',errMsg:'网络错误'})
    resetDownload()
  }
  xhr.send()
}
function resetDownload() { setTimeout(()=>{downloadingIndex.value=-1;downloadProgress.value=0;downloadStatus.value=''},1200) }
function copyUrl(url) { navigator.clipboard.writeText(url).then(()=>ElMessage({message:'链接已复制 📋',type:'success',plain:true})) }
function coverError(e) { e.target.src='https://via.placeholder.com/120x90?text=No+Cover' }
</script>
<style scoped>
.page{min-height:100vh;background:var(--bg-page)}
.main{max-width:760px;margin:0 auto;padding:40px 20px 80px;display:flex;flex-direction:column;gap:20px}
.hero{text-align:center;padding:20px 0 10px}
.hero-badge{display:inline-flex;align-items:center;gap:6px;background:var(--gradient-btn);color:#fff;padding:5px 16px;border-radius:var(--border-radius-full);font-size:12px;font-weight:600;margin-bottom:16px;box-shadow:0 4px 12px rgba(108,99,255,0.3)}
.hero-title{font-size:38px;font-weight:900;background:var(--gradient-btn);-webkit-background-clip:text;-webkit-text-fill-color:transparent;margin-bottom:10px;line-height:1.2}
.hero-sub{font-size:15px;color:var(--text-secondary);margin-bottom:20px}
.platform-chips{display:flex;flex-wrap:wrap;gap:8px;justify-content:center}
.chip{background:var(--bg-card);color:var(--text-secondary);padding:5px 14px;border-radius:var(--border-radius-full);font-size:13px;border:1px solid var(--border-color);box-shadow:var(--shadow-sm);transition:var(--transition)}
.chip:hover{border-color:var(--color-primary);color:var(--color-primary);transform:translateY(-1px)}
.input-card{padding:24px}
.input-label{display:flex;align-items:center;gap:6px;color:var(--text-secondary);font-size:13px;font-weight:600;margin-bottom:12px}
.url-input :deep(.el-textarea__inner){background:var(--bg-input);border-radius:var(--border-radius-md);border:1.5px solid var(--border-color);font-size:14px;transition:var(--transition);color:var(--text-primary)}
.url-input :deep(.el-textarea__inner:focus){border-color:var(--color-primary);box-shadow:0 0 0 3px rgba(108,99,255,0.12)}
.input-actions{display:flex;justify-content:flex-end;gap:10px;margin-top:14px}
.btn-parse{background:var(--gradient-btn)!important;border:none!important;padding:0 28px!important;font-weight:600!important;box-shadow:0 4px 16px rgba(108,99,255,0.35)!important}
.btn-parse:hover{transform:translateY(-1px);box-shadow:0 6px 20px rgba(108,99,255,0.45)!important}
.result-card{padding:24px}
.result-header{display:flex;gap:18px;align-items:flex-start}
.cover-wrap{position:relative;flex-shrink:0}
.cover-img{width:120px;height:90px;object-fit:cover;border-radius:var(--border-radius-md);display:block}
.platform-badge-cover{position:absolute;bottom:6px;left:6px;background:rgba(0,0,0,0.65);color:#fff;font-size:10px;padding:2px 7px;border-radius:var(--border-radius-full);backdrop-filter:blur(4px)}
.result-meta{flex:1}
.result-title{font-size:17px;font-weight:700;color:var(--text-primary);line-height:1.4;margin-bottom:8px}
.result-author{display:flex;align-items:center;gap:5px;color:var(--text-secondary);font-size:13px}
.progress-block{background:var(--bg-input);border-radius:var(--border-radius-md);padding:14px 18px;margin-bottom:16px}
.progress-info{display:flex;justify-content:space-between;align-items:center;margin-bottom:10px}
.progress-text{display:flex;align-items:center;gap:6px;font-size:13px;color:var(--color-primary);font-weight:500}
.progress-pct-badge{background:var(--gradient-btn);color:#fff;font-size:12px;font-weight:700;padding:2px 10px;border-radius:var(--border-radius-full)}
.download-section{}
.section-title{display:flex;align-items:center;gap:6px;font-size:14px;font-weight:700;color:var(--text-primary);margin-bottom:12px}
.quality-list{display:flex;flex-direction:column;gap:10px}
.quality-row{display:flex;align-items:center;justify-content:space-between;padding:12px 16px;background:var(--bg-input);border-radius:var(--border-radius-md);border:1px solid var(--border-color);transition:var(--transition)}
.quality-row:hover{border-color:var(--color-primary);background:var(--bg-card-hover)}
.quality-info{display:flex;align-items:center;gap:10px}
.quality-dot{width:8px;height:8px;border-radius:50%;background:var(--gradient-btn)}
.quality-name{font-weight:600;color:var(--text-primary);font-size:14px}
.quality-actions{display:flex;gap:8px}
.btn-dl{background:var(--gradient-btn)!important;border:none!important;font-weight:600!important;min-width:100px}
.btn-copy{border-color:var(--border-color)!important;color:var(--text-secondary)!important}
.guide-card{padding:22px 24px}
.guide-title{display:flex;align-items:center;gap:7px;font-size:15px;font-weight:700;color:var(--text-primary);margin-bottom:16px}
.guide-steps{display:flex;flex-direction:column;gap:10px}
.guide-step{display:flex;align-items:flex-start;gap:12px}
.step-num{flex-shrink:0;width:24px;height:24px;border-radius:50%;background:var(--gradient-btn);color:#fff;font-size:12px;font-weight:700;display:flex;align-items:center;justify-content:center}
.step-text{font-size:14px;color:var(--text-secondary);line-height:1.6;padding-top:2px}
.slide-fade-enter-active{transition:all 0.35s cubic-bezier(0.4,0,0.2,1)}
.slide-fade-leave-active{transition:all 0.2s ease}
.slide-fade-enter-from{opacity:0;transform:translateY(16px)}
.slide-fade-leave-to{opacity:0;transform:translateY(-8px)}
.fade-enter-active,.fade-leave-active{transition:opacity 0.25s ease}
.fade-enter-from,.fade-leave-to{opacity:0}
</style>
