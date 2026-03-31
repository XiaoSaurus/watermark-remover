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

function parseVideo(url) { return request('POST', '/api/video/parse', { url }) }

function getDownloadUrl(videoUrl, filename) {
  return `${app.globalData.apiBase}/api/video/download?url=${encodeURIComponent(videoUrl)}&filename=${encodeURIComponent(filename)}`
}

// 解析历史
function saveParseHistory(item) { return request('POST', '/api/parse-history', { ...item, client: 'miniprogram' }) }
function getParseHistory(page, size) { return request('GET', `/api/parse-history?page=${page||0}&size=${size||20}`) }
function deleteParseHistory(id) { return request('DELETE', `/api/parse-history/${id}`) }
function clearParseHistoryApi() { return request('DELETE', '/api/parse-history') }

// 下载历史
function saveDownloadHistory(item) { return request('POST', '/api/history', { ...item, client: 'miniprogram' }) }
function getDownloadHistory(page, size) { return request('GET', `/api/history?page=${page||0}&size=${size||20}`) }
function deleteDownloadHistory(id) { return request('DELETE', `/api/history/${id}`) }
function clearDownloadHistoryApi() { return request('DELETE', '/api/history') }

module.exports = {
  parseVideo, getDownloadUrl,
  saveParseHistory, getParseHistory, deleteParseHistory, clearParseHistoryApi,
  saveDownloadHistory, getDownloadHistory, deleteDownloadHistory, clearDownloadHistoryApi
}
