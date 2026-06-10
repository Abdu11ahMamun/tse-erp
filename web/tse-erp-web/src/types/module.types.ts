// src/types/module.types.ts

export interface AfmModule {
  id: number
  moduleName: string
  isActive: 0 | 1
  createdBy: number | null
  updatedBy: number | null
  createdAt: string | null
  updatedAt: string | null
}

export interface CreateModuleRequest {
  moduleName: string
  isActive: 0 | 1
}

export interface UpdateModuleRequest extends CreateModuleRequest {
  id: number
}
