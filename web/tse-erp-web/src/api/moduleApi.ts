// src/api/moduleApi.ts

import axiosInstance from './axiosInstance'
import type { AfmModule, CreateModuleRequest, UpdateModuleRequest } from '../types/module.types'

export const moduleApi = {
  getAll: async (): Promise<AfmModule[]> => {
    const res = await axiosInstance.get<AfmModule[]>('/modules')
    return res.data
  },

  getById: async (id: number): Promise<AfmModule> => {
    const res = await axiosInstance.get<AfmModule>(`/modules/${id}`)
    return res.data
  },

  create: async (payload: CreateModuleRequest): Promise<AfmModule> => {
    const res = await axiosInstance.post<AfmModule>('/modules', payload)
    return res.data
  },

  update: async (id: number, payload: UpdateModuleRequest): Promise<AfmModule> => {
    const res = await axiosInstance.put<AfmModule>(`/modules/${id}`, payload)
    return res.data
  },

  delete: async (id: number): Promise<void> => {
    await axiosInstance.delete(`/modules/${id}`)
  },
}
