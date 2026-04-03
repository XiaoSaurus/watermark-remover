const app = getApp()

Page({
  data: {
    loading: false,
    form: {
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    }
  },

  onOldPasswordInput(e) {
    this.setData({ 'form.oldPassword': e.detail.value })
  },

  onNewPasswordInput(e) {
    this.setData({ 'form.newPassword': e.detail.value })
  },

  onConfirmPasswordInput(e) {
    this.setData({ 'form.confirmPassword': e.detail.value })
  },

  goBack() {
    wx.navigateBack()
  },

  submitPassword() {
    const { oldPassword, newPassword, confirmPassword } = this.data.form

    if (!oldPassword) {
      wx.showToast({ title: '请输入旧密码', icon: 'none' })
      return
    }
    if (!newPassword) {
      wx.showToast({ title: '请输入新密码', icon: 'none' })
      return
    }
    if (newPassword.length < 6) {
      wx.showToast({ title: '密码长度至少6位', icon: 'none' })
      return
    }
    if (newPassword !== confirmPassword) {
      wx.showToast({ title: '两次密码不一致', icon: 'none' })
      return
    }

    this.setData({ loading: true })
    wx.showLoading({ title: '提交中...', mask: true })

    wx.request({
      url: app.globalData.apiBase + '/auth/password',
      method: 'PUT',
      header: {
        'Authorization': 'Bearer ' + app.globalData.token,
        'Content-Type': 'application/json'
      },
      data: this.data.form,
      success: (res) => {
        if (res.data.code === 200) {
          wx.showToast({ title: '密码修改成功', icon: 'success' })
          setTimeout(() => {
            app.clearUser()
            wx.redirectTo({ url: '/pages/login/login' })
          }, 1500)
        } else {
          wx.showToast({ title: res.data.message || '修改失败', icon: 'none' })
        }
      },
      fail: () => {
        wx.showToast({ title: '修改失败', icon: 'error' })
      },
      complete: () => {
        wx.hideLoading()
        this.setData({ loading: false })
      }
    })
  }
})
