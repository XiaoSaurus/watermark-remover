// utils/api.js - 封装所有 API 请求
const app = getApp()

/**
 * 发起请求
 */
function request(method, path, data) {
  return new Promise((resolve, reject) => {
    wx.request({
      url: app.globalData.apiBase + path,
      method,
      data,
      header: {
        'Content-Type': 'application/json'
      },
      success(res) {
        if (res.statusCode === 200) {
          resolve(res.data)
        } else {
          reject(new Error(`HTTP ${res.statusCode}`))
        }
      },
      fail(err) {
        reject(new Error(err.errMsg || '网络请求失败'))
      }
    })
  })
}

/**
 * 解析视频链接
 * @param {string} url 分享链接或文案
 */
function parseVideo(url) {
  return request('POST', '/api/video/parse', { url })
}

/**
 * 获取代理下载地址（小程序用 wx.downloadFile 下载）
 * @param {string} videoUrl 视频地址
 * @param {string} filename 文件名
 */
function getDownloadUrl(videoUrl, filename) {
  const encoded = encodeURIComponent(videoUrl)
  const encodedName = encodeURIComponent(filename)
  return `${app.globalData.apiBase}/api/video/download?url=${encoded}&filename=${encodedName}`
}

module.exports = { parseVideo, getDownloadUrl }