const api = require('../../utils/api.js')

Page({
  data: {
    phone: '',
    code: '',
    password: '',
    confirmPassword: '',
    focusField: '',
    smsLoading: false,
    smsCooldown: 0,
    loading: false
  },

  onLoad() {},

  goBack() {
    wx.navigateBack()
  },

  onPhoneInput(e) { this.setData({ phone: e.detail.value }) },
  onCodeInput(e) { this.setData({ code: e.detail.value }) },
  onPasswordInput(e) { this.setData({ password: e.detail.value }) },
  onConfirmInput(e) { this.setData({ confirmPassword: e.detail.value }) },

  onFieldFocus(e) {
    this.setData({ focusField: e.currentTarget.dataset.field })
  },

  onFieldBlur() {
    this.setData({ focusField: '' })
  },

  async sendSms() {
    if (!this.data.phone || this.data.phone.length !== 11) {
      wx.showToast({ title: '请输入正确手机号', icon: 'none' }); return
    }
    this.setData({ smsLoading: true })
    try {
      await api.sendSms(this.data.phone, 'reset')
      wx.showToast({ title: '验证码已发送', icon: 'success' })
      let sec = 60, t = setInterval(() => { sec--; this.setData({ smsCooldown: sec }); if (sec <= 0) clearInterval(t) }, 1000)
    } catch (e) {
      wx.showToast({ title: e.message || '发送失败', icon: 'none' })
    } finally {
      this.setData({ smsLoading: false })
    }
  },

  async handleReset() {
    const { phone, code, password, confirmPassword } = this.data
    if (!phone || !code || !password) { wx.showToast({ title: '请填写完整', icon: 'none' }); return }
    if (password !== confirmPassword) { wx.showToast({ title: '两次密码不一致', icon: 'none' }); return }
    
    this.setData({ loading: true })
    wx.showLoading({ title: '重置中...' })
    try {
      await api.resetPassword(phone, code, password, confirmPassword)
      wx.showToast({ title: '密码重置成功', icon: 'success' })
      setTimeout(() => wx.navigateBack(), 1000)
    } catch (e) {
      wx.showToast({ title: e.message || '重置失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
      wx.hideLoading()
    }
  },

  goLogin() { wx.navigateBack() }
})