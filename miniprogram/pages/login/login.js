const api = require('../../utils/api.js')
const app = getApp()

Page({
  data: {
    loginType: 'phone',
    phone: '',
    code: '',
    smsLoading: false,
    smsCooldown: 0,
  },

  onLoad() {},

  selectType(e) {
    this.setData({ loginType: e.currentTarget.dataset.type })
  },

  onPhoneInput(e) { this.setData({ phone: e.detail.value }) },
  onCodeInput(e) { this.setData({ code: e.detail.value }) },

  async sendSms() {
    const { phone } = this.data
    if (!phone || phone.length !== 11) {
      wx.showToast({ title: '请输入正确手机号', icon: 'none' }); return
    }
    this.setData({ smsLoading: true })
    try {
      await api.sendSms(phone, 'login')
      wx.showToast({ title: '验证码已发送', icon: 'success' })
      this.startCooldown()
    } catch (e) {
      wx.showToast({ title: e.message || '发送失败', icon: 'none' })
    } finally {
      this.setData({ smsLoading: false })
    }
  },

  startCooldown() {
    let sec = 60
    const timer = setInterval(() => {
      sec--
      this.setData({ smsCooldown: sec })
      if (sec <= 0) clearInterval(timer)
    }, 1000)
  },

  async handleLogin() {
    const { phone, code } = this.data
    if (!phone || !code) {
      wx.showToast({ title: '请输入手机号和验证码', icon: 'none' }); return
    }
    wx.showLoading({ title: '登录中...' })
    try {
      const res = await api.login('phone', { phone, code })
      app.setUser(res.token, res)
      wx.showToast({ title: '登录成功', icon: 'success' })
      setTimeout(() => wx.navigateBack(), 1000)
    } catch (e) {
      wx.showToast({ title: e.message || '登录失败', icon: 'none' })
    } finally {
      wx.hideLoading()
    }
  },

  async onGetPhoneNumber(e) {
    if (!e.detail.code) return
    wx.showLoading({ title: '登录中...' })
    try {
      const res = await api.login('wechat_miniprogram', { code: e.detail.code })
      app.setUser(res.token, res)
      wx.showToast({ title: '登录成功', icon: 'success' })
      setTimeout(() => wx.navigateBack(), 1000)
    } catch (e) {
      wx.showToast({ title: e.message || '登录失败', icon: 'none' })
    } finally {
      wx.hideLoading()
    }
  },

  async handleWechatLogin() {
    wx.showLoading({ title: '获取中...' })
    try {
      const res = await api.login('wechat_miniprogram', {})
      app.setUser(res.token, res)
      wx.showToast({ title: '登录成功', icon: 'success' })
      setTimeout(() => wx.navigateBack(), 1000)
    } catch (e) {
      wx.showToast({ title: e.message || '登录失败', icon: 'none' })
    } finally {
      wx.hideLoading()
    }
  },

  async handleTouristLogin() {
    wx.showLoading({ title: '登录中...' })
    try {
      const res = await api.login('tourist', {})
      app.setUser(res.token, res)
      wx.showToast({ title: '登录成功', icon: 'success' })
      setTimeout(() => wx.navigateBack(), 1000)
    } catch (e) {
      wx.showToast({ title: e.message || '登录失败', icon: 'none' })
    } finally {
      wx.hideLoading()
    }
  },

  goRegister() { wx.navigateTo({ url: '/pages/register/register' }) },
  goReset() { wx.navigateTo({ url: '/pages/reset/reset' }) }
})
