const app = getApp()

Page({
  data: {
    loading: false,
    countdown: 0,
    form: {
      phone: '',
      code: ''
    }
  },

  onPhoneInput(e) {
    this.setData({ 'form.phone': e.detail.value })
  },

  onCodeInput(e) {
    this.setData({ 'form.code': e.detail.value })
  },

  sendCode() {
    const phone = this.data.form.phone
    if (!phone || !/^1[3-9]\d{9}$/.test(phone)) {
      wx.showToast({ title: '请输入正确的手机号', icon: 'none' })
      return
    }

    wx.request({
      url: app.globalData.apiUrl + '/auth/sms/send',
      method: 'POST',
      header: { 'Content-Type': 'application/json' },
      data: { phone, scene: 'bind' },
      success: (res) => {
        if (res.data.code === 200) {
          wx.showToast({ title: '验证码已发送', icon: 'success' })
          this.startCountdown()
        } else {
          wx.showToast({ title: res.data.message || '发送失败', icon: 'none' })
        }
      },
      fail: () => {
        wx.showToast({ title: '发送失败', icon: 'error' })
      }
    })
  },

  startCountdown() {
    let countdown = 60
    this.setData({ countdown })
    const timer = setInterval(() => {
      countdown--
      this.setData({ countdown })
      if (countdown <= 0) clearInterval(timer)
    }, 1000)
  },

  submitBind() {
    const { phone, code } = this.data.form

    if (!phone || !/^1[3-9]\d{9}$/.test(phone)) {
      wx.showToast({ title: '请输入正确的手机号', icon: 'none' })
      return
    }
    if (!code) {
      wx.showToast({ title: '请输入验证码', icon: 'none' })
      return
    }

    this.setData({ loading: true })
    wx.showLoading({ title: '绑定中...', mask: true })

    wx.request({
      url: app.globalData.apiUrl + '/auth/bind-phone',
      method: 'POST',
      header: {
        'Authorization': 'Bearer ' + app.globalData.token,
        'Content-Type': 'application/json'
      },
      data: this.data.form,
      success: (res) => {
        if (res.data.code === 200) {
          wx.showToast({ title: '绑定成功', icon: 'success' })
          setTimeout(() => wx.navigateBack(), 1000)
        } else {
          wx.showToast({ title: res.data.message || '绑定失败', icon: 'none' })
        }
      },
      fail: () => {
        wx.showToast({ title: '绑定失败', icon: 'error' })
      },
      complete: () => {
        wx.hideLoading()
        this.setData({ loading: false })
      }
    })
  }
})
