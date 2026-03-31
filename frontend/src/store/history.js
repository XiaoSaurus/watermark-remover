// 历史记录管理 - 统一走后端 MySQL 存储
import { historyApi } from '@/api/watermark'

export async function getHistory(page = 0, size = 20) {
  const res = await historyApi.list(page, size)
  return res.data  // Page 对象
}

export async function addHistory(item) {
  await historyApi.save({
    platform: item.platform,
    title: item.title,
    cover: item.cover,
    quality: item.quality,
    videoUrl: item.url,
    shareUrl: item.shareUrl || '',
    status: item.status,
    errMsg: item.errMsg || '',
    client: 'web'
  })
}

export async function removeHistory(id) {
  await historyApi.remove(id)
}

export async function clearHistory() {
  await historyApi.clear()
}
