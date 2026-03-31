const app = getApp()

Page({
  data: {
    list: [],
    platformMap: {
      douyin: '🎵 抖音', kuaishou: '⚡ 快手',
      bilibili: '📺 B站', weibo: '🌐 微博', xiaohongshu: '📕 小红书'
    },
    downloadingTime: null,
    downloadProgress: 0,
    downloadStatus: ''
  },

  onShow() {
    const history = app.globalData.history || []
    const list = history.map(item => ({
      ...item,
      timeStr: this.formatTime(item.time)
    }))
    this.setData({ list })
  },

  formatTime(ts) {
    const d = new Date(ts)
    const pad = n => String(n).padStart(2, '0')
    return `${d.getMonth()+1}/${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
  },

  clearAll() {
    wx.showModal({
      title: '确认清空',
      content: '确定要清空所有历史记录吗？',
      success: (res) => {
        if (res.confirm) {
          app.globalData.history = []
          wx.removeStorageSync('downloadHistory')
          this.setData({ list: [] })
          wx.showToast({ title: '已清空', icon: 'success' })
        }
      }
    })
  },

  reDownload(e) {
    const item = e.currentTarget.dataset.item
    if (!item.url) {
      wx.showToast({ title: '该记录缺少视频地址', icon: 'none' })
      return
    }
    if (this.data.downloadingTime !== null) return

    const { getDownloadUrl } = require('../../utils/api')
    const filename = `video_${item.quality}_${Date.now()}.mp4`
    const downloadUrl = getDownloadUrl(item.url, filename)
    const self = this

    self.setData({ downloadingTime: item.time, downloadProgress: 0, downloadStatus: 'downloading' })

    const task = wx.downloadFile({
      url: downloadUrl,
      success(res) {
        if (res.statusCode === 200) {
          self.setData({ downloadStatus: 'saving', downloadProgress: 100 })
          wx.saveVideoToPhotosAlbum({
            filePath: res.tempFilePath,
            success() {
              wx.showToast({ title: '已保存到相册 ✅', icon: 'success' })
              self._updateItemStatus(item.time, 'success', '')
              self._resetDownload()
            },
            fail(err) {
              self._resetDownload()
              if (err.errMsg && err.errMsg.includes('auth deny')) {
                wx.showModal({
                  title: '需要相册权限', content: '请授权访问相册以保存视频',
                  confirmText: '去授权',
                  success(r) { if (r.confirm) wx.openSetting() }
                })
              } else {
                wx.showToast({ title: '保存失败', icon: 'none' })
                self._updateItemStatus(item.time, 'fail', '保存失败')
              }
            }
          })
        } else {
          wx.showToast({ title: '下载失败: HTTP ' + res.statusCode, icon: 'none' })
          self._updateItemStatus(item.time, 'fail', 'HTTP ' + res.statusCode)
          self._resetDownload()
        }
      },
      fail(err) {
        wx.showToast({ title: '下载失败', icon: 'none' })
        self._updateItemStatus(item.time, 'fail', err.errMsg || '网络错误')
        self._resetDownload()
      }
    })

    task.onProgressUpdate(res => {
      self.setData({ downloadProgress: res.progress })
    })
  },

  _updateItemStatus(time, status, errMsg) {
    // 更新列表
    const list = this.data.list.map(i =>
      i.time === time ? { ...i, status, errMsg } : i
    )
    this.setData({ list })
    // 同步 globalData 和 storage
    const history = app.globalData.history.map(i =>
      i.time === time ? { ...i, status, errMsg } : i
    )
    app.globalData.history = history
    wx.setStorageSync('downloadHistory', history)
  },

  _resetDownload() {
    setTimeout(() => {
      this.setData({ downloadingTime: null, downloadProgress: 0, downloadStatus: '' })
    }, 1000)
  }
})
