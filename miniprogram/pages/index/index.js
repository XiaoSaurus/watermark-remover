// pages/index/index.js
const { parseVideo, getDownloadUrl } = require('../../utils/api')
const app = getApp()

Page({
  data: {
    inputUrl: '',
    loading: false,
    result: null,
    platformLabel: '',
    // 下载进度相关
    downloadingIndex: -1,
    downloadProgress: 0,
    downloadStatus: '',   // 'downloading' | 'saving' | ''
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
          douyin: '🎵 抖音', kuaishou: '⚡ 快手',
          bilibili: '📺 B站', weibo: '🌐 微博', xiaohongshu: '📕 小红书'
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
      wx.showToast({ title: err.message || '网络错误，请检查服务器地址', icon: 'none', duration: 3000 })
    } finally {
      this.setData({ loading: false })
    }
  },

  downloadVideo(e) {
    const { url, quality, index } = e.currentTarget.dataset
    if (this.data.downloadingIndex !== -1) {
      wx.showToast({ title: '请等待当前下载完成', icon: 'none' })
      return
    }

    const filename = `video_${quality}_${Date.now()}.mp4`
    const downloadUrl = getDownloadUrl(url, filename)
    const self = this

    self.setData({
      downloadingIndex: index,
      downloadProgress: 0,
      downloadStatus: 'downloading'
    })

    const task = wx.downloadFile({
      url: downloadUrl,
      success(res) {
        if (res.statusCode === 200) {
          self.setData({ downloadStatus: 'saving', downloadProgress: 100 })
          wx.saveVideoToPhotosAlbum({
            filePath: res.tempFilePath,
            success() {
              wx.showToast({ title: '已保存到相册 ✅', icon: 'success', duration: 2000 })
              // ✅ 写入历史记录
              app.addHistory({
                title: self.data.result.title,
                platform: self.data.result.platform,
                cover: self.data.result.cover,
                quality
              })
              self._resetDownload()
            },
            fail(err) {
              self._resetDownload()
              if (err.errMsg && err.errMsg.includes('auth deny')) {
                wx.showModal({
                  title: '需要相册权限',
                  content: '请授权访问相册以保存视频',
                  confirmText: '去授权',
                  success(r) { if (r.confirm) wx.openSetting() }
                })
              } else {
                wx.showToast({ title: '保存失败', icon: 'none', duration: 2000 })
              }
            }
          })
        } else {
          wx.showToast({ title: '下载失败: HTTP ' + res.statusCode, icon: 'none' })
          self._resetDownload()
        }
      },
      fail(err) {
        wx.showToast({ title: '下载失败: ' + (err.errMsg || '未知错误'), icon: 'none', duration: 3000 })
        self._resetDownload()
      }
    })

    // 监听下载进度
    task.onProgressUpdate(function(res) {
      self.setData({
        downloadProgress: res.progress,
        downloadStatus: 'downloading'
      })
    })
  },

  _resetDownload() {
    this.setData({
      downloadingIndex: -1,
      downloadProgress: 0,
      downloadStatus: ''
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
