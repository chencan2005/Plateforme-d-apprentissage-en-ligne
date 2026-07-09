import type { UserRole } from '@/types'

export function toFrontendRole(role?: string): UserRole {
  return role?.toUpperCase() === 'TEACHER' ? 'teacher' : 'student'
}

export function toBackendRole(role: UserRole): 'STUDENT' | 'TEACHER' {
  return role === 'teacher' ? 'TEACHER' : 'STUDENT'
}
