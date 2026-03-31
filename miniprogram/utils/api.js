const app = getApp()

function request(method, path, data) {
  return new Promise((resolve, reject) => {
    wx.request({
      url: app.globalData.apiBase + path,
      method, data,
      header: { 'Content-Type': 'application/json' },
      success(res) {
        if (res.statusCode === 200) resolve(res.data)
        else reject(new Error(`HTTP ${res.statusCode}`))
      },
      fail(err) { reject(new Error(err.errMsg || '网络请求失败')) }
    })
  })
}

function parseVideo(url) {
  return request('POST', '/api/video/parse', { url })
}

function getDownloadUrl(videoUrl, filename) {
  const encoded = encodeURIComponent(videoUrl)
  const encodedName = encodeURIComponent(filename)
  return `${app.globalData.apiBase}/api/video/download?url=${encoded}&filename=${encodedName}`
}

// 历史记录 API
function saveHistory(item) {
  return request('POST', '/api/history', {
    platform: item.platform,
    title: item.title,
    cover: item.cover,
    quality: item.quality,
    videoUrl: item.url,
    shareUrl: item.shareUrl || '',
    status: item.status,
    errMsg: item.errMsg || '',
    client: 'miniprogram'
  })
}

function getHistory(page, size) {
  return request('GET', `/api/history?page=${page || 0}&size=${size || 20}`)
}

function deleteHistory(id) {
  return request('DELETE', `/api/history/${id}`)
}

function clearHistoryApi() {
  return request('DELETE', '/api/history')
}

module.exports = { parseVideo, getDownloadUrl, saveHistory, getHistory, deleteHistory, clearHistoryApi }
