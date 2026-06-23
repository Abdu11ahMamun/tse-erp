// src/store/authStore.ts

import { create } from 'zustand'

interface AuthUser {
  username: string
  role: string
}

interface AuthState {
  accessToken: string | null
  refreshToken: string | null
  user: AuthUser | null
  isAuthenticated: boolean

  login: (response: {
    accessToken: string
    refreshToken?: string
    username: string
    role: string
  }) => void
  logout: () => void
  setAccessToken: (token: string) => void
}

export const useAuthStore = create<AuthState>((set) => ({
  // Initial state — accessToken memory only, refreshToken from localStorage
  accessToken: null,
  refreshToken: localStorage.getItem('refreshToken'),
  user: null,
  isAuthenticated: false,

  login: (response) => {
    // accessToken → memory only (never localStorage)
    // refreshToken → localStorage
    if (response.refreshToken) {
      localStorage.setItem('refreshToken', response.refreshToken)
    }
    set({
      accessToken: response.accessToken,
      refreshToken: response.refreshToken ?? localStorage.getItem('refreshToken'),
      user: { username: response.username, role: response.role },
      isAuthenticated: true,
    })
  },

  logout: () => {
    localStorage.removeItem('refreshToken')
    set({
      accessToken: null,
      refreshToken: null,
      user: null,
      isAuthenticated: false,
    })
  },

  setAccessToken: (token) => {
    set({ accessToken: token })
  },
}))