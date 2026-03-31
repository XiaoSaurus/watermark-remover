const { getParseHistory, deleteParseHistory, clearParseHistoryApi, saveDownloadHistory, getDownloadUrl } = require('../../utils/api')
const app = getApp()

Page({
  data: {
    list: [], total: 0, page: 0, pageLoading: false,
    theme: {},
    platformMap: { douyin:'🎵 抖音',kuaishou:'⚡ 快手',bilibili:'📺 B站',weibo:'🌐 微博',xiaohongshu:'📕 小红书' },
    downloadingKey: null, downloadProgress: 0, downloadStatus: ''
  },

  onShow() {
    this.setData({ theme: app.getTheme() })
    this.loadList()
  },

  async loadList() {
    this.setData({ pageLoading: true })
    try {
      const res = await getParseHistory(this.data.page, 20)
      const list = (res.data.content || []).map(item => ({ ...item, timeStr: this.formatTime(item.createdAt) }))
      this.setData({ list, total: res.data.totalElements || 0 })
    } catch { wx.showToast({ title:'加载失败', icon:'none' }) }
    finally { this.setData({ pageLoading: false }) }
  },

  formatTime(ts) {
    if (!ts) return ''
    const d = new Date(ts), pad = n => String(n).padStart(2,'0')
    return `${d.getMonth()+1}/${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
  },

  clearAll() {
    wx.showModal({ title:'确认清空', content:'确定清空所有解析历史？',
      success: async res => {
        if (res.confirm) {
          try { await clearParseHistoryApi(); this.setData({ list:[], total:0 }); wx.showToast({ title:'已清空', icon:'success' }) }
          catch { wx.showToast({ title:'清空失败', icon:'none' }) }
        }
      }
    })
  },

  async deleteItem(e) {
    const id = e.currentTarget.dataset.id
    try { await deleteParseHistory(id); this.setData({ list: this.data.list.filter(i=>i.id!==id) }); wx.showToast({ title:'已删除', icon:'success' }) }
    catch { wx.showToast({ title:'删除失败', icon:'none' }) }
  },

  downloadVideo(e) {
    const { item, url, quality, vi } = e.currentTarget.dataset
    const key = `${item.id}-${vi}`
    if (this.data.downloadingKey !== null) return
    const filename = `video_${quality}_${Date.now()}.mp4`
    const downloadUrl = getDownloadUrl(url, filename)
    const self = this
    const base = { platform:item.platform, title:item.title, cover:item.cover, quality, videoUrl:url }
    self.setData({ downloadingKey:key, downloadProgress:0, downloadStatus:'downloading' })
    const task = wx.downloadFile({
      url: downloadUrl,
      success(res) {
        if (res.statusCode === 200) {
          self.setData({ downloadStatus:'saving', downloadProgress:100 })
          wx.saveVideoToPhotosAlbum({
            filePath: res.tempFilePath,
            success() { wx.showToast({title:'已保存到相册 ✅',icon:'none'}); saveDownloadHistory({...base,status:'success'}).catch(()=>{}); self._resetDownload() },
            fail(err) {
              saveDownloadHistory({...base,status:'fail',errMsg:'保存相册失败'}).catch(()=>{}); self._resetDownload()
              if (err.errMsg&&err.errMsg.includes('auth deny')) wx.showModal({title:'需要相册权限',content:'请授权访问相册',confirmText:'去授权',success(r){if(r.confirm)wx.openSetting()}})
            }
          })
        } else { saveDownloadHistory({...base,status:'fail',errMsg:'HTTP '+res.statusCode}).catch(()=>{}); wx.showToast({title:'下载失败',icon:'none'}); self._resetDownload() }
      },
      fail(err) { saveDownloadHistory({...base,status:'fail',errMsg:err.errMsg||'网络错误'}).catch(()=>{}); wx.showToast({title:'下载失败',icon:'none'}); self._resetDownload() }
    })
    task.onProgressUpdate(res => { self.setData({ downloadProgress: res.progress }) })
  },

  _resetDownload() { setTimeout(()=>{ this.setData({downloadingKey:null,downloadProgress:0,downloadStatus:''}) },1000) },
  copyUrl(e) { wx.setClipboardData({data:e.currentTarget.dataset.url,success:()=>wx.showToast({title:'链接已复制',icon:'success'})}) }
})
