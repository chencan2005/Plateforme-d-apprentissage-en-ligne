import axios from 'axios'
import type { AxiosInstance, AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { clearAuthStorage } from '@/utils/user'

const request: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '',
  timeout: 15000,
})

request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error),
)

request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code === 200 || res.code === undefined) {
      return res
    }
    ElMessage.error(res.message || '服务器错误')
    return Promise.reject(new Error(res.message || '服务器错误'))
  },
  (error) => {
    if (error.response?.status === 401) {
      clearAuthStorage()
      if (router.currentRoute.value.path !== '/login') {
        router.push('/login')
      }
      ElMessage.warning('登录已过期，请重新登录')
    } else if (error.code === 'ECONNABORTED') {
      ElMessage.error('请求超时，请稍后重试')
    } else if (!error.response) {
      ElMessage.error('网络错误，请检查网络连接')
    } else {
      ElMessage.error(error.response?.data?.message || `服务器错误 (${error.response?.status})`)
    }
    return Promise.reject(error)
  },
)

export function get<T>(url: string, config?: AxiosRequestConfig) {
  return request.get<unknown, { data: T }>(url, config)
}

export function post<T>(url: string, data?: unknown, config?: AxiosRequestConfig) {
  return request.post<unknown, { data: T }>(url, data, config)
}

export function put<T>(url: string, data?: unknown, config?: AxiosRequestConfig) {
  return request.put<unknown, { data: T }>(url, data, config)
}

export function del<T>(url: string, config?: AxiosRequestConfig) {
  return request.delete<unknown, { data: T }>(url, config)
}

export default request
