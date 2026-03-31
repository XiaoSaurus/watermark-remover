// pages/index/index.js
const { parseVideo, getDownloadUrl } = require('../../utils/api')
const app = getApp()

Page({
  data: {
    inputUrl: '',
    loading: false,
    result: null,
    downloadingIndex: -1,
    platformLabel: '',
    platforms: [
      { name: '抖音', icon: '🎵' },
      { name: '快手', icon: '⚡' },
      { name: 'B站', icon: '📺' },
      { name: '微博', icon: '🌐' },
      { name: '小红书', icon: '📕' }
    ],
    steps: [
      '打开抖音/快手/B站等 App，找到想下载的视频',
      '点击「分享」→「复制链接」或「复制文案」',
      '粘贴到上方输入框，点击「立即解析」',
      '选择清晰度，点击「下载」保存到相册'
    ]
  },

  onInput(e) {
    this.setData({ inputUrl: e.detail.value })
  },

  clearInput() {
    this.setData({ inputUrl: '', result: null })
  },

  async handleParse() {
    const url = this.data.inputUrl.trim()
    if (!url) return

    this.setData({ loading: true, result: null })

    try {
      const res = await parseVideo(url)
      if (res.code === 200) {
        const platformMap = {
          douyin: '🎵 抖音',
          kuaishou: '⚡ 快手',
          bilibili: '📺 B站',
          weibo: '🌐 微博',
          xiaohongshu: '📕 小红书'
        }
        this.setData({
          result: res.data,
          platformLabel: platformMap[res.data.platform] || res.data.platform
        })
        wx.showToast({ title: '解析成功', icon: 'success' })
      } else {
        wx.showToast({ title: res.message || '解析失败', icon: 'none', duration: 3000 })
      }
    } catch (err) {
      wx.showToast({
        title: err.message || '网络错误，请检查服务器地址',
        icon: 'none',
        duration: 3000
      })
    } finally {
      this.setData({ loading: false })
    }
  },

  downloadVideo(e) {
    const { url, quality, index } = e.currentTarget.dataset
    const filename = `video_${quality}_${Date.now()}.mp4`
    const downloadUrl = getDownloadUrl(url, filename)

    this.setData({ downloadingIndex: index })

    wx.showLoading({ title: '下载中...', mask: true })

    wx.downloadFile({
      url: downloadUrl,
      success: (res) => {
        if (res.statusCode === 200) {
          // 保存到相册
          wx.saveVideoToPhotosAlbum({
            filePath: res.tempFilePath,
            success: () => {
              wx.showToast({ title: '已保存到相册', icon: 'success' })
              // 记录历史
              app.addHistory({
                title: this.data.result.title,
                platform: this.data.result.platform,
                cover: this.data.result.cover,
                quality
              })
            },
            fail: (err) => {
              if (err.errMsg.includes('auth deny')) {
                wx.showModal({
                  title: '需要授权',
                  content: '请允许访问相册权限以保存视频',
                  confirmText: '去授权',
                  success: (r) => {
                    if (r.confirm) wx.openSetting()
                  }
                })
              } else {
                wx.showToast({ title: '保存失败: ' + err.errMsg, icon: 'none' })
              }
            }
          })
        } else {
          wx.showToast({ title: '下载失败: HTTP ' + res.statusCode, icon: 'none' })
        }
      },
      fail: (err) => {
        wx.showToast({ title: '下载失败: ' + err.errMsg, icon: 'none', duration: 3000 })
      },
      complete: () => {
        wx.hideLoading()
        this.setData({ downloadingIndex: -1 })
      }
    })
  },

  copyUrl(e) {
    const { url } = e.currentTarget.dataset
    wx.setClipboardData({
      data: url,
      success: () => wx.showToast({ title: '链接已复制', icon: 'success' })
    })
  }
})