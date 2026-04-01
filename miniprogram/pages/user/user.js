const app = getApp()

Page({
  data: {
    userInfo: null,
    loginTypeLabel: '',
    
    // 编辑资料
    showEditModal: false,
    editForm: {
      nickname: '',
      bio: '',
      gender: 0,
      birthday: ''
    },
    saving: false,
    
    // 修改密码
    showPasswordModal: false,
    passwordForm: {
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    },
    changingPassword: false,
    
    // 绑定手机号
    showBindPhoneModal: false,
    bindPhoneForm: {
      phone: '',
      code: ''
    },
    bindingPhone: false,
    sendingCode: false,
    countdown: 0
  },

  onShow() {
    this.loadUserProfile()
  },

  // ===== 加载用户信息 =====
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

  // ===== 导航 =====
  goLogin() { 
    wx.navigateTo({ url: '/pages/login/login' }) 
  },

  chooseAvatar() {
    wx.showActionSheet({
      itemList: ['更换头像'],
      success: () => {
        wx.chooseMedia({
          count: 1,
          mediaType: ['image'],
          sourceType: ['album', 'camera'],
          success: (res) => {
            const file = res.tempFiles[0]
            this.uploadAvatar(file.tempFilePath)
          }
        })
      }
    })
  },

  uploadAvatar(filePath) {
    wx.showLoading({ title: '上传中...' })
    wx.uploadFile({
      url: app.globalData.apiUrl + '/auth/avatar',
      filePath: filePath,
      name: 'file',
      header: {
        'Authorization': 'Bearer ' + app.globalData.token
      },
      success: (res) => {
        wx.hideLoading()
        const data = JSON.parse(res.data)
        if (data.code === 0) {
          this.data.userInfo.avatar = data.data.url
          this.setData({ userInfo: this.data.userInfo })
          app.globalData.userInfo = this.data.userInfo
          wx.showToast({ title: '头像更新成功', icon: 'success' })
        } else {
          wx.showToast({ title: data.message || '上传失败', icon: 'error' })
        }
      },
      fail: () => {
        wx.hideLoading()
        wx.showToast({ title: '上传失败', icon: 'error' })
      }
    })
  },

  // ===== 编辑资料 =====
  editProfile() {
    this.setData({
      editForm: {
        nickname: this.data.userInfo.nickname || '',
        bio: this.data.userInfo.bio || '',
        gender: this.data.userInfo.gender || 0,
        birthday: this.data.userInfo.birthday || ''
      },
      showEditModal: true
    })
  },

  closeEditModal() {
    this.setData({ showEditModal: false })
  },

  onNicknameInput(e) {
    this.setData({ 'editForm.nickname': e.detail.value })
  },

  onBioInput(e) {
    this.setData({ 'editForm.bio': e.detail.value })
  },

  onGenderChange(e) {
    this.setData({ 'editForm.gender': parseInt(e.detail.value) })
  },

  onBirthdayChange(e) {
    this.setData({ 'editForm.birthday': e.detail.value })
  },

  saveProfile() {
    this.setData({ saving: true })
    wx.request({
      url: app.globalData.apiUrl + '/auth/profile',
      method: 'PUT',
      header: {
        'Authorization': 'Bearer ' + app.globalData.token,
        'Content-Type': 'application/json'
      },
      data: this.data.editForm,
      success: (res) => {
        if (res.data.code === 0) {
          this.data.userInfo = Object.assign(this.data.userInfo, this.data.editForm)
          this.setData({ userInfo: this.data.userInfo, showEditModal: false })
          app.globalData.userInfo = this.data.userInfo
          wx.showToast({ title: '保存成功', icon: 'success' })
        } else {
          wx.showToast({ title: res.data.message || '保存失败', icon: 'error' })
        }
      },
      fail: () => {
        wx.showToast({ title: '保存失败', icon: 'error' })
      },
      complete: () => {
        this.setData({ saving: false })
      }
    })
  },

  // ===== 修改密码 =====
  changePassword() {
    this.setData({
      passwordForm: { oldPassword: '', newPassword: '', confirmPassword: '' },
      showPasswordModal: true
    })
  },

  closePasswordModal() {
    this.setData({ showPasswordModal: false })
  },

  onOldPasswordInput(e) {
    this.setData({ 'passwordForm.oldPassword': e.detail.value })
  },

  onNewPasswordInput(e) {
    this.setData({ 'passwordForm.newPassword': e.detail.value })
  },

  onConfirmPasswordInput(e) {
    this.setData({ 'passwordForm.confirmPassword': e.detail.value })
  },

  submitPassword() {
    const { oldPassword, newPassword, confirmPassword } = this.data.passwordForm
    
    if (!oldPassword) {
      wx.showToast({ title: '请输入旧密码', icon: 'error' })
      return
    }
    if (!newPassword) {
      wx.showToast({ title: '请输入新密码', icon: 'error' })
      return
    }
    if (newPassword.length < 6) {
      wx.showToast({ title: '密码长度至少6位', icon: 'error' })
      return
    }
    if (newPassword !== confirmPassword) {
      wx.showToast({ title: '两次密码不一致', icon: 'error' })
      return
    }

    this.setData({ changingPassword: true })
    wx.request({
      url: app.globalData.apiUrl + '/auth/password',
      method: 'PUT',
      header: {
        'Authorization': 'Bearer ' + app.globalData.token,
        'Content-Type': 'application/json'
      },
      data: this.data.passwordForm,
      success: (res) => {
        if (res.data.code === 0) {
          wx.showToast({ title: '密码修改成功，请重新登录', icon: 'success' })
          setTimeout(() => {
            this.handleLogout()
          }, 1500)
        } else {
          wx.showToast({ title: res.data.message || '修改失败', icon: 'error' })
        }
      },
      fail: () => {
        wx.showToast({ title: '修改失败', icon: 'error' })
      },
      complete: () => {
        this.setData({ changingPassword: false })
      }
    })
  },

  // ===== 绑定手机号 =====
  bindPhone() {
    this.setData({
      bindPhoneForm: { phone: '', code: '' },
      showBindPhoneModal: true
    })
  },

  closeBindPhoneModal() {
    this.setData({ showBindPhoneModal: false })
  },

  onPhoneInput(e) {
    this.setData({ 'bindPhoneForm.phone': e.detail.value })
  },

  onCodeInput(e) {
    this.setData({ 'bindPhoneForm.code': e.detail.value })
  },

  sendCode() {
    const phone = this.data.bindPhoneForm.phone
    if (!phone || !/^1[3-9]\d{9}$/.test(phone)) {
      wx.showToast({ title: '请输入正确的手机号', icon: 'error' })
      return
    }

    this.setData({ sendingCode: true })
    wx.request({
      url: app.globalData.apiUrl + '/auth/sms/send',
      method: 'POST',
      header: { 'Content-Type': 'application/json' },
      data: { phone: phone, scene: 'bind' },
      success: (res) => {
        if (res.data.code === 0) {
          wx.showToast({ title: '验证码已发送', icon: 'success' })
          this.startCountdown()
        } else {
          wx.showToast({ title: res.data.message || '发送失败', icon: 'error' })
        }
      },
      fail: () => {
        wx.showToast({ title: '发送失败', icon: 'error' })
      },
      complete: () => {
        this.setData({ sendingCode: false })
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

  submitBindPhone() {
    const { phone, code } = this.data.bindPhoneForm
    
    if (!phone || !/^1[3-9]\d{9}$/.test(phone)) {
      wx.showToast({ title: '请输入正确的手机号', icon: 'error' })
      return
    }
    if (!code) {
      wx.showToast({ title: '请输入验证码', icon: 'error' })
      return
    }

    this.setData({ bindingPhone: true })
    wx.request({
      url: app.globalData.apiUrl + '/auth/bind-phone',
      method: 'POST',
      header: {
        'Authorization': 'Bearer ' + app.globalData.token,
        'Content-Type': 'application/json'
      },
      data: this.data.bindPhoneForm,
      success: (res) => {
        if (res.data.code === 0) {
          wx.showToast({ title: '绑定成功', icon: 'success' })
          this.setData({ showBindPhoneModal: false })
          this.loadUserProfile()
        } else {
          wx.showToast({ title: res.data.message || '绑定失败', icon: 'error' })
        }
      },
      fail: () => {
        wx.showToast({ title: '绑定失败', icon: 'error' })
      },
      complete: () => {
        this.setData({ bindingPhone: false })
      }
    })
  },

  // ===== 退出登录 =====
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
  },

  // ===== 工具函数 =====
  getLevelName(level) {
    const names = ['', '新手', '初级', '中级', '高级', '专家', '大师', '宗师', '王者', '传奇', '至尊']
    return names[level] || '未知'
  },

  getGenderLabel(gender) {
    const map = { 0: '保密', 1: '男', 2: '女' }
    return map[gender] || '未知'
  }
})
