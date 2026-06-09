const STORAGE_KEY = 'exam_admin_banners'

export function loadBanners() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    return raw ? JSON.parse(raw) : []
  } catch (e) {
    console.warn('读取轮播图缓存失败：', e)
    return []
  }
}

export function saveBanners(list) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(list))
}

export function clearBanners() {
  localStorage.removeItem(STORAGE_KEY)
}
