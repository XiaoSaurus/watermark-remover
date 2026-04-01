const api = require('../../utils/api.js')
const app = getApp()

Page({
  data: {
    tabIndex: 0,
    phone: '',
    code: '',
    focusField: '',
    smsLoading: false,
    smsCooldown: 0,
    loading: false
  },

  onLoad() {},

  goBack() {
    wx.navigateBack()
  },

  switchTab(e) {
    this.setData({ tabIndex: parseInt(e.currentTarget.dataset.index) })
  },

  onPhoneInput(e) { this.setData({ phone: e.detail.value }) },
  onCodeInput(e) { this.setData({ code: e.detail.value }) },

  onFieldFocus(e) {
    this.setData({ focusField: e.currentTarget.dataset.field })
  },

  onFieldBlur() {
    this.setData({ focusField: '' })
  },

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
    this.setData({ loading: true })
    wx.showLoading({ title: '登录中...' })
    try {
      const res = await api.login('phone', { phone, code })
      app.setUser(res.token, res)
      wx.showToast({ title: '登录成功', icon: 'success' })
      setTimeout(() => wx.navigateBack(), 1000)
    } catch (e) {
      wx.showToast({ title: e.message || '登录失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
      wx.hideLoading()
    }
  },

  async onGetPhoneNumber(e) {
    if (!e.detail.code) {
      wx.showToast({ title: '授权失败', icon: 'none' }); return
    }
    this.setData({ loading: true })
    wx.showLoading({ title: '登录中...' })
    try {
      const res = await api.login('wechat_miniprogram', { code: e.detail.code })
      app.setUser(res.token, res)
      wx.showToast({ title: '登录成功', icon: 'success' })
      setTimeout(() => wx.navigateBack(), 1000)
    } catch (e) {
      wx.showToast({ title: e.message || '登录失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
      wx.hideLoading()
    }
  },

  async handleTouristLogin() {
    this.setData({ loading: true })
    wx.showLoading({ title: '登录中...' })
    try {
      const res = await api.login('tourist', {})
      app.setUser(res.token, res)
      wx.showToast({ title: '登录成功', icon: 'success' })
      setTimeout(() => wx.navigateBack(), 1000)
    } catch (e) {
      wx.showToast({ title: e.message || '登录失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
      wx.hideLoading()
    }
  },

  goRegister() { wx.navigateTo({ url: '/pages/register/register' }) },
  goReset() { wx.navigateTo({ url: '/pages/reset/reset' }) }
})