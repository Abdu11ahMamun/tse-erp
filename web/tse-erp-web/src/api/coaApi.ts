// src/api/coaApi.ts

import axios from 'axios';
import type { AfmCoa, CreateCoaRequest, UpdateCoaRequest } from '../types/coa.types';

const BASE_URL = 'http://localhost:8080/api/v1';

const api = axios.create({
  baseURL: BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const coaApi = {
  getAll: async (): Promise<AfmCoa[]> => {
    const res = await api.get<AfmCoa[]>('/coa');
    return res.data;
  },

  getById: async (id: number): Promise<AfmCoa> => {
    const res = await api.get<AfmCoa>(`/coa/${id}`);
    return res.data;
  },

  create: async (payload: CreateCoaRequest): Promise<AfmCoa> => {
    const res = await api.post<AfmCoa>('/coa', payload);
    return res.data;
  },

  update: async (id: number, payload: UpdateCoaRequest): Promise<AfmCoa> => {
    const res = await api.put<AfmCoa>(`/coa/${id}`, payload);
    return res.data;
  },

  delete: async (id: number): Promise<void> => {
    await api.delete(`/coa/${id}`);
  },
};