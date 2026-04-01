const api = require('../../utils/api.js')
const app = getApp()

Page({
  data: {
    phone: '',
    code: '',
    password: '',
    confirmPassword: '',
    username: '',
    smsCooldown: 0,
    usernameStatus: '',
    usernameMessage: '',
    usernameChecking: false
  },

  onPhoneInput(e) { this.setData({ phone: e.detail.value }) },
  onCodeInput(e) { this.setData({ code: e.detail.value }) },
  onPasswordInput(e) { this.setData({ password: e.detail.value }) },
  onConfirmInput(e) { this.setData({ confirmPassword: e.detail.value }) },

  onUsernameInput(e) {
    const username = e.detail.value
    this.setData({ username, usernameStatus: '', usernameMessage: '' })

    // 防抖检查
    if (this.usernameCheckTimer) clearTimeout(this.usernameCheckTimer)

    if (!username || username.length < 3) {
      this.setData({ usernameStatus: '', usernameMessage: '' })
      return
    }

    // 格式检查
    if (!/^[a-zA-Z0-9_\u4e00-\u9fa5]+$/.test(username)) {
      this.setData({ usernameStatus: 'invalid', usernameMessage: '只能包含字母、数字、下划线和中文' })
      return
    }

    if (username.length < 3 || username.length > 20) {
      this.setData({ usernameStatus: 'invalid', usernameMessage: '用户名长度需在3-20位之间' })
      return
    }

    // 延迟检查
    this.usernameCheckTimer = setTimeout(() => {
      this.checkUsername(username)
    }, 500)
  },

  async checkUsername(username) {
    this.setData({ usernameChecking: true, usernameStatus: '', usernameMessage: '' })

    try {
      const res = await api.checkUsernameExists(username)
      if (res.exists) {
        this.setData({ usernameStatus: 'taken', usernameMessage: '用户名已被占用' })
      } else {
        this.setData({ usernameStatus: 'available', usernameMessage: '用户名可用' })
      }
    } catch (e) {
      this.setData({ usernameStatus: 'invalid', usernameMessage: e.message || '检查失败' })
    } finally {
      this.setData({ usernameChecking: false })
    }
  },

  async sendSms() {
    if (!this.data.phone || this.data.phone.length !== 11) {
      wx.showToast({ title: '请输入正确手机号', icon: 'none' }); return
    }
    try {
      await api.sendSms(this.data.phone, 'register')
      wx.showToast({ title: '已发送', icon: 'success' })
      let sec = 60, t = setInterval(() => { sec--; this.setData({ smsCooldown: sec }); if (sec <= 0) clearInterval(t) }, 1000)
    } catch (e) { wx.showToast({ title: e.message || '发送失败', icon: 'none' }) }
  },

  async handleRegister() {
    const { phone, code, password, confirmPassword, username, usernameStatus } = this.data
    if (!phone || !code || !password) { wx.showToast({ title: '请填写完整', icon: 'none' }); return }
    if (password !== confirmPassword) { wx.showToast({ title: '两次密码不一致', icon: 'none' }); return }

    // 验证用户名
    if (username) {
      if (usernameStatus === 'taken') { wx.showToast({ title: '用户名已被占用', icon: 'none' }); return }
      if (usernameStatus !== 'available') { wx.showToast({ title: '请等待用户名检查完成', icon: 'none' }); return }
    }

    wx.showLoading({ title: '注册中...' })
    try {
      const res = await api.register(phone, code, password, confirmPassword, username)
      app.setUser(res.token, res)
      wx.showToast({ title: '注册成功', icon: 'success' })
      setTimeout(() => wx.navigateBack(), 1000)
    } catch (e) { wx.showToast({ title: e.message || '注册失败', icon: 'none' }) }
    finally { wx.hideLoading() }
  },

  goLogin() { wx.navigateBack() }
})
