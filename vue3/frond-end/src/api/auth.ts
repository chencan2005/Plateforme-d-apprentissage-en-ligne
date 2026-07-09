import { post } from './request'
import { getUserProfile } from './user'
import type { UserInfo } from '@/types'
import { toBackendRole } from '@/utils/role'

export interface LoginParams {
  username: string
  password: string
}

export interface RegisterParams {
  username: string
  password: string
  role: 'student' | 'teacher'
}

export interface LoginResult {
  token: string
  userInfo: UserInfo
}

export async function login(data: LoginParams) {
  const res = await post<string>('/api/users/login', data)
  const token = res.data
  // 先拉 profile，成功后再由 store.setUser 统一写入 token，避免半登录态
  const profile = await getUserProfileWithToken(token)
  return { data: { token, userInfo: profile } as LoginResult }
}

async function getUserProfileWithToken(token: string): Promise<UserInfo> {
  const prev = localStorage.getItem('token')
  localStorage.setItem('token', token)
  try {
    const profile = await getUserProfile()
    return profile.data
  } catch (e) {
    if (prev) localStorage.setItem('token', prev)
    else localStorage.removeItem('token')
    throw e
  }
}

export function register(data: RegisterParams) {
  return post<void>('/api/users/register', {
    username: data.username,
    password: data.password,
    role: toBackendRole(data.role),
  })
}
