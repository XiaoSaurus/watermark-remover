const app = getApp()

Page({
  data: {
    saving: false,
    userInfo: null,
    form: {
      nickname: '',
      bio: '',
      gender: 0,
      birthday: ''
    },
    today: ''
  },

  onLoad() {
    const userInfo = app.globalData.userInfo
    this.setData({ 
      userInfo,
      form: {
        nickname: userInfo.nickname || '',
        bio: userInfo.bio || '',
        gender: userInfo.gender || 0,
        birthday: userInfo.birthday || ''
      },
      today: new Date().toISOString().split('T')[0]
    })
  },

  goBack() {
    wx.navigateBack()
  },

  onNicknameInput(e) {
    this.setData({ 'form.nickname': e.detail.value })
  },

  onBioInput(e) {
    this.setData({ 'form.bio': e.detail.value })
  },

  onGenderChange(e) {
    this.setData({ 'form.gender': parseInt(e.currentTarget.dataset.gender) })
  },

  onBirthdayChange(e) {
    this.setData({ 'form.birthday': e.detail.value })
  },

  chooseAvatar() {
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const filePath = res.tempFiles[0].tempFilePath
        this.uploadAvatar(filePath)
      }
    })
  },

  uploadAvatar(filePath) {
    wx.showLoading({ title: '上传中...', mask: true })
    wx.uploadFile({
      url: app.globalData.apiUrl + '/avatar/upload',
      filePath: filePath,
      name: 'file',
      header: {
        'Authorization': 'Bearer ' + app.globalData.token
      },
      success: (res) => {
        wx.hideLoading()
        const data = JSON.parse(res.data)
        if (data.code === 200) {
          this.setData({ 'form.avatar': data.data.url })
          wx.showToast({ title: '头像已更新', icon: 'success' })
        } else {
          wx.showToast({ title: data.message || '上传失败', icon: 'none' })
        }
      },
      fail: () => {
        wx.hideLoading()
        wx.showToast({ title: '上传失败', icon: 'error' })
      }
    })
  },

  saveProfile() {
    const { form } = this.data
    if (!form.nickname) {
      wx.showToast({ title: '请输入昵称', icon: 'none' })
      return
    }

    this.setData({ saving: true })
    wx.showLoading({ title: '保存中...', mask: true })
    
    wx.request({
      url: app.globalData.apiUrl + '/auth/profile',
      method: 'PUT',
      header: {
        'Authorization': 'Bearer ' + app.globalData.token,
        'Content-Type': 'application/json'
      },
      data: form,
      success: (res) => {
        if (res.data.code === 200) {
          // 更新全局用户信息
          const userInfo = { ...this.data.userInfo, ...form }
          app.globalData.userInfo = userInfo
          wx.showToast({ title: '保存成功', icon: 'success' })
          setTimeout(() => wx.navigateBack(), 1000)
        } else {
          wx.showToast({ title: res.data.message || '保存失败', icon: 'none' })
        }
      },
      fail: () => {
        wx.showToast({ title: '保存失败', icon: 'error' })
      },
      complete: () => {
        wx.hideLoading()
        this.setData({ saving: false })
      }
    })
  }
})
