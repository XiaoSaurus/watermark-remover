// app.js
App({
  globalData: {
    // 后端 API 地址 - 开发时用本机 IP，上线后换成服务器地址
    // 注意：微信小程序不支持 localhost，需要用本机局域网 IP
    apiBase: 'http://192.168.1.100:8080',
    history: []
  },

  onLaunch() {
    // 读取历史记录
    const history = wx.getStorageSync('downloadHistory') || []
    this.globalData.history = history
  },

  // 添加历史记录
  addHistory(item) {
    const history = this.globalData.history
    history.unshift({
      ...item,
      time: Date.now()
    })
    // 最多保留 50 条
    if (history.length > 50) history.pop()
    this.globalData.history = history
    wx.setStorageSync('downloadHistory', history)
  }
})