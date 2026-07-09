import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserInfo, UserRole } from '@/types'
import { getUserInfo as getStoredUser, clearAuthStorage } from '@/utils/user'

export const useUserStore = defineStore('user', () => {
  const userInfo = ref<UserInfo | null>(getStoredUser())

  const isLoggedIn = computed(() => !!localStorage.getItem('token'))
  const role = computed<UserRole | null>(() => userInfo.value?.role ?? null)
  const isStudent = computed(() => role.value === 'student')
  const isTeacher = computed(() => role.value === 'teacher')

  function setUser(info: UserInfo, token: string) {
    userInfo.value = info
    localStorage.setItem('token', token)
    localStorage.setItem('userInfo', JSON.stringify(info))
    localStorage.setItem('loginTime', String(Date.now()))
  }

  function updateUser(info: Partial<UserInfo>) {
    if (userInfo.value) {
      userInfo.value = { ...userInfo.value, ...info }
      localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    }
  }

  function logout() {
    userInfo.value = null
    clearAuthStorage()
    location.href = '/login'
  }

  function loadFromStorage() {
    userInfo.value = getStoredUser()
  }

  return { userInfo, isLoggedIn, role, isStudent, isTeacher, setUser, updateUser, logout, loadFromStorage }
})
