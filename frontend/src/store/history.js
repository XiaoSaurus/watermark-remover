// 历史记录管理（localStorage 持久化）
const STORAGE_KEY = 'wm_download_history'
const MAX_ITEMS = 50

export function getHistory() {
  try {
    return JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]')
  } catch {
    return []
  }
}

export function addHistory(item) {
  const list = getHistory()
  list.unshift({ ...item, time: Date.now() })
  if (list.length > MAX_ITEMS) list.pop()
  localStorage.setItem(STORAGE_KEY, JSON.stringify(list))
}

export function clearHistory() {
  localStorage.removeItem(STORAGE_KEY)
}
