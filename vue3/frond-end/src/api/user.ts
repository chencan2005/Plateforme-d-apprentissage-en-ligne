import { post, put } from './request'
import type { UserInfo } from '@/types'
import { toFrontendRole } from '@/utils/role'

interface ProfileDTO {
  id: number
  username: string
  role: string
  createdTime?: string
}

function mapProfile(dto: ProfileDTO): UserInfo {
  return {
    id: dto.id,
    username: dto.username,
    role: toFrontendRole(dto.role),
    createdTime: dto.createdTime,
  }
}

export async function getUserProfile() {
  const res = await post<ProfileDTO>('/api/users/profile')
  return { ...res, data: mapProfile(res.data) }
}

export function changePassword(data: { oldPassword: string; newPassword: string }) {
  return put<void>('/api/users/updatePassword', data)
}
