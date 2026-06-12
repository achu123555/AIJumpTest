import { request } from './request'

// 管理端：获取全部轮播图
// GET /api/banners/list
export function getBannerList() {
  return request('/api/banners/list')
}

// 学生端：首页获取启用轮播图
// GET /api/banners/active
export function getActiveBanners() {
  return request('/api/banners/active')
}

// 管理端：根据 ID 查询轮播图详情
// GET /api/banners/{id}
export function getBannerById(id) {
  return request(`/api/banners/${id}`)
}

// 管理端：上传轮播图图片到 OSS
// POST /api/banners/upload-image
export function uploadBannerImage(file) {
  const formData = new FormData()
  formData.append('file', file)

  return request('/api/banners/upload-image', {
    method: 'POST',
    body: formData
  })
}

// 管理端：新增轮播图
// POST /api/banners/add
export function addBanner(data) {
  return request('/api/banners/add', {
    method: 'POST',
    body: data
  })
}

// 管理端：更新轮播图
// PUT /api/banners/update
export function updateBanner(data) {
  return request('/api/banners/update', {
    method: 'PUT',
    body: data
  })
}

// 管理端：删除轮播图
// DELETE /api/banners/delete/{id}
export function deleteBanner(id) {
  return request(`/api/banners/delete/${id}`, {
    method: 'DELETE'
  })
}

// 管理端：切换轮播图启用 / 禁用状态
// PUT /api/banners/toggle/{id}?isActive=true
export function toggleBannerStatus(id, isActive) {
  const params = new URLSearchParams()
  params.append('isActive', String(Boolean(isActive)))

  return request(`/api/banners/toggle/${id}?${params.toString()}`, {
    method: 'PUT'
  })
}
