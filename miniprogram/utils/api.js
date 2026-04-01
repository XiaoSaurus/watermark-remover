const app = getApp()

function request(method, path, data) {
  return new Promise((resolve, reject) => {
    const header = { 'Content-Type': 'application/json' }
    const token = app.globalData.token
    if (token) header['Authorization'] = 'Bearer ' + token
    wx.request({
      url: app.globalData.apiBase + path,
      method, data,
      header,
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

// 用户认证
function sendSms(phone, scene) { return request('POST', '/api/auth/sms/send', { phone, scene }) }

function verifySmsCode(phone, code, scene) { 
  return request('POST', '/api/auth/sms/verify', { phone, code, scene }) 
}

function login(type, data) { return request('POST', '/api/auth/login', { type, ...data }) }

function register(phone, code, password, confirmPassword, username) {
  const data = { type: 'phone', phone, code, password, confirmPassword }
  if (username) data.username = username
  return request('POST', '/api/auth/register', data)
}

function resetPassword(phone, code, newPassword, confirmPassword) {
  return request('POST', '/api/auth/reset-password', { phone, code, newPassword, confirmPassword })
}

function getUserInfo() { return request('GET', '/api/auth/me') }

module.exports = {
  parseVideo, getDownloadUrl,
  saveParseHistory, getParseHistory, deleteParseHistory, clearParseHistoryApi,
  saveDownloadHistory, getDownloadHistory, deleteDownloadHistory, clearDownloadHistoryApi,
  sendSms, verifySmsCode, login, register, resetPassword, getUserInfo
}
