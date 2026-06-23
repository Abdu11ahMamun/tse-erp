// src/api/permissionApi.ts

import axiosInstance from './axiosInstance'
import type { AfmPermission, CreatePermissionRequest, UpdatePermissionRequest } from '../types/permission.types'

export const permissionApi = {
  getAll: async (): Promise<AfmPermission[]> => {
    const res = await axiosInstance.get<AfmPermission[]>('/permissions')
    return res.data
  },

  getById: async (id: number): Promise<AfmPermission> => {
    const res = await axiosInstance.get<AfmPermission>(`/permissions/${id}`)
    return res.data
  },

  getByModule: async (moduleId: number): Promise<AfmPermission[]> => {
    const res = await axiosInstance.get<AfmPermission[]>(`/permissions/module/${moduleId}`)
    return res.data
  },

  create: async (payload: CreatePermissionRequest): Promise<AfmPermission> => {
    const res = await axiosInstance.post<AfmPermission>('/permissions', payload)
    return res.data
  },

  update: async (id: number, payload: UpdatePermissionRequest): Promise<AfmPermission> => {
    const res = await axiosInstance.put<AfmPermission>(`/permissions/${id}`, payload)
    return res.data
  },

  delete: async (id: number): Promise<void> => {
    await axiosInstance.delete(`/permissions/${id}`)
  },
}