// src/api/roleApi.ts

import axiosInstance from './axiosInstance'
import type { AfmRole, CreateRoleRequest, UpdateRoleRequest } from '../types/role.types'

export const roleApi = {
  getAll: async (): Promise<AfmRole[]> => {
    const res = await axiosInstance.get<AfmRole[]>('/roles')
    return res.data
  },

  getById: async (id: number): Promise<AfmRole> => {
    const res = await axiosInstance.get<AfmRole>(`/roles/${id}`)
    return res.data
  },

  create: async (payload: CreateRoleRequest): Promise<AfmRole> => {
    const res = await axiosInstance.post<AfmRole>('/roles', payload)
    return res.data
  },

  update: async (id: number, payload: UpdateRoleRequest): Promise<AfmRole> => {
    const res = await axiosInstance.put<AfmRole>(`/roles/${id}`, payload)
    return res.data
  },

  delete: async (id: number): Promise<void> => {
    await axiosInstance.delete(`/roles/${id}`)
  },
}