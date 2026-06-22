import { request } from './request'

function buildQuery(params = {}) {
  const searchParams = new URLSearchParams()

  Object.entries(params).forEach(([key, value]) => {
    if (value === undefined || value === null || value === '') {
      return
    }

    if (Array.isArray(value)) {
      value.forEach(item => {
        if (item !== undefined && item !== null && item !== '') {
          searchParams.append(key, item)
        }
      })
      return
    }

    searchParams.append(key, value)
  })

  const query = searchParams.toString()
  return query ? `?${query}` : ''
}

export function getQuestionPage(params) {
  return request(`/api/questions/list${buildQuery(params)}`)
}

export function getQuestionById(id) {
  return request(`/api/questions/${id}`)
}

export function addQuestion(data) {
  return request('/api/questions', { method: 'POST', body: data })
}

export function updateQuestion(data) {
  return request('/api/questions', { method: 'PUT', body: data })
}

export function deleteQuestion(id) {
  return request(`/api/questions/${id}`, { method: 'DELETE' })
}

export function getPopularQuestions(size = 6) {
  return request(`/api/questions/popular${buildQuery({ size })}`)
}


/**
 * 下载题目 Excel 导入模板。
 *
 * 后端接口：GET /api/questions/template
 * 这里使用 responseType: 'blob' 接收 Excel 文件流；
 * rawResponse: true 用于拿到响应头中的文件名。
 */
export function downloadQuestionTemplate() {
  return request('/api/questions/template', {
    responseType: 'blob',
    rawResponse: true
  })
}

/**
 * 批量导入题目 Excel。
 *
 * 后端接口：POST /api/questions/import
 * 后端参数名固定为 file，所以 FormData 里也必须使用 file。
 */
export function importQuestionExcel(file) {
  const formData = new FormData()
  formData.append('file', file)

  return request('/api/questions/import', {
    method: 'POST',
    body: formData,
    // 导入大 Excel 可能比较慢，这里适当放宽超时时间。
    timeout: 60000
  })
}

/**
 * 批量导出题目 Excel。
 *
 * 后端接口：GET /api/questions/export
 * params 会被 axios 自动拼接成查询字符串，例如：
 * /api/questions/export?type=CHOICE&difficulty=EASY&keyword=Java
 */
export function exportQuestionExcel(params = {}) {
  return request('/api/questions/export', {
    params,
    responseType: 'blob',
    rawResponse: true,
    timeout: 60000
  })
}

/**
 * AI 批量生成题目。
 *
 * 后端接口：POST /api/questions/ai/generate
 * 返回值：AI 生成的题目预览列表，前端确认后再逐条调用新增题目接口入库。
 */
export function generateAiQuestions(data) {
  return request('/api/questions/ai/generate', {
    method: 'POST',
    body: data,
    timeout: 120000
  })
}
