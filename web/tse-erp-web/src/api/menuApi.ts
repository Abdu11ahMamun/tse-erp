// src/api/menuApi.ts

import axiosInstance from './axiosInstance'
import type { AfmMenu, CreateMenuRequest, UpdateMenuRequest } from '../types/menu.types'

export const menuApi = {
  getAll: async (): Promise<AfmMenu[]> => {
    const res = await axiosInstance.get<AfmMenu[]>('/menus')
    return res.data
  },

  getById: async (id: number): Promise<AfmMenu> => {
    const res = await axiosInstance.get<AfmMenu>(`/menus/${id}`)
    return res.data
  },

  getByModule: async (moduleId: number): Promise<AfmMenu[]> => {
    const res = await axiosInstance.get<AfmMenu[]>(`/menus/module/${moduleId}`)
    return res.data
  },

  create: async (payload: CreateMenuRequest): Promise<AfmMenu> => {
    const res = await axiosInstance.post<AfmMenu>('/menus', payload)
    return res.data
  },

  update: async (id: number, payload: UpdateMenuRequest): Promise<AfmMenu> => {
    const res = await axiosInstance.put<AfmMenu>(`/menus/${id}`, payload)
    return res.data
  },

  delete: async (id: number): Promise<void> => {
    await axiosInstance.delete(`/menus/${id}`)
  },
}