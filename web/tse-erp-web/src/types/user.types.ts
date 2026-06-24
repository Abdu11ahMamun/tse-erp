// src/types/user.types.ts

export interface AfmUser {
  id: number
  userName: string
  fullName: string
  email: string
  mobileNo: string | null
  fromDate: string | null
  toDate: string | null
  roleId: number
  userTypeId: number
  isActive: 0 | 1
  gender: string | null
  createdAt: string | null
  updatedAt: string | null
}

export interface CreateUserRequest {
  userName: string
  fullName: string
  email: string
  password: string
  mobileNo?: string | null
  fromDate?: string | null
  toDate?: string | null
  roleId: number
  userTypeId: number
  isActive: 0 | 1
  gender?: string | null
}

export interface UpdateUserRequest {
  id: number
  userName: string
  fullName: string
  email: string
  mobileNo?: string | null
  fromDate?: string | null
  toDate?: string | null
  roleId: number
  userTypeId: number
  isActive: 0 | 1
  gender?: string | null
}