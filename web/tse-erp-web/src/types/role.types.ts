// src/types/role.types.ts

export interface AfmRole {
  id: number
  roleName: string
  isActive: 0 | 1
  createdBy: number | null
  updatedBy: number | null
  createdAt: string | null
  updatedAt: string | null
}

export interface CreateRoleRequest {
  roleName: string
  isActive: 0 | 1
}

export interface UpdateRoleRequest extends CreateRoleRequest {
  id: number
}