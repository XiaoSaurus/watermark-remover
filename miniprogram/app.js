App({
  globalData: {
    // 开发环境API地址 - 生产环境请修改为实际服务器地址
    apiBase: 'http://localhost:8080',
    history: [],
    theme: 'purple',
    userInfo: null,
    token: null
  },

  // 5 套主题（简约：页头/背景为浅纯色，主按钮为纯色 primary）
  themes: {
    purple: {
      name: '紫罗兰', emoji: '💜', desc: '经典主色，简约清爽',
      primary: '#6c63ff',
      primaryLight: '#8b85ff',
      gradBtn: '#6c63ff',
      gradBg: '#f7f8fa',
      navBg: '#ffffff'
    },
    slate: {
      name: '深空蓝', emoji: '🌌', desc: '蓝调工具感',
      primary: '#4a7fa5',
      primaryLight: '#6b9fc4',
      gradBtn: '#4a7fa5',
      gradBg: '#f4f7f9',
      navBg: '#ffffff'
    },
    sage: {
      name: '抹茶绿', emoji: '🍵', desc: '清新自然',
      primary: '#3d8b6e',
      primaryLight: '#5aaf8e',
      gradBtn: '#3d8b6e',
      gradBg: '#f4faf7',
      navBg: '#ffffff'
    },
    rose: {
      name: '玫瑰粉', emoji: '🌸', desc: '柔和粉调',
      primary: '#c0607a',
      primaryLight: '#d4849a',
      gradBtn: '#c0607a',
      gradBg: '#fdf5f7',
      navBg: '#ffffff'
    },
    amber: {
      name: '琥珀橙', emoji: '🍂', desc: '温暖点缀',
      primary: '#c07830',
      primaryLight: '#d49050',
      gradBtn: '#c07830',
      gradBg: '#faf6f0',
      navBg: '#ffffff'
    }
  },

  onLaunch() {
    const history = wx.getStorageSync('downloadHistory') || []
    this.globalData.history = history
    const savedTheme = wx.getStorageSync('mp_theme') || 'purple'
    this.globalData.theme = savedTheme
    const token = wx.getStorageSync('token') || null
    const userInfo = wx.getStorageSync('userInfo') || null
    this.globalData.token = token
    this.globalData.userInfo = userInfo
  },

  getTheme() {
    return this.themes[this.globalData.theme] || this.themes.purple
  },

  setTheme(key) {
    this.globalData.theme = key
    wx.setStorageSync('mp_theme', key)
  },

  addHistory(item) {
    const history = this.globalData.history
    history.unshift({ ...item, time: Date.now() })
    if (history.length > 50) history.pop()
    this.globalData.history = history
    wx.setStorageSync('downloadHistory', history)
  },

  setUser(token, userInfo) {
    this.globalData.token = token
    this.globalData.userInfo = userInfo
    wx.setStorageSync('token', token)
    wx.setStorageSync('userInfo', userInfo)
  },

  clearUser() {
    this.globalData.token = null
    this.globalData.userInfo = null
    wx.removeStorageSync('token')
    wx.removeStorageSync('userInfo')
  },

  getUser() {
    return this.globalData.userInfo
  }
})
