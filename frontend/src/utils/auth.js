const AUTH_KEY = 'aijump_auth_user'

export function getAuthUser() {
  try {
    const raw = localStorage.getItem(AUTH_KEY)
    return raw ? JSON.parse(raw) : null
  } catch (error) {
    console.warn('读取登录信息失败：', error)
    return null
  }
}

export function setAuthUser(user) {
  localStorage.setItem(AUTH_KEY, JSON.stringify(user || {}))
}

export function clearAuthUser() {
  localStorage.removeItem(AUTH_KEY)
}

export function isLoggedIn() {
  return !!getAuthUser()?.token
}

export function getAuthRole() {
  return getAuthUser()?.role || ''
}

export function isAdmin() {
  return getAuthRole() === 'ADMIN'
}

export function isStudent() {
  return getAuthRole() === 'STUDENT'
}
