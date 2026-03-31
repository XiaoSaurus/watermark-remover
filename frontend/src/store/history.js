import { parseHistoryApi, historyApi } from '@/api/watermark'

// 解析历史
export async function saveParse(item) {
  return parseHistoryApi.save({ ...item, client: 'web' })
}
export async function getParseHistory(page=0, size=20) {
  return parseHistoryApi.list(page, size)
}
export async function removeParseHistory(id) {
  return parseHistoryApi.remove(id)
}
export async function clearParseHistory() {
  return parseHistoryApi.clear()
}

// 下载历史
export async function saveDownload(item) {
  return historyApi.save({ ...item, client: 'web' })
}
export async function getDownloadHistory(page=0, size=20) {
  return historyApi.list(page, size)
}
export async function removeDownloadHistory(id) {
  return historyApi.remove(id)
}
export async function clearDownloadHistory() {
  return historyApi.clear()
}
