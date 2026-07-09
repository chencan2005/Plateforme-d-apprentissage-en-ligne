import type { UserInfo } from '@/types'

export const getUserInfo = (): UserInfo | null => {
  const str = localStorage.getItem('userInfo')
  if (!str) return null
  try {
    return JSON.parse(str) as UserInfo
  } catch {
    localStorage.removeItem('userInfo')
    return null
  }
}

export const getToken = (): string | null => localStorage.getItem('token')

export const clearAuthStorage = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  localStorage.removeItem('loginTime')
}

/** 清除本地登录态并跳转登录页（用于主动退出） */
export const logout = () => {
  clearAuthStorage()
  location.href = '/login'
}

export const roleLabel = (role?: string) => {
  const r = role?.toLowerCase()
  if (r === 'teacher') return '教师'
  if (r === 'student') return '学生'
  return role || '未知'
}
