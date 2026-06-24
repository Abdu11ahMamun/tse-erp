// src/api/userApi.ts

import axiosInstance from './axiosInstance'
import type { AfmUser, CreateUserRequest, UpdateUserRequest } from '../types/user.types'

export const userApi = {
  getAll: async (): Promise<AfmUser[]> => {
    const res = await axiosInstance.get<AfmUser[]>('/users')
    return res.data
  },

  getById: async (id: number): Promise<AfmUser> => {
    const res = await axiosInstance.get<AfmUser>(`/users/${id}`)
    return res.data
  },

  create: async (payload: CreateUserRequest): Promise<AfmUser> => {
    const res = await axiosInstance.post<AfmUser>('/users', payload)
    return res.data
  },

  update: async (id: number, payload: UpdateUserRequest): Promise<AfmUser> => {
    const res = await axiosInstance.put<AfmUser>(`/users/${id}`, payload)
    return res.data
  },

  delete: async (id: number): Promise<void> => {
    await axiosInstance.delete(`/users/${id}`)
  },
}