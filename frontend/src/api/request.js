const SUCCESS_CODE = 200

/**
 * 统一请求工具，当前项目使用 fetch，不依赖 axios。
 *
 * 开发环境下请求 /api 会被 vite.config.js 代理到 Spring Boot：
 * http://localhost:8080
 */
export async function request(url, options = {}) {
  const config = { ...options }
  const headers = { ...(options.headers || {}) }
  const body = config.body
  const isFormData = typeof FormData !== 'undefined' && body instanceof FormData

  // 普通对象自动转 JSON；FormData 不能手动设置 Content-Type，否则 multipart boundary 会丢。
  if (body !== undefined && body !== null && !isFormData && typeof body !== 'string') {
    config.body = JSON.stringify(body)
    headers['Content-Type'] = headers['Content-Type'] || 'application/json'
  }

  if (body === undefined || body === null) {
    delete config.body
  }

  const response = await fetch(url, {
    ...config,
    headers
  })

  const text = await response.text()
  let result = null

  if (text) {
    try {
      result = JSON.parse(text)
    } catch (error) {
      result = text
    }
  }

  if (!response.ok) {
    const message = result?.message || result?.msg || `HTTP ${response.status}：${response.statusText || '接口请求失败'}`
    throw new Error(message)
  }

  // 兼容后端统一返回格式：{ code, message, data, timestamp }
  if (result && typeof result === 'object' && Object.prototype.hasOwnProperty.call(result, 'code')) {
    if (Number(result.code) !== SUCCESS_CODE) {
      throw new Error(result.message || result.msg || '接口请求失败')
    }
    return result.data
  }

  return result
}
