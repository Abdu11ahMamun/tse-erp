// src/types/menu.types.ts

export interface AfmMenu {
  id: number
  menuName: string
  moduleId: number
  isParent: 0 | 1
  parentMenuId: number | null
  permissionId: string   // JSON string: "[]" or "[1,2,3]"
  sortOrder: string | null
  routeName: string | null
  isTopMenu: 0 | 1
  isActive: 0 | 1
  createdBy: number | null
  updatedBy: number | null
  createdAt: string | null
  updatedAt: string | null
}

export interface CreateMenuRequest {
  menuName: string
  moduleId: number
  isParent: 0 | 1
  parentMenuId?: number | null
  permissionId?: string
  sortOrder?: string | null
  routeName?: string | null
  isTopMenu: 0 | 1
  isActive: 0 | 1
}

export interface UpdateMenuRequest extends CreateMenuRequest {
  id: number
}

// UI helper — tree node
export interface MenuTreeNode extends AfmMenu {
  children: MenuTreeNode[]
}