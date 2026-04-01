App({
  globalData: {
    apiBase: 'http://192.168.10.105:8080',
    history: [],
    theme: 'purple',
    userInfo: null,
    token: null
  },

  // 5套柔和主题
  themes: {
    purple: {
      name: '紫罗兰', emoji: '💜', desc: '经典紫色，沉稳优雅',
      primary: '#7c6ff7',
      primaryLight: '#a09af9',
      gradBtn: 'linear-gradient(135deg,#7c6ff7,#9b59b6)',
      gradBg:  'linear-gradient(135deg,#7c6ff7 0%,#9b59b6 100%)',
      navBg:   '#7c6ff7'
    },
    slate: {
      name: '深空蓝', emoji: '🌌', desc: '深邃蓝调，专业沉稳',
      primary: '#4a7fa5',
      primaryLight: '#6b9fc4',
      gradBtn: 'linear-gradient(135deg,#4a7fa5,#2c5f82)',
      gradBg:  'linear-gradient(135deg,#5b8db8 0%,#2c5f82 100%)',
      navBg:   '#4a7fa5'
    },
    sage: {
      name: '抹茶绿', emoji: '🍵', desc: '清新自然，舒适护眼',
      primary: '#4a9e7f',
      primaryLight: '#6bbfa0',
      gradBtn: 'linear-gradient(135deg,#4a9e7f,#2d7a5f)',
      gradBg:  'linear-gradient(135deg,#5aaf8e 0%,#2d7a5f 100%)',
      navBg:   '#4a9e7f'
    },
    rose: {
      name: '玫瑰粉', emoji: '🌸', desc: '温柔粉调，清新甜美',
      primary: '#c0607a',
      primaryLight: '#d4849a',
      gradBtn: 'linear-gradient(135deg,#c0607a,#a04060)',
      gradBg:  'linear-gradient(135deg,#cc7088 0%,#a04060 100%)',
      navBg:   '#c0607a'
    },
    amber: {
      name: '琥珀橙', emoji: '🍂', desc: '温暖橙调，活力秋意',
      primary: '#c07830',
      primaryLight: '#d49050',
      gradBtn: 'linear-gradient(135deg,#c07830,#a05820)',
      gradBg:  'linear-gradient(135deg,#cc8840 0%,#a05820 100%)',
      navBg:   '#c07830'
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
