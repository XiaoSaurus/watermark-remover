const { parseVideo, getDownloadUrl, saveParseHistory, saveDownloadHistory } = require('../../utils/api')
const app = getApp()

Page({
  data: {
    inputUrl: '', loading: false, result: null, platformLabel: '',
    downloadingIndex: -1, downloadProgress: 0, downloadStatus: '',
    showTheme: false, currentThemeKey: 'purple', theme: {}, themeList: [],
    platforms: [
      { name:'抖音',  iconPath:'/images/platform_douyin.png' },
      { name:'快手',  iconPath:'/images/platform_kuaishou.png' },
      { name:'B站',   iconPath:'/images/platform_bilibili.png' },
      { name:'微博',  iconPath:'/images/platform_weibo.png' },
      { name:'小红书',iconPath:'/images/platform_xiaohongshu.png' }
    ],
    steps: [
      { icon:'/images/step_1.png', title:'打开 App 找到视频', desc:'抖音/快手/B站/微博/小红书均支持' },
      { icon:'/images/step_2.png', title:'点击分享复制链接', desc:'点「分享」→「复制链接」或「复制文案」' },
      { icon:'/images/step_3.png', title:'粘贴链接立即解析', desc:'粘贴到输入框，点击「立即解析」' },
      { icon:'/images/step_4.png', title:'选择清晰度下载',   desc:'选择画质，点击「下载」保存到相册' }
    ]
  },

  onLoad() { this._applyTheme() },
  onShow() { this._applyTheme() },

  _applyTheme() {
    const key = app.globalData.theme || 'purple'
    const theme = app.getTheme()
    const themeList = Object.entries(app.themes).map(([k,v]) => ({ key:k, ...v }))
    this.setData({ currentThemeKey:key, theme, themeList })
    wx.setNavigationBarColor({ frontColor:'#ffffff', backgroundColor:theme.navBg })
  },

  onInput(e) { this.setData({ inputUrl: e.detail.value }) },
  clearInput() { this.setData({ inputUrl:'', result:null }) },
  showThemePicker() { this.setData({ showTheme:true }) },
  hideThemePicker() { this.setData({ showTheme:false }) },

  selectTheme(e) {
    const key = e.currentTarget.dataset.key
    app.setTheme(key)
    this._applyTheme()
    this.setData({ showTheme:false })
    wx.showToast({ title:'主题已切换 ✨', icon:'none', duration:1200 })
  },

  async handleParse() {
    const url = this.data.inputUrl.trim()
    if (!url || this.data.loading) return
    this.setData({ loading:true, result:null })
    try {
      const res = await parseVideo(url)
      if (res.code === 200) {
        const pm = { douyin:'🎵 抖音',kuaishou:'⚡ 快手',bilibili:'📺 B站',weibo:'🌐 微博',xiaohongshu:'📕 小红书' }
        this.setData({ result:res.data, platformLabel:pm[res.data.platform]||res.data.platform })
        wx.showToast({ title:'解析成功 ✅', icon:'none', duration:1500 })
        saveParseHistory({ platform:res.data.platform, title:res.data.title, cover:res.data.cover, author:res.data.author, shareUrl:url, videoUrls:res.data.videoUrls }).catch(()=>{})
      } else {
        wx.showToast({ title:res.message||'解析失败', icon:'none', duration:3000 })
      }
    } catch(err) {
      wx.showToast({ title:err.message||'网络错误', icon:'none', duration:3000 })
    } finally {
      this.setData({ loading:false })
    }
  },

  downloadVideo(e) {
    const { url, quality, index } = e.currentTarget.dataset
    if (this.data.downloadingIndex !== -1) return
    const filename = `video_${quality}_${Date.now()}.mp4`
    const downloadUrl = getDownloadUrl(url, filename)
    const self = this
    const base = { platform:self.data.result.platform, title:self.data.result.title, cover:self.data.result.cover, quality, videoUrl:url }
    self.setData({ downloadingIndex:index, downloadProgress:0, downloadStatus:'downloading' })
    const task = wx.downloadFile({
      url: downloadUrl,
      success(res) {
        if (res.statusCode === 200) {
          self.setData({ downloadStatus:'saving', downloadProgress:100 })
          wx.saveVideoToPhotosAlbum({
            filePath: res.tempFilePath,
            success() { wx.showToast({title:'已保存到相册 ✅',icon:'none',duration:2000}); saveDownloadHistory({...base,status:'success'}).catch(()=>{}); self._resetDownload() },
            fail(err) {
              saveDownloadHistory({...base,status:'fail',errMsg:'保存相册失败'}).catch(()=>{}); self._resetDownload()
              if (err.errMsg&&err.errMsg.includes('auth deny')) wx.showModal({title:'需要相册权限',content:'请授权访问相册',confirmText:'去授权',success(r){if(r.confirm)wx.openSetting()}})
            }
          })
        } else {
          saveDownloadHistory({...base,status:'fail',errMsg:'HTTP '+res.statusCode}).catch(()=>{})
          wx.showToast({title:'下载失败',icon:'none'}); self._resetDownload()
        }
      },
      fail(err) {
        saveDownloadHistory({...base,status:'fail',errMsg:err.errMsg||'网络错误'}).catch(()=>{})
        wx.showToast({title:'下载失败',icon:'none'}); self._resetDownload()
      }
    })
    task.onProgressUpdate(res => { self.setData({ downloadProgress:res.progress }) })
  },

  _resetDownload() { this.setData({ downloadingIndex:-1, downloadProgress:0, downloadStatus:'' }) },
  copyUrl(e) { wx.setClipboardData({data:e.currentTarget.dataset.url, success:()=>wx.showToast({title:'链接已复制',icon:'success'})}) }
})
