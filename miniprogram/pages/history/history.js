const { getHistory, deleteHistory, clearHistoryApi, saveHistory, getDownloadUrl } = require('../../utils/api')

Page({
  data: {
    list: [],
    total: 0,
    page: 0,
    pageSize: 20,
    pageLoading: false,
    platformMap: {
      douyin: '🎵 抖音', kuaishou: '⚡ 快手',
      bilibili: '📺 B站', weibo: '🌐 微博', xiaohongshu: '📕 小红书'
    },
    downloadingId: null,
    downloadProgress: 0,
    downloadStatus: ''
  },

  onShow() {
    this.loadHistory()
  },

  async loadHistory() {
    this.setData({ pageLoading: true })
    try {
      const res = await getHistory(this.data.page, this.data.pageSize)
      const list = (res.data.content || []).map(item => ({
        ...item,
        timeStr: this.formatTime(item.createdAt)
      }))
      this.setData({ list, total: res.data.totalElements || 0 })
    } catch (err) {
      wx.showToast({ title: '加载失败: ' + (err.message || ''), icon: 'none' })
    } finally {
      this.setData({ pageLoading: false })
    }
  },

  formatTime(ts) {
    if (!ts) return ''
    const d = new Date(ts)
    const pad = n => String(n).padStart(2, '0')
    return `${d.getMonth()+1}/${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
  },

  clearAll() {
    wx.showModal({
      title: '确认清空', content: '确定要清空所有历史记录吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            await clearHistoryApi()
            this.setData({ list: [], total: 0 })
            wx.showToast({ title: '已清空', icon: 'success' })
          } catch {
            wx.showToast({ title: '清空失败', icon: 'none' })
          }
        }
      }
    })
  },

  async deleteItem(e) {
    const id = e.currentTarget.dataset.id
    try {
      await deleteHistory(id)
      const list = this.data.list.filter(i => i.id !== id)
      this.setData({ list, total: this.data.total - 1 })
      wx.showToast({ title: '已删除', icon: 'success' })
    } catch {
      wx.showToast({ title: '删除失败', icon: 'none' })
    }
  },

  reDownload(e) {
    const item = e.currentTarget.dataset.item
    if (!item.videoUrl) {
      wx.showToast({ title: '缺少视频地址', icon: 'none' })
      return
    }
    if (this.data.downloadingId !== null) return

    const filename = `video_${item.quality}_${Date.now()}.mp4`
    const downloadUrl = getDownloadUrl(item.videoUrl, filename)
    const self = this

    self.setData({ downloadingId: item.id, downloadProgress: 0, downloadStatus: 'downloading' })

    const task = wx.downloadFile({
      url: downloadUrl,
      success(res) {
        if (res.statusCode === 200) {
          self.setData({ downloadStatus: 'saving', downloadProgress: 100 })
          wx.saveVideoToPhotosAlbum({
            filePath: res.tempFilePath,
            success() {
              wx.showToast({ title: '已保存到相册 ✅', icon: 'success' })
              self._updateStatus(item.id, 'success', '')
              self._resetDownload()
            },
            fail(err) {
              self._resetDownload()
              self._updateStatus(item.id, 'fail', '保存相册失败')
              if (err.errMsg && err.errMsg.includes('auth deny')) {
                wx.showModal({
                  title: '需要相册权限', content: '请授权访问相册',
                  confirmText: '去授权',
                  success(r) { if (r.confirm) wx.openSetting() }
                })
              }
            }
          })
        } else {
          wx.showToast({ title: '下载失败 HTTP ' + res.statusCode, icon: 'none' })
          self._updateStatus(item.id, 'fail', 'HTTP ' + res.statusCode)
          self._resetDownload()
        }
      },
      fail(err) {
        wx.showToast({ title: '下载失败', icon: 'none' })
        self._updateStatus(item.id, 'fail', err.errMsg || '网络错误')
        self._resetDownload()
      }
    })

    task.onProgressUpdate(res => {
      self.setData({ downloadProgress: res.progress })
    })
  },

  async _updateStatus(id, status, errMsg) {
    // 更新列表显示
    const list = this.data.list.map(i =>
      i.id === id ? { ...i, status, errMsg } : i
    )
    this.setData({ list })
    // 同步到数据库
    const item = this.data.list.find(i => i.id === id)
    if (item) {
      try {
        await saveHistory({ ...item, url: item.videoUrl, status, errMsg, client: 'miniprogram' })
      } catch {}
    }
  },

  _resetDownload() {
    setTimeout(() => {
      this.setData({ downloadingId: null, downloadProgress: 0, downloadStatus: '' })
    }, 1000)
  }
})
