<template>
  <div class="page">
    <Navbar />
    <main class="main">
      <div class="page-header">
        <div class="page-title">
          <el-icon><Download /></el-icon><h2>下载历史</h2>
          <span class="count-badge" v-if="total">{{ total }}</span>
        </div>
        <el-button v-if="list.length" type="danger" plain round size="small" @click="handleClear">
          <el-icon><Delete /></el-icon> 清空
        </el-button>
      </div>
      <div v-if="pageLoading" class="loading-wrap">
        <el-icon class="is-loading" size="36" color="var(--color-primary)"><Loading /></el-icon>
      </div>
      <el-empty v-else-if="!list.length" description="暂无下载记录，去解析历史中下载视频吧～" class="empty-card card" />
      <div v-else class="history-list">
        <div v-for="item in list" :key="item.id" class="history-item card">
          <img :src="item.cover" class="item-cover" @error="coverError" />
          <div class="item-info">
            <div class="item-title">{{ item.title || '无标题' }}</div>
            <div class="item-tags">
              <span class="tag tag-platform">{{ platformMap[item.platform] || item.platform }}</span>
              <span class="tag tag-quality">{{ item.quality }}</span>
              <span class="tag tag-success" v-if="item.status==='success'">
                <el-icon><CircleCheck /></el-icon> 下载成功
              </span>
              <span class="tag tag-fail" v-else-if="item.status==='fail'" :title="item.errMsg">
                <el-icon><CircleClose /></el-icon> 下载失败
              </span>
            </div>
            <div class="item-bottom">
              <span class="item-time"><el-icon><Clock /></el-icon>{{ formatTime(item.createdAt) }}</span>
              <span class="item-errmsg" v-if="item.status==='fail' && item.errMsg">{{ item.errMsg }}</span>
            </div>
          </div>
          <el-button size="small" circle plain @click="handleDelete(item.id)"><el-icon><Delete /></el-icon></el-button>
        </div>
      </div>
      <div v-if="total > pageSize" class="pagination">
        <el-pagination v-model:current-page="currentPage" :page-size="pageSize"
          :total="total" layout="prev,pager,next" @current-change="loadList" background />
      </div>
    </main>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Download, Delete, Clock, CircleCheck, CircleClose, Loading } from '@element-plus/icons-vue'
import Navbar from '@/components/Navbar.vue'
import { historyApi } from '@/api/watermark'
import { clearDownloadHistory, removeDownloadHistory } from '@/store/history'
const list=ref([]),total=ref(0),currentPage=ref(1),pageSize=20,pageLoading=ref(false)
const platformMap={douyin:'🎵 抖音',kuaishou:'⚡ 快手',bilibili:'📺 B站',weibo:'🌐 微博',xiaohongshu:'📕 小红书'}
onMounted(loadList)
async function loadList(){
  pageLoading.value=true
  try{const res=await historyApi.list(currentPage.value-1,pageSize);list.value=res.data.content;total.value=res.data.totalElements}
  catch{ElMessage.error('加载失败')}finally{pageLoading.value=false}
}
function formatTime(ts){
  if(!ts)return'';const d=new Date(ts),pad=n=>String(n).padStart(2,'0')
  return`${d.getMonth()+1}/${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}
async function handleClear(){
  await ElMessageBox.confirm('确定清空所有下载历史？','确认清空',{type:'warning',confirmButtonText:'清空',cancelButtonText:'取消'})
  await clearDownloadHistory();list.value=[];total.value=0;ElMessage({message:'已清空',type:'success',plain:true})
}
async function handleDelete(id){
  await removeDownloadHistory(id);list.value=list.value.filter(i=>i.id!==id);total.value--
  ElMessage({message:'已删除',type:'success',plain:true})
}
function coverError(e){e.target.src='https://via.placeholder.com/80x60?text=No+Cover'}
</script>
<style scoped>
.page{min-height:100vh;background:var(--bg-page)}
.main{max-width:760px;margin:0 auto;padding:32px 20px 60px}
.page-header{display:flex;align-items:center;justify-content:space-between;margin-bottom:24px}
.page-title{display:flex;align-items:center;gap:8px}
.page-title h2{font-size:22px;font-weight:800;color:var(--text-primary)}
.count-badge{background:var(--gradient-btn);color:#fff;font-size:11px;font-weight:700;padding:2px 8px;border-radius:var(--border-radius-full)}
.loading-wrap{display:flex;justify-content:center;padding:80px}
.empty-card{padding:60px 40px!important}
.history-list{display:flex;flex-direction:column;gap:12px}
.history-item{display:flex;gap:14px;align-items:center;padding:16px 20px}
.item-cover{width:80px;height:60px;object-fit:cover;border-radius:var(--border-radius-md);flex-shrink:0;background:var(--bg-input)}
.item-info{flex:1;overflow:hidden;min-width:0}
.item-title{font-size:14px;font-weight:600;color:var(--text-primary);white-space:nowrap;overflow:hidden;text-overflow:ellipsis;margin-bottom:7px}
.item-tags{display:flex;flex-wrap:wrap;gap:5px;margin-bottom:6px;align-items:center}
.tag{font-size:11px;padding:2px 9px;border-radius:var(--border-radius-full);font-weight:500;display:inline-flex;align-items:center;gap:3px}
.tag-platform{color:var(--color-primary);background:var(--bg-tag)}
.tag-quality{color:var(--text-secondary);background:var(--bg-input);border:1px solid var(--border-color)}
.tag-success{color:var(--color-success);background:var(--color-success-bg)}
.tag-fail{color:var(--color-danger);background:var(--color-danger-bg);cursor:help}
.item-bottom{display:flex;align-items:center;gap:10px}
.item-time{display:flex;align-items:center;gap:4px;font-size:11px;color:var(--text-muted)}
.item-errmsg{font-size:11px;color:var(--color-danger)}
.pagination{display:flex;justify-content:center;margin-top:28px}
</style>
