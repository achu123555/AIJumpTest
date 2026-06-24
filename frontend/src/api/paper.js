import { request } from './request'

function buildQuery(params = {}) {
  const searchParams = new URLSearchParams()

  Object.entries(params).forEach(([key, value]) => {
    if (value === undefined || value === null || value === '') return
    searchParams.append(key, value)
  })

  const query = searchParams.toString()
  return query ? `?${query}` : ''
}

/**
 * 查询试卷列表。
 *
 * 后端接口：GET /api/papers/list
 * 支持参数：name、status。
 */
export function getPaperList(params = {}) {
  return request(`/api/papers/list${buildQuery(params)}`)
}

/**
 * 查询试卷详情，包含题目、答案、选项、realScore。
 *
 * 后端接口：GET /api/papers/{id}
 */
export function getPaperDetail(id) {
  return request(`/api/papers/${id}`)
}

/**
 * 手动创建试卷。
 *
 * 后端接口：POST /api/papers
 * 请求体：
 * {
 *   name: string,
 *   description: string,
 *   duration: number,
 *   questions: { [questionId]: score }
 * }
 */
export function createPaper(data) {
  return request('/api/papers', {
    method: 'POST',
    body: data
  })
}

/**
 * 编辑试卷。
 *
 * 后端接口：PUT /api/papers/{id}
 */
export function updatePaper(id, data) {
  return request(`/api/papers/${id}`, {
    method: 'PUT',
    body: data
  })
}

/**
 * 切换试卷状态。
 *
 * 后端接口：PUT /api/papers/status/{id}?status=PUBLISHED
 */
export function switchPaperStatus(id, status) {
  return request(`/api/papers/status/${id}${buildQuery({ status })}`, {
    method: 'PUT'
  })
}

/**
 * 删除试卷。
 *
 * 后端接口：DELETE /api/papers/{id}
 */
export function deletePaper(id) {
  return request(`/api/papers/${id}`, {
    method: 'DELETE'
  })
}

/**
 * 智能组卷。
 *
 * 后端接口：POST /api/papers/intelligent
 * 注意：你当前后端 intelligentCreatePaper 还没有完整实现，所以前端暂时只保留 API 封装。
 */
export function intelligentCreatePaper(data) {
  return request('/api/papers/intelligent', {
    method: 'POST',
    body: data,
    timeout: 120000
  })
}
