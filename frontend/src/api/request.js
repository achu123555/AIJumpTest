import axios from 'axios'

const SUCCESS_CODE = 200

/**
 * 统一请求工具，项目已切换为 axios。
 *
 * 开发环境下请求 /api 会被 vite.config.js 代理到 Spring Boot：
 * http://localhost:8080
 */
const service = axios.create({
  baseURL: '',
  timeout: 15000
})

service.interceptors.request.use(
  config => config,
  error => Promise.reject(error)
)

service.interceptors.response.use(
  response => {
    const result = response.data

    // 兼容后端统一返回格式：{ code, message, data, timestamp }
    if (result && typeof result === 'object' && Object.prototype.hasOwnProperty.call(result, 'code')) {
      if (Number(result.code) !== SUCCESS_CODE) {
        return Promise.reject(new Error(result.message || result.msg || '接口请求失败'))
      }
      return result.data
    }

    return result
  },
  error => {
    const response = error.response
    const data = response?.data
    const message =
      data?.message ||
      data?.msg ||
      error.message ||
      (response ? `HTTP ${response.status}：接口请求失败` : '接口请求失败')

    return Promise.reject(new Error(message))
  }
)

/**
 * 兼容之前旧版风格的调用：
 * request(url, { method: 'POST', body: data })
 *
 * 内部统一转换成 axios 的：
 * service({ url, method, data })
 */
export function request(url, options = {}) {
  const { method = 'GET', body, headers, ...rest } = options
  const config = {
    url,
    method,
    headers: { ...(headers || {}) },
    ...rest
  }

  if (body !== undefined && body !== null) {
    config.data = body
  }

  return service(config)
}

export default service
