import { request } from './request'

// 管理端：查询所有分类，平铺列表，无 children
// GET /api/categories
export function getCategoryList() {
  return request('/api/categories')
}

// 管理端：查询分类树，包含 children
// GET /api/categories/tree
export function getCategoryTree() {
  return request('/api/categories/tree')
}

// 管理端：新增子分类
// POST /api/categories
export function addCategory(data) {
  return request('/api/categories', {
    method: 'POST',
    body: data
  })
}

// 管理端：更新子分类
// PUT /api/categories
export function updateCategory(data) {
  return request('/api/categories', {
    method: 'PUT',
    body: data
  })
}

// 管理端：删除子分类
// DELETE /api/categories/{id}
export function deleteCategory(id) {
  return request(`/api/categories/${id}`, {
    method: 'DELETE'
  })
}
