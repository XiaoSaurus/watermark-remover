const { parseVideo, getDownloadUrl, saveParseHistory, saveDownloadHistory } = require('../../utils/api')
const app = getApp()

Page({
  data: {
    inputUrl: '',
    loading: false,
    result: null,
    platformLabel: '',
    downloadingIndex: -1,
    downloadProgress: 0,
    downloadStatus: '',
    showTheme: false,
    currentThemeKey: 'purple',
    theme: {},
    themeList: [],
    platforms: [
      { name:'抖音',icon:'🎵' },{ name:'快手',icon:'⚡' },
      { name:'B站',icon:'📺' },{ name:'微博',icon:'🌐' },{ name:'小红书',icon:'📕' }
    ],
    steps: [
      '打开抖音/快手/B站等 App，找到想下载的视频',
      '点击「分享」→「复制链接」或「复制文案」',
      '粘贴到上方输入框，点击「立即解析」',
      '在解析结果中选择清晰度，点击「下载」'
    ]
  },

  onLoad() {
    this._applyTheme()
  },

  onShow() {
    this._applyTheme()
  },

  _applyTheme() {
    const key = app.globalData.theme || 'purple'
    const theme = app.getTheme()
    const themeList = Object.entries(app.themes).map(([k,v]) => ({ key: k, ...v }))
    this.setData({ currentThemeKey: key, theme, themeList })
    // 更新导航栏颜色
    wx.setNavigationBarColor({ frontColor: '#ffffff', backgroundColor: theme.navBg })
  },

  onInput(e) { this.setData({ inputUrl: e.detail.value }) },
  clearInput() { this.setData({ inputUrl: '', result: null }) },

  showThemePicker() { this.setData({ showTheme: true }) },
  hideThemePicker() { this.setData({ showTheme: false }) },

  selectTheme(e) {
    const key = e.currentTarget.dataset.key
    app.setTheme(key)
    this._applyTheme()
    this.setData({ showTheme: false })
    wx.showToast({ title: '主题已切换', icon: 'success', duration: 1000 })
  },

  async handleParse() {
    const url = this.data.inputUrl.trim()
    if (!url) return
    this.setData({ loading: true, result: null })
    try {
      const res = await parseVideo(url)
      if (res.code === 200) {
        const platformMap = { douyin:'🎵 抖音',kuaishou:'⚡ 快手',bilibili:'📺 B站',weibo:'🌐 微博',xiaohongshu:'📕 小红书' }
        this.setData({ result: res.data, platformLabel: platformMap[res.data.platform] || res.data.platform })
        wx.showToast({ title: '解析成功 ✅', icon: 'none', duration: 1500 })
        saveParseHistory({ platform:res.data.platform, title:res.data.title, cover:res.data.cover, author:res.data.author, shareUrl:url, videoUrls:res.data.videoUrls }).catch(()=>{})
      } else {
        wx.showToast({ title: res.message || '解析失败', icon: 'none', duration: 3000 })
      }
    } catch (err) {
      wx.showToast({ title: err.message || '网络错误', icon: 'none', duration: 3000 })
    } finally {
      this.setData({ loading: false })
    }
  },

  downloadVideo(e) {
    const { url, quality, index } = e.currentTarget.dataset
    if (this.data.downloadingIndex !== -1) return
    const filename = `video_${quality}_${Date.now()}.mp4`
    const downloadUrl = getDownloadUrl(url, filename)
    const self = this
    const base = { platform:self.data.result.platform, title:self.data.result.title, cover:self.data.result.cover, quality, videoUrl:url }
    self.setData({ downloadingIndex: index, downloadProgress: 0, downloadStatus: 'downloading' })
    const task = wx.downloadFile({
      url: downloadUrl,
      success(res) {
        if (res.statusCode === 200) {
          self.setData({ downloadStatus: 'saving', downloadProgress: 100 })
          wx.saveVideoToPhotosAlbum({
            filePath: res.tempFilePath,
            success() {
              wx.showToast({ title: '已保存到相册 ✅', icon: 'none', duration: 2000 })
              saveDownloadHistory({ ...base, status:'success' }).catch(()=>{})
              self._resetDownload()
            },
            fail(err) {
              saveDownloadHistory({ ...base, status:'fail', errMsg:'保存相册失败' }).catch(()=>{})
              self._resetDownload()
              if (err.errMsg && err.errMsg.includes('auth deny')) {
                wx.showModal({ title:'需要相册权限', content:'请授权访问相册', confirmText:'去授权', success(r){ if(r.confirm) wx.openSetting() } })
              }
            }
          })
        } else {
          saveDownloadHistory({ ...base, status:'fail', errMsg:'HTTP '+res.statusCode }).catch(()=>{})
          wx.showToast({ title:'下载失败 HTTP '+res.statusCode, icon:'none' })
          self._resetDownload()
        }
      },
      fail(err) {
        saveDownloadHistory({ ...base, status:'fail', errMsg:err.errMsg||'网络错误' }).catch(()=>{})
        wx.showToast({ title:'下载失败', icon:'none' })
        self._resetDownload()
      }
    })
    task.onProgressUpdate(res => { self.setData({ downloadProgress: res.progress }) })
  },

  _resetDownload() {
    this.setData({ downloadingIndex:-1, downloadProgress:0, downloadStatus:'' })
  },

  copyUrl(e) {
    wx.setClipboardData({ data: e.currentTarget.dataset.url, success:()=>wx.showToast({title:'链接已复制',icon:'success'}) })
  }
})
