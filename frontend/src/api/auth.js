import { request } from './request'

/**
 * 用户登录。
 * 后端会从 sys_user 表查询用户，并使用 BCrypt 校验密码，成功后返回 JWT。
 */
export function login(data) {
  return request('/api/auth/login', {
    method: 'POST',
    body: data
  })
}

/**
 * 学生注册。
 * 公开注册只创建 STUDENT 账号，管理员账号通过数据库初始化。
 */
export function register(data) {
  return request('/api/auth/register', {
    method: 'POST',
    body: data
  })
}

/**
 * 获取当前登录用户信息。
 */
export function getMe() {
  return request('/api/auth/me')
}
