// src/pages/admin/RoleEditPage.tsx

import { useState, useMemo } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { ArrowLeft, Shield, Save, Trash2, Check, Layers, Hash, Key, Plus } from 'lucide-react'
import { roleApi } from '../../api/roleApi'
import { roleDetailApi } from '../../api/roleDetailApi'
import { moduleApi } from '../../api/moduleApi'
import { permissionApi } from '../../api/permissionApi'
import { menuApi } from '../../api/menuApi'
import type { CreateRoleRequest } from '../../types/role.types'
import type { AfmRoleDetail } from '../../types/roleDetail.types'

function Toast({ message, type }: { message: string; type: 'success' | 'error' }) {
  return (
    <div className={`fixed top-5 right-5 z-50 flex items-center gap-2.5 px-4 py-3 rounded-lg shadow-xl text-sm font-medium
      ${type === 'success' ? 'bg-green-600 text-white' : 'bg-red-600 text-white'}`}>
      <span>{type === 'success' ? '✓' : '✕'}</span>
      {message}
    </div>
  )
}

function parsePermissionIds(raw: string): number[] {
  try {
    const parsed = JSON.parse(raw)
    return Array.isArray(parsed) ? parsed.map(Number) : []
  } catch { return [] }
}

const MODULE_COLORS = [
  { bg: 'bg-blue-50', border: 'border-blue-200', text: 'text-blue-700', badge: 'bg-blue-100 text-blue-700 border-blue-200', dot: 'bg-blue-500' },
  { bg: 'bg-violet-50', border: 'border-violet-200', text: 'text-violet-700', badge: 'bg-violet-100 text-violet-700 border-violet-200', dot: 'bg-violet-500' },
  { bg: 'bg-emerald-50', border: 'border-emerald-200', text: 'text-emerald-700', badge: 'bg-emerald-100 text-emerald-700 border-emerald-200', dot: 'bg-emerald-500' },
  { bg: 'bg-amber-50', border: 'border-amber-200', text: 'text-amber-700', badge: 'bg-amber-100 text-amber-700 border-amber-200', dot: 'bg-amber-500' },
  { bg: 'bg-rose-50', border: 'border-rose-200', text: 'text-rose-700', badge: 'bg-rose-100 text-rose-700 border-rose-200', dot: 'bg-rose-500' },
  { bg: 'bg-cyan-50', border: 'border-cyan-200', text: 'text-cyan-700', badge: 'bg-cyan-100 text-cyan-700 border-cyan-200', dot: 'bg-cyan-500' },
]

