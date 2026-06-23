// src/api/axiosInstance.ts

import axios from 'axios'
import { useAuthStore } from '../store/authStore'

const BASE_URL = 'http://localhost:8080/api/v1'

const axiosInstance = axios.create({
  baseURL: BASE_URL,
  headers: { 'Content-Type': 'application/json' },
})

// ── Request interceptor — attach accessToken ──────────────────────────────
axiosInstance.interceptors.request.use(
  (config) => {
    const token = useAuthStore.getState().accessToken
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// ── Response interceptor — handle auth errors ─────────────────────────────
let isRefreshing = false
let failedQueue: Array<{
  resolve: (token: string) => void
  reject: (error: unknown) => void
}> = []

const processQueue = (error: unknown, token: string | null = null) => {
  failedQueue.forEach(({ resolve, reject }) => {
    if (error) reject(error)
    else resolve(token!)
  })
  failedQueue = []
}

axiosInstance.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config
    const status = error.response?.status
    const message = error.response?.data?.message ?? ''

    // ── 401 handling ──
    if (status === 401) {
      // Token expired → try refresh
      if (message === 'Token has expired' && !originalRequest._retry) {
        if (isRefreshing) {
          // Queue the request until refresh completes
          return new Promise((resolve, reject) => {
            failedQueue.push({ resolve, reject })
          }).then((token) => {
            originalRequest.headers.Authorization = `Bearer ${token}`
            return axiosInstance(originalRequest)
          }).catch((err) => Promise.reject(err))
        }

        originalRequest._retry = true
        isRefreshing = true

        const refreshToken = useAuthStore.getState().refreshToken

        if (!refreshToken) {
          useAuthStore.getState().logout()
          window.location.href = '/login'
          return Promise.reject(error)
        }

        try {
          const res = await axios.post(`${BASE_URL}/auth/refresh`, { refreshToken })
          const newAccessToken = res.data.accessToken

          useAuthStore.getState().setAccessToken(newAccessToken)
          processQueue(null, newAccessToken)

          originalRequest.headers.Authorization = `Bearer ${newAccessToken}`
          return axiosInstance(originalRequest)
        } catch (refreshError) {
          processQueue(refreshError, null)
          useAuthStore.getState().logout()
          window.location.href = '/login'
          return Promise.reject(refreshError)
        } finally {
          isRefreshing = false
        }
      }

      // Other 401 (not expired) → logout
      useAuthStore.getState().logout()
      window.location.href = '/login'
      return Promise.reject(error)
    }

    // ── 403 handling ──
    if (status === 403) {
      console.warn('[Auth] Access denied:', error.response?.data?.message)
      // Component level e handle korbe — error throw koro
      return Promise.reject(error)
    }

    // ── 400 handling — extract backend message ──
    if (status === 400) {
      const backendMessage = error.response?.data?.message || 'Bad request'
      return Promise.reject(new Error(backendMessage))
    }

    // ── 500 handling ──
    if (status === 500) {
      return Promise.reject(new Error('Something went wrong. Please try again.'))
    }

    // Generic error
    const fallbackMessage = error.response?.data?.message || error.message || 'Network error'
    console.error('[API Error]', fallbackMessage)
    return Promise.reject(error)
  }
)

export default axiosInstance