const app = getApp()

Page({
  data: {
    userInfo: null,
    loginTypeLabel: ''
  },

  onShow() {
    this.loadUserProfile()
  },

  loadUserProfile() {
    const userInfo = app.globalData.userInfo
    if (!userInfo) {
      this.setData({ userInfo: null })
      return
    }

    this.setData({ userInfo })
    
    const map = { phone: '手机号登录', wechat: '微信登录', tourist: '游客登录' }
    this.setData({ loginTypeLabel: map[userInfo.loginType] || userInfo.loginType })
  },

  goLogin() { 
    wx.navigateTo({ url: '/pages/login/login' }) 
  },

  goEditProfile() {
    wx.navigateTo({ url: '/pages/edit-profile/edit-profile' })
  },

  goChangePassword() {
    wx.navigateTo({ url: '/pages/change-password/change-password' })
  },

  goBindPhone() {
    wx.navigateTo({ url: '/pages/bind-phone/bind-phone' })
  },

  openVip() {
    wx.showToast({ title: 'VIP功能开发中', icon: 'none' })
  },

  handleLogout() {
    wx.showModal({
      title: '提示',
      content: '确定要退出登录吗？',
      success: res => {
        if (res.confirm) {
          app.clearUser()
          this.setData({ userInfo: null })
          wx.showToast({ title: '已退出', icon: 'success' })
          wx.navigateTo({ url: '/pages/login/login' })
        }
      }
    })
  }
})
