const { getDownloadHistory, deleteDownloadHistory, clearDownloadHistoryApi } = require('../../utils/api')

Page({
  data: {
    list: [],
    total: 0,
    page: 0,
    pageLoading: false,
    platformMap: {
      douyin:'🎵 抖音',kuaishou:'⚡ 快手',bilibili:'📺 B站',weibo:'🌐 微博',xiaohongshu:'📕 小红书'
    }
  },

  onShow() { this.loadList() },

  async loadList() {
    this.setData({ pageLoading: true })
    try {
      const res = await getDownloadHistory(this.data.page, 20)
      const list = (res.data.content || []).map(item => ({
        ...item,
        timeStr: this.formatTime(item.createdAt)
      }))
      this.setData({ list, total: res.data.totalElements || 0 })
    } catch {
      wx.showToast({ title: '加载失败', icon: 'none' })
    } finally {
      this.setData({ pageLoading: false })
    }
  },

  formatTime(ts) {
    if (!ts) return ''
    const d = new Date(ts)
    const pad = n => String(n).padStart(2,'0')
    return `${d.getMonth()+1}/${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
  },

  clearAll() {
    wx.showModal({
      title: '确认清空', content: '确定清空所有下载历史？',
      success: async res => {
        if (res.confirm) {
          try {
            await clearDownloadHistoryApi()
            this.setData({ list: [], total: 0 })
            wx.showToast({ title: '已清空', icon: 'success' })
          } catch { wx.showToast({ title: '清空失败', icon: 'none' }) }
        }
      }
    })
  },

  async deleteItem(e) {
    const id = e.currentTarget.dataset.id
    try {
      await deleteDownloadHistory(id)
      this.setData({ list: this.data.list.filter(i => i.id !== id) })
      wx.showToast({ title: '已删除', icon: 'success' })
    } catch { wx.showToast({ title: '删除失败', icon: 'none' }) }
  }
})
