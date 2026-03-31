const app = getApp()

Page({
  data: {
    list: [],
    platformMap: {
      douyin: '🎵 抖音',
      kuaishou: '⚡ 快手',
      bilibili: '📺 B站',
      weibo: '🌐 微博',
      xiaohongshu: '📕 小红书'
    }
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
  }
})