// src/hooks/useAuth.ts

import { useMutation } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import axios from 'axios'
import { useAuthStore } from '../store/authStore'

const BASE_URL = 'http://localhost:8080/api/v1'

interface LoginRequest {
  username: string
  password: string
}

export function useAuth() {
  const { login, logout, isAuthenticated, user } = useAuthStore()
  const navigate = useNavigate()

  const loginMutation = useMutation({
    mutationFn: async (credentials: LoginRequest) => {
    try {
      const res = await axios.post(`${BASE_URL}/auth/login`, credentials)
      return res.data
    } catch (error: any) {
      // Backend message extract করো
      const message = error?.response?.data?.message || 'Login failed. Please try again.'
      throw new Error(message)
    }
    },
    onSuccess: (data) => {
      login({
        accessToken: data.accessToken,
        refreshToken: data.refreshToken,
        username: data.username,
        role: data.role,
      })
      navigate('/dashboard')
    },
  })
  const handleLogout = async () => {
    try {
      const token = useAuthStore.getState().accessToken
      if (token) {
        await axios.post(
          `${BASE_URL}/auth/logout`,
          {},
          { headers: { Authorization: `Bearer ${token}` } }
        )
      }
    } catch {
      // logout locally even if server call fails
    } finally {
      logout()
      navigate('/login')
    }
  }

  return {
    loginMutation,
    logout: handleLogout,
    isAuthenticated,
    user,
  }
}