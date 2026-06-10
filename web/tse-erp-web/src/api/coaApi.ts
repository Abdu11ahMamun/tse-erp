// src/api/coaApi.ts

import axiosInstance from './axiosInstance'
import type { AfmCoa, CreateCoaRequest, UpdateCoaRequest } from '../types/coa.types'

export const coaApi = {
  getAll: async (): Promise<AfmCoa[]> => {
    const res = await axiosInstance.get<AfmCoa[]>('/coa')
    return res.data
  },

  getById: async (id: number): Promise<AfmCoa> => {
    const res = await axiosInstance.get<AfmCoa>(`/coa/${id}`)
    return res.data
  },

  create: async (payload: CreateCoaRequest): Promise<AfmCoa> => {
    const res = await axiosInstance.post<AfmCoa>('/coa', payload)
    return res.data
  },

  update: async (id: number, payload: UpdateCoaRequest): Promise<AfmCoa> => {
    const res = await axiosInstance.put<AfmCoa>(`/coa/${id}`, payload)
    return res.data
  },

  delete: async (id: number): Promise<void> => {
    await axiosInstance.delete(`/coa/${id}`)
  },
}