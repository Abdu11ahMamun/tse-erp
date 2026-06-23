// src/api/roleDetailApi.ts

import axiosInstance from './axiosInstance'
import type { AfmRoleDetail, CreateRoleDetailRequest } from '../types/roleDetail.types'

export const roleDetailApi = {
  getAll: async (): Promise<AfmRoleDetail[]> => {
    const res = await axiosInstance.get<AfmRoleDetail[]>('/role-details')
    return res.data
  },

  getById: async (id: number): Promise<AfmRoleDetail> => {
    const res = await axiosInstance.get<AfmRoleDetail>(`/role-details/${id}`)
    return res.data
  },

  getByRole: async (roleId: number): Promise<AfmRoleDetail[]> => {
    const res = await axiosInstance.get<AfmRoleDetail[]>(`/role-details/role/${roleId}`)
    return res.data
  },

  create: async (payload: CreateRoleDetailRequest): Promise<AfmRoleDetail> => {
    const res = await axiosInstance.post<AfmRoleDetail>('/role-details', payload)
    return res.data
  },

  update: async (id: number, payload: CreateRoleDetailRequest): Promise<AfmRoleDetail> => {
    const res = await axiosInstance.put<AfmRoleDetail>(`/role-details/${id}`, payload)
    return res.data
  },

  delete: async (id: number): Promise<void> => {
    await axiosInstance.delete(`/role-details/${id}`)
  },
}