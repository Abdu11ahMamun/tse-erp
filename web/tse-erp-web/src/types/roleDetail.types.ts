// src/types/roleDetail.types.ts

export interface AfmRoleDetail {
  id: number
  roleId: number
  moduleId: number
  menuId: number
  permissionId: string  // JSON string: "[1,2,3]"
  createdBy: number | null
  updatedBy: number | null
  createdAt: string | null
  updatedAt: string | null
}

export interface CreateRoleDetailRequest {
  roleId: number
  moduleId: number
  menuId: number
  permissionId: string  // JSON string: "[1,2,3]"
}

// UI helper — parsed version
export interface RoleDetailParsed extends Omit<AfmRoleDetail, 'permissionId'> {
  permissionIds: number[]
}