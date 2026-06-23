// src/types/permission.types.ts

export interface AfmPermission {
  id: number
  permissionName: string
  moduleId: number
  isActive: 0 | 1
  createdBy: number | null
  updatedBy: number | null
  createdAt: string | null
  updatedAt: string | null
}

export interface CreatePermissionRequest {
  permissionName: string
  moduleId: number
  isActive: 0 | 1
}

export interface UpdatePermissionRequest extends CreatePermissionRequest {
  id: number
}