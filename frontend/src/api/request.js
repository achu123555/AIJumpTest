import axios from 'axios'
import { getAuthUser, clearAuthUser } from '../utils/auth'

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
  config => {
    const user = getAuthUser()
    if (user?.token) {
      // 标准 JWT 写法：Authorization: Bearer token。
      config.headers.Authorization = `Bearer ${user.token}`

      // 兼容旧版后端拦截器，保留 X-Auth-Token 不影响新版本。
      config.headers['X-Auth-Token'] = user.token
      config.headers['X-Auth-Role'] = user.role
    }
    return config
  },
  error => Promise.reject(error)
)

service.interceptors.response.use(
  response => {
    // 文件下载接口需要拿到响应头里的 Content-Disposition，
    // 所以通过 rawResponse=true 让调用方拿完整 axios response。
    if (response.config?.rawResponse) {
      return response
    }

    const result = response.data

    // 兼容后端统一返回格式：{ code, message, data, timestamp }
    if (result && typeof result === 'object' && Object.prototype.hasOwnProperty.call(result, 'code')) {
      if (Number(result.code) !== SUCCESS_CODE) {
        if (Number(result.code) === 401) {
          clearAuthUser()
          if (window.location.pathname !== '/home') {
            window.location.href = `/home?login=1&redirect=${encodeURIComponent(window.location.pathname + window.location.search)}`
          }
        }
        return Promise.reject(new Error(result.message || result.msg || '接口请求失败'))
      }
      return result.data
    }

    return result
  },
  error => {
    const response = error.response
    const data = response?.data
    if (response?.status === 401) {
      clearAuthUser()
      if (window.location.pathname !== '/home') {
        window.location.href = `/home?login=1&redirect=${encodeURIComponent(window.location.pathname + window.location.search)}`
      }
    }

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
