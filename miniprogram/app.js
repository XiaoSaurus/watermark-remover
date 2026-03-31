App({
  globalData: {
    apiBase: 'http://10.16.247.27:8080',
    history: [],
    theme: 'purple'   // 当前主题
  },

  // 主题配置
  themes: {
    purple: {
      name: '紫罗兰', emoji: '💜',
      primary: '#6c63ff', primaryLight: '#8b85ff',
      gradBtn: 'linear-gradient(135deg,#6c63ff,#764ba2)',
      gradBg:  'linear-gradient(135deg,#667eea 0%,#764ba2 100%)',
      navBg: '#6c63ff'
    },
    ocean: {
      name: '深海蓝', emoji: '🌊',
      primary: '#0ea5e9', primaryLight: '#38bdf8',
      gradBtn: 'linear-gradient(135deg,#0ea5e9,#0284c7)',
      gradBg:  'linear-gradient(135deg,#0ea5e9 0%,#0369a1 100%)',
      navBg: '#0ea5e9'
    },
    rose: {
      name: '玫瑰红', emoji: '🌹',
      primary: '#f43f5e', primaryLight: '#fb7185',
      gradBtn: 'linear-gradient(135deg,#f43f5e,#e11d48)',
      gradBg:  'linear-gradient(135deg,#f43f5e 0%,#be123c 100%)',
      navBg: '#f43f5e'
    },
    forest: {
      name: '森林绿', emoji: '🌿',
      primary: '#10b981', primaryLight: '#34d399',
      gradBtn: 'linear-gradient(135deg,#10b981,#059669)',
      gradBg:  'linear-gradient(135deg,#10b981 0%,#047857 100%)',
      navBg: '#10b981'
    },
    sunset: {
      name: '日落橙', emoji: '🌅',
      primary: '#f97316', primaryLight: '#fb923c',
      gradBtn: 'linear-gradient(135deg,#f97316,#ea580c)',
      gradBg:  'linear-gradient(135deg,#f97316 0%,#c2410c 100%)',
      navBg: '#f97316'
    }
  },

  onLaunch() {
    const history = wx.getStorageSync('downloadHistory') || []
    this.globalData.history = history
    const savedTheme = wx.getStorageSync('mp_theme') || 'purple'
    this.globalData.theme = savedTheme
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
  }
})