export default function RoleEditPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const roleId = Number(id)

  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' } | null>(null)
  const [selectedModuleId, setSelectedModuleId] = useState<number | ''>('')
  const [selectedMenuId, setSelectedMenuId] = useState<number | ''>('')
  const [selectedPermissions, setSelectedPermissions] = useState<number[]>([])
  const [deletingId, setDeletingId] = useState<number | null>(null)

  const showToast = (message: string, type: 'success' | 'error' = 'success') => {
    setToast({ message, type })
    setTimeout(() => setToast(null), 3000)
  }

  // ── Queries ──
  const { data: role } = useQuery({
    queryKey: ['roles', roleId],
    queryFn: () => roleApi.getById(roleId),
    enabled: !!roleId,
  })

  const { data: assignments = [], isLoading: assignLoading } = useQuery({
    queryKey: ['role-details', roleId],
    queryFn: () => roleDetailApi.getByRole(roleId),
    enabled: !!roleId,
  })

  const { data: modules = [] } = useQuery({ queryKey: ['modules'], queryFn: moduleApi.getAll })
  const { data: allPermissions = [] } = useQuery({ queryKey: ['permissions'], queryFn: permissionApi.getAll })
  const { data: allMenus = [] } = useQuery({ queryKey: ['menus'], queryFn: menuApi.getAll })

  const { data: moduleMenus = [] } = useQuery({
    queryKey: ['menus', 'module', selectedModuleId],
    queryFn: () => menuApi.getByModule(Number(selectedModuleId)),
    enabled: !!selectedModuleId,
  })

  const { data: modulePermissions = [] } = useQuery({
    queryKey: ['permissions', 'module', selectedModuleId],
    queryFn: () => permissionApi.getByModule(Number(selectedModuleId)),
    enabled: !!selectedModuleId,
  })

  // ── Role edit form ──
  const { register, handleSubmit, formState: { errors } } = useForm<CreateRoleRequest>({
    values: role ? { roleName: role.roleName, isActive: role.isActive } : undefined,
  })

  // ── Already assigned permission IDs for this module+menu combo ──
  const alreadyAssignedPermIds = useMemo(() => {
    if (!selectedModuleId || !selectedMenuId) return new Set<number>()
    const matching = assignments.filter(
      a => a.moduleId === Number(selectedModuleId) && a.menuId === Number(selectedMenuId)
    )
    const ids = new Set<number>()
    matching.forEach(a => parsePermissionIds(a.permissionId).forEach(id => ids.add(id)))
    return ids
  }, [assignments, selectedModuleId, selectedMenuId])

  // Available permissions = module permissions minus already assigned
  const availablePermissions = modulePermissions.filter(p => !alreadyAssignedPermIds.has(p.id))

  // ── Maps ──
  const permissionMap = useMemo(() => {
    const map = new Map<number, string>()
    allPermissions.forEach(p => map.set(p.id, p.permissionName))
    return map
  }, [allPermissions])

  const menuMap = useMemo(() => {
    const map = new Map<number, string>()
    allMenus.forEach(m => map.set(m.id, m.menuName))
    return map
  }, [allMenus])

  const moduleColorMap = useMemo(() => {
    const map = new Map<number, number>()
    let i = 0
    assignments.forEach(d => {
      if (!map.has(d.moduleId)) { map.set(d.moduleId, i); i++ }
    })
    return map
  }, [assignments])

  // ── Mutations ──
  const updateRoleMutation = useMutation({
    mutationFn: (data: CreateRoleRequest) =>
      roleApi.update(roleId, { ...data, id: roleId }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['roles'] })
      showToast('Role updated successfully!')
    },
    onError: (error: any) => showToast(error?.response?.data?.message || 'Failed to update.', 'error'),
  })

  const assignMutation = useMutation({
    mutationFn: roleDetailApi.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['role-details', roleId] })
      setSelectedModuleId('')
      setSelectedMenuId('')
      setSelectedPermissions([])
      showToast('Permissions assigned!')
    },
    onError: (error: any) => showToast(error?.response?.data?.message || 'Failed to assign.', 'error'),
  })

  const deleteMutation = useMutation({
    mutationFn: roleDetailApi.delete,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['role-details', roleId] })
      setDeletingId(null)
      showToast('Assignment removed.')
    },
    onError: () => { setDeletingId(null); showToast('Failed to remove.', 'error') },
  })

  const togglePermission = (id: number) =>
    setSelectedPermissions(prev =>
      prev.includes(id) ? prev.filter(p => p !== id) : [...prev, id]
    )

  const toggleAll = () =>
    setSelectedPermissions(
      selectedPermissions.length === availablePermissions.length
        ? [] : availablePermissions.map(p => p.id)
    )

  const handleAssign = () => {
    if (!selectedModuleId) return showToast('Select a module.', 'error')
    if (!selectedMenuId) return showToast('Select a menu.', 'error')
    if (selectedPermissions.length === 0) return showToast('Select at least one permission.', 'error')
    assignMutation.mutate({
      roleId,
      moduleId: Number(selectedModuleId),
      menuId: Number(selectedMenuId),
      permissionId: JSON.stringify(selectedPermissions),
    })
  }

  const getModuleName = (id: number) => modules.find(m => m.id === id)?.moduleName ?? `Module ${id}`
  const getMenuName = (id: number) => menuMap.get(id) ?? `Menu ${id}`
  const getPermissionNames = (raw: string) =>
    parsePermissionIds(raw).map(id => permissionMap.get(id) ?? `#${id}`)

  return (
    <div className="h-full bg-gray-50 flex flex-col">
      {toast && <Toast message={toast.message} type={toast.type} />}

      {/* Breadcrumb header */}
      <div className="bg-white border-b border-gray-200 px-6 py-4 flex items-center justify-between flex-shrink-0">
        <div className="flex items-center gap-3">
          <button onClick={() => navigate('/admin/roles')}
            className="p-2 rounded-lg text-gray-400 hover:text-gray-700 hover:bg-gray-100 transition-colors">
            <ArrowLeft size={16} />
          </button>
          <div className="flex items-center gap-2 text-sm">
            <span className="text-gray-400">Admin</span>
            <span className="text-gray-300">/</span>
            <span className="text-gray-400 cursor-pointer hover:text-blue-600" onClick={() => navigate('/admin/roles')}>Roles</span>
            <span className="text-gray-300">/</span>
            <span className="text-gray-700 font-medium">{role?.roleName ?? 'Edit'}</span>
          </div>
        </div>
      </div>

      {/* Main — two columns */}
      <div className="flex-1 overflow-hidden flex gap-0">

        {/* ── LEFT — Role info + Assign form ── */}
        <div className="w-[380px] flex-shrink-0 flex flex-col bg-white border-r border-gray-200 overflow-y-auto">

          {/* Role info section */}
          <div className="px-5 py-4 border-b border-gray-100">
            <div className="flex items-center gap-2.5 mb-4">
              <div className="w-7 h-7 bg-blue-600 rounded-lg flex items-center justify-center">
                <Shield size={13} className="text-white" />
              </div>
              <h2 className="text-sm font-semibold text-gray-800">Role Information</h2>
            </div>

            <form className="space-y-3">
              <div>
                <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wide mb-1.5">
                  Role Name <span className="text-red-400">*</span>
                </label>
                <input
                  {...register('roleName', { required: true })}
                  className={`w-full border rounded-lg px-3 py-2 text-sm outline-none transition-colors
                    ${errors.roleName ? 'border-red-300 bg-red-50' : 'border-gray-200 focus:border-blue-400 focus:ring-2 focus:ring-blue-50'}`}
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wide mb-1.5">Status</label>
                <div className="flex gap-2">
                  {[{ label: 'Active', value: 1 }, { label: 'Inactive', value: 0 }].map(opt => (
                    <label key={opt.value}
                      className="flex-1 flex items-center gap-2 border border-gray-200 rounded-lg px-3 py-2 cursor-pointer hover:border-blue-300 transition-colors has-[:checked]:border-blue-500 has-[:checked]:bg-blue-50">
                      <input type="radio" value={opt.value} {...register('isActive')} className="accent-blue-600" />
                      <span className="text-sm text-gray-700">{opt.label}</span>
                    </label>
                  ))}
                </div>
              </div>

              <button
                type="button"
                onClick={handleSubmit(data => updateRoleMutation.mutate({ ...data, isActive: Number(data.isActive) as 0 | 1 }))}
                disabled={updateRoleMutation.isPending}
                className="w-full py-2 bg-blue-600 hover:bg-blue-700 disabled:opacity-50 text-white text-sm font-medium rounded-lg transition-colors flex items-center justify-center gap-2"
              >
                <Save size={13} />
                {updateRoleMutation.isPending ? 'Saving...' : 'Save Changes'}
              </button>
            </form>
          </div>

          {/* Assign permission section */}
          <div className="px-5 py-4 flex-1">
            <div className="flex items-center gap-2.5 mb-4">
              <div className="w-7 h-7 bg-violet-100 rounded-lg flex items-center justify-center">
                <Key size={13} className="text-violet-600" />
              </div>
              <h2 className="text-sm font-semibold text-gray-800">Assign Permissions</h2>
            </div>

            <div className="space-y-3">
              {/* Module */}
              <div>
                <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wide mb-1.5">Module</label>
                <select
                  value={selectedModuleId}
                  onChange={e => {
                    setSelectedModuleId(e.target.value ? Number(e.target.value) : '')
                    setSelectedMenuId('')
                    setSelectedPermissions([])
                  }}
                  className="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm outline-none focus:border-blue-400 focus:ring-2 focus:ring-blue-50 bg-white"
                >
                  <option value="">-- Select Module --</option>
                  {modules.map(m => <option key={m.id} value={m.id}>{m.moduleName}</option>)}
                </select>
              </div>

              {/* Menu */}
              <div>
                <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wide mb-1.5">Menu</label>
                <select
                  value={selectedMenuId}
                  onChange={e => { setSelectedMenuId(e.target.value ? Number(e.target.value) : ''); setSelectedPermissions([]) }}
                  disabled={!selectedModuleId}
                  className="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm outline-none focus:border-blue-400 focus:ring-2 focus:ring-blue-50 bg-white disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  <option value="">-- Select Menu --</option>
                  {moduleMenus.map(m => <option key={m.id} value={m.id}>{m.menuName}</option>)}
                </select>
              </div>

              {/* Permissions */}
              <div>
                <div className="flex items-center justify-between mb-1.5">
                  <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wide">
                    Permissions
                    {selectedPermissions.length > 0 && (
                      <span className="ml-1.5 bg-blue-600 text-white text-xs px-1.5 py-0.5 rounded-full font-bold">{selectedPermissions.length}</span>
                    )}
                  </label>
                  {availablePermissions.length > 0 && (
                    <button onClick={toggleAll} className="text-xs text-blue-600 hover:text-blue-700 font-semibold">
                      {selectedPermissions.length === availablePermissions.length ? 'Deselect All' : 'Select All'}
                    </button>
                  )}
                </div>

                {!selectedMenuId ? (
                  <div className="text-xs text-gray-400 py-4 text-center border border-dashed border-gray-200 rounded-lg bg-gray-50">
                    Select module & menu first
                  </div>
                ) : availablePermissions.length === 0 ? (
                  <div className="text-xs text-gray-400 py-4 text-center border border-dashed border-gray-200 rounded-lg bg-gray-50">
                    All permissions already assigned
                  </div>
                ) : (
                  <div className="space-y-1 max-h-44 overflow-y-auto border border-gray-200 rounded-lg p-2 bg-gray-50">
                    {availablePermissions.map(perm => {
                      const checked = selectedPermissions.includes(perm.id)
                      return (
                        <label key={perm.id}
                          className={`flex items-center gap-2.5 px-3 py-2 rounded-lg cursor-pointer transition-all
                            ${checked ? 'bg-blue-50 border border-blue-200' : 'hover:bg-white border border-transparent'}`}>
                          <div className={`w-4 h-4 rounded flex items-center justify-center flex-shrink-0 border-2 transition-all
                            ${checked ? 'bg-blue-600 border-blue-600' : 'border-gray-300 bg-white'}`}>
                            {checked && <Check size={9} className="text-white" strokeWidth={3.5} />}
                          </div>
                          <input type="checkbox" className="hidden" checked={checked} onChange={() => togglePermission(perm.id)} />
                          <span className={`text-sm transition-colors ${checked ? 'text-blue-700 font-semibold' : 'text-gray-600'}`}>
                            {perm.permissionName}
                          </span>
                        </label>
                      )
                    })}
                  </div>
                )}
              </div>

              <button
                onClick={handleAssign}
                disabled={assignMutation.isPending || selectedPermissions.length === 0}
                className="w-full py-2.5 bg-violet-600 hover:bg-violet-700 disabled:opacity-40 disabled:cursor-not-allowed text-white text-sm font-semibold rounded-lg transition-colors flex items-center justify-center gap-2"
              >
                {assignMutation.isPending ? (
                  <><span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />Assigning...</>
                ) : (
                  <><Plus size={14} />Assign Permissions</>
                )}
              </button>
            </div>
          </div>
        </div>

        {/* ── RIGHT — Assigned permissions list ── */}
        <div className="flex-1 overflow-y-auto p-5">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-sm font-semibold text-gray-800">Permission List</h2>
            {assignments.length > 0 && (
              <span className="text-xs text-gray-500 bg-white border border-gray-200 px-2.5 py-1 rounded-full">
                {assignments.length} assignment{assignments.length !== 1 ? 's' : ''}
              </span>
            )}
          </div>

          {assignLoading ? (
            <div className="space-y-3">
              {[1,2,3].map(i => <div key={i} className="h-20 bg-white rounded-xl border border-gray-200 animate-pulse" />)}
            </div>
          ) : assignments.length === 0 ? (
            <div className="flex flex-col items-center justify-center h-64 text-center">
              <div className="w-16 h-16 bg-gray-100 rounded-2xl flex items-center justify-center mb-3">
                <Key size={24} className="text-gray-300" />
              </div>
              <p className="text-sm font-semibold text-gray-400">No permissions assigned yet</p>
              <p className="text-xs text-gray-300 mt-1">Use the left panel to assign permissions</p>
            </div>
          ) : (
            <div className="space-y-3">
              {assignments.map((detail, index) => {
                const colorIndex = moduleColorMap.get(detail.moduleId) ?? index
                const color = MODULE_COLORS[colorIndex % MODULE_COLORS.length]
                const permNames = getPermissionNames(detail.permissionId)

                return (
                  <div key={detail.id} className={`border rounded-xl p-4 transition-all hover:shadow-sm ${color.bg} ${color.border}`}>
                    <div className="flex items-start justify-between gap-3">
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center gap-2 flex-wrap mb-2.5">
                          <div className="flex items-center gap-1.5">
                            <Layers size={13} className={color.text} />
                            <span className={`text-sm font-bold ${color.text}`}>{getModuleName(detail.moduleId)}</span>
                          </div>
                          <span className="flex items-center gap-1 text-xs text-gray-500 bg-white px-2 py-0.5 rounded-md border border-gray-200 font-medium">
                            <Hash size={10} />
                            {getMenuName(detail.menuId)}
                          </span>
                          <span className="text-xs text-gray-400 bg-white px-2 py-0.5 rounded-md border border-gray-200">
                            {permNames.length} permission{permNames.length !== 1 ? 's' : ''}
                          </span>
                        </div>
                        <div className="flex flex-wrap gap-1.5">
                          {permNames.map((name, i) => (
                            <span key={i} className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-md text-xs font-medium border ${color.badge}`}>
                              <Key size={9} />
                              {name}
                            </span>
                          ))}
                        </div>
                      </div>
                      <button
                        onClick={() => {
                          if (confirm('Remove this assignment?')) {
                            setDeletingId(detail.id)
                            deleteMutation.mutate(detail.id)
                          }
                        }}
                        disabled={deletingId === detail.id}
                        className="p-1.5 rounded-lg text-gray-300 hover:text-red-500 hover:bg-white transition-all border border-transparent hover:border-red-100 flex-shrink-0"
                      >
                        <Trash2 size={14} />
                      </button>
                    </div>
                  </div>
                )
              })}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}