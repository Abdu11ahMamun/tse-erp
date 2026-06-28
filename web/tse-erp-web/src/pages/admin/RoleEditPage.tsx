// src/pages/admin/RoleEditPage.tsx

import { useState, useMemo, useRef, useEffect } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { ArrowLeft, Trash2, ChevronDown, X, Search, List, Shield, Key } from 'lucide-react'
import axiosInstance from '../../api/axiosInstance'
import { moduleApi } from '../../api/moduleApi'
import { menuApi } from '../../api/menuApi'
import { roleApi } from '../../api/roleApi'
import { roleDetailApi } from '../../api/roleDetailApi'
import type { CreateRoleRequest } from '../../types/role.types'

interface AssignedPermission {
  id: number
  moduleId: number
  moduleName: string
  menuId: number
  menuName: string
  permissions: { id: number; permissionName: string }[]
}

interface RoleDetails {
  roleId: number
  roleName: string
  assignedPermissions: AssignedPermission[]
}

interface AvailablePermission {
  id: number
  permissionName: string
}

function Toast({ message, type }: { message: string; type: 'success' | 'error' }) {
  return (
    <div className={`fixed top-5 right-5 z-50 flex items-center gap-2.5 px-4 py-3 rounded-lg shadow-xl text-sm font-medium
      ${type === 'success' ? 'bg-green-600 text-white' : 'bg-red-600 text-white'}`}>
      <span>{type === 'success' ? '✓' : '✕'}</span>
      {message}
    </div>
  )
}

function PermissionMultiSelect({
  options, selected, onChange, disabled,
}: {
  options: AvailablePermission[]
  selected: number[]
  onChange: (ids: number[]) => void
  disabled: boolean
}) {
  const [open, setOpen] = useState(false)
  const ref = useRef<HTMLDivElement>(null)

  useEffect(() => {
    const handler = (e: MouseEvent) => {
      if (ref.current && !ref.current.contains(e.target as Node)) setOpen(false)
    }
    document.addEventListener('mousedown', handler)
    return () => document.removeEventListener('mousedown', handler)
  }, [])

  const toggle = (id: number) =>
    onChange(selected.includes(id) ? selected.filter(s => s !== id) : [...selected, id])

  const remove = (id: number, e: React.MouseEvent) => {
    e.stopPropagation()
    onChange(selected.filter(s => s !== id))
  }

  const selectedOptions = options.filter(o => selected.includes(o.id))

  return (
    <div ref={ref} className="relative flex-1 min-w-0">
      <div
        onClick={() => !disabled && setOpen(!open)}
        className={`min-h-[40px] border rounded-lg px-3 py-1.5 flex flex-wrap gap-1.5 items-center cursor-pointer bg-white transition-all
          ${disabled ? 'opacity-50 cursor-not-allowed bg-gray-50 border-gray-200' : 'border-gray-300 hover:border-blue-400'}
          ${open ? 'border-blue-500 ring-2 ring-blue-100' : ''}`}
      >
        {selectedOptions.length === 0 ? (
          <span className="text-sm text-gray-400 py-0.5">Select permissions...</span>
        ) : (
          selectedOptions.map(opt => (
            <span key={opt.id} className="flex items-center gap-1 bg-blue-100 text-blue-700 text-xs font-medium px-2 py-0.5 rounded-md border border-blue-200">
              {opt.permissionName}
              <button onClick={e => remove(opt.id, e)} className="hover:text-blue-900 ml-0.5">
                <X size={10} />
              </button>
            </span>
          ))
        )}
        <ChevronDown size={14} className={`ml-auto text-gray-400 flex-shrink-0 transition-transform ${open ? 'rotate-180' : ''}`} />
      </div>

      {open && (
        // <div className="absolute top-full left-0 right-0 mt-1 bg-white border border-gray-200 rounded-xl shadow-xl z-30 max-h-52 overflow-y-auto">
        // <div className="absolute top-full left-0 right-0 mt-1 bg-white border border-gray-200 rounded-xl shadow-xl z-50 max-h-52 overflow-y-auto">
        <div className="fixed bg-white border border-gray-200 rounded-xl shadow-xl z-[9999] max-h-52 overflow-y-auto"
  style={{
    width: ref.current?.getBoundingClientRect().width,
    top: ref.current ? ref.current.getBoundingClientRect().bottom + window.scrollY + 4 : 0,
    left: ref.current?.getBoundingClientRect().left,
  }}
>
          
          {options.length === 0 ? (
            <div className="px-4 py-4 text-sm text-gray-400 text-center">No permissions available</div>
          ) : (
            options.map(opt => (
              <label key={opt.id}
                className={`flex items-center gap-3 px-4 py-2.5 cursor-pointer hover:bg-gray-50 transition-colors
                  ${selected.includes(opt.id) ? 'bg-blue-50' : ''}`}>
                <div className={`w-4 h-4 rounded border-2 flex items-center justify-center flex-shrink-0 transition-colors
                  ${selected.includes(opt.id) ? 'bg-blue-600 border-blue-600' : 'border-gray-300'}`}>
                  {selected.includes(opt.id) && (
                    <svg width="8" height="8" viewBox="0 0 8 8" fill="none">
                      <path d="M1 4L3 6L7 2" stroke="white" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
                    </svg>
                  )}
                </div>
                <input type="checkbox" className="hidden" checked={selected.includes(opt.id)} onChange={() => toggle(opt.id)} />
                <span className="text-sm text-gray-700">{opt.permissionName}</span>
              </label>
            ))
          )}
        </div>
      )}
    </div>
  )
}

export default function RoleEditPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const roleId = Number(id)

  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' } | null>(null)
  const [selectedModuleId, setSelectedModuleId] = useState<number | null>(null)
  const [selectedMenuId, setSelectedMenuId] = useState<number | null>(null)
  const [selectedPermissions, setSelectedPermissions] = useState<number[]>([])
  const [searchQuery, setSearchQuery] = useState('')
  const [isActiveToggle, setIsActiveToggle] = useState<0 | 1>(1)

  const showToast = (msg: string, type: 'success' | 'error' = 'success') => {
    setToast({ message: msg, type })
    setTimeout(() => setToast(null), 3000)
  }

  // ── Queries ──
  const { data: roleDetails, isLoading: detailsLoading } = useQuery<RoleDetails>({
    queryKey: ['role-details-new', roleId],
    queryFn: async () => {
      const res = await axiosInstance.get(`/roles/${roleId}/details`)
      return res.data
    },
    enabled: !!roleId,
  })

  const { data: role } = useQuery({
    queryKey: ['roles', roleId],
    queryFn: () => roleApi.getById(roleId),
    enabled: !!roleId,
  })

  useEffect(() => {
    if (role) setIsActiveToggle(role.isActive)
  }, [role])

  const { data: modules = [] } = useQuery({
    queryKey: ['modules'],
    queryFn: moduleApi.getAll,
  })

  const { data: moduleMenus = [] } = useQuery({
    queryKey: ['menus', 'module', selectedModuleId],
    queryFn: () => menuApi.getByModule(Number(selectedModuleId)),
    enabled: selectedModuleId !== null,
  })

  // Only show leaf menus (isParent === 0) — parent menus have no permissions
  const leafMenus = moduleMenus.filter(m => m.isParent === 0)

  const { data: availablePermissions = [] } = useQuery<AvailablePermission[]>({
    queryKey: ['available-permissions', roleId, selectedModuleId, selectedMenuId],
    queryFn: async () => {
      const res = await axiosInstance.get(
        `/roles/${roleId}/available-permissions?moduleId=${selectedModuleId}&menuId=${selectedMenuId}`
      )
      return res.data
    },
    enabled: selectedModuleId !== null && selectedMenuId !== null,
  })

  const { register, handleSubmit, formState: { errors } } = useForm<CreateRoleRequest>({
    values: role ? { roleName: role.roleName, isActive: role.isActive } : undefined,
  })

  const assignedPermissions = roleDetails?.assignedPermissions ?? []

  const filteredAssignments = useMemo(() => {
    if (!searchQuery.trim()) return assignedPermissions
    const q = searchQuery.toLowerCase()
    return assignedPermissions.filter(a =>
      a.menuName.toLowerCase().includes(q) ||
      a.moduleName.toLowerCase().includes(q)
    )
  }, [assignedPermissions, searchQuery])

  // ── Mutations ──
  const updateRoleMutation = useMutation({
    mutationFn: (data: CreateRoleRequest) => roleApi.update(roleId, { ...data, id: roleId }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['roles'] })
      showToast('Role updated!')
    },
    onError: (e: any) => showToast(e?.message || 'Update failed.', 'error'),
  })

  const assignMutation = useMutation({
    mutationFn: async (payload: { moduleId: number; menuId: number; permissionIds: number[] }) => {
      const res = await axiosInstance.post(`/roles/${roleId}/permissions`, payload)
      return res.data as RoleDetails
    },
    onSuccess: (updatedDetails) => {
      queryClient.setQueryData(['role-details-new', roleId], updatedDetails)
      setSelectedModuleId(null)
      setSelectedMenuId(null)
      setSelectedPermissions([])
      showToast('Permissions assigned!')
    },
    onError: (e: any) => showToast(e?.message || 'Assign failed.', 'error'),
  })

  const deleteMutation = useMutation({
    mutationFn: (detailId: number) => roleDetailApi.delete(detailId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['role-details-new', roleId] })
      showToast('Assignment removed.')
    },
    onError: () => showToast('Remove failed.', 'error'),
  })

  const handleAssign = () => {
    if (!selectedModuleId) return showToast('Select a module.', 'error')
    if (!selectedMenuId) return showToast('Select a menu.', 'error')
    if (selectedPermissions.length === 0) return showToast('Select at least one permission.', 'error')
    assignMutation.mutate({
      moduleId: selectedModuleId,
      menuId: selectedMenuId,
      permissionIds: selectedPermissions,
    })
  }

  // DEBUG
useEffect(() => {
  console.log('🔵 selectedModuleId:', selectedModuleId, typeof selectedModuleId)
  console.log('🔵 selectedMenuId:', selectedMenuId, typeof selectedMenuId)
  console.log('🔵 enabled:', selectedModuleId !== null && selectedMenuId !== null)
}, [selectedModuleId, selectedMenuId])
// DEBUG
useEffect(() => {
  console.log('🟢 availablePermissions:', availablePermissions)
}, [availablePermissions])

// DEBUG  
useEffect(() => {
  console.log('🟡 moduleMenus:', moduleMenus)
  console.log('🟡 leafMenus:', leafMenus)
}, [moduleMenus])

  return (
    <div className="h-full bg-gray-50 flex flex-col">
      {toast && <Toast message={toast.message} type={toast.type} />}

      {/* Top bar */}
      <div className="bg-white border-b border-gray-200 px-6 py-3.5 flex items-center justify-between flex-shrink-0">
        <div className="flex items-center gap-3">
          <button onClick={() => navigate('/admin/roles')}
            className="p-1.5 rounded-lg text-gray-400 hover:text-gray-700 hover:bg-gray-100 transition-colors">
            <ArrowLeft size={16} />
          </button>
          <div className="flex items-center gap-2 text-sm text-gray-500">
            <span className="hover:text-blue-600 cursor-pointer" onClick={() => navigate('/admin/roles')}>Roles</span>
            <span>/</span>
            <span className="font-semibold text-gray-800">{roleDetails?.roleName ?? role?.roleName ?? 'Edit'}</span>
          </div>
        </div>
        <button
          onClick={() => navigate('/admin/roles')}
          className="flex items-center gap-2 px-4 py-2 bg-gray-800 hover:bg-gray-900 text-white text-sm font-medium rounded-lg transition-colors"
        >
          <List size={14} />
          List
        </button>
      </div>

      <div className="flex-1 overflow-y-auto p-5 space-y-4">

        {/* Role info card */}
        <div className="bg-white border border-gray-200 rounded-xl shadow-sm">
          <div className="flex items-center gap-2.5 px-5 py-3.5 border-b border-gray-100">
            <div className="w-7 h-7 bg-blue-600 rounded-lg flex items-center justify-center">
              <Shield size={13} className="text-white" />
            </div>
            <h2 className="text-sm font-semibold text-gray-800">Role Information</h2>
          </div>
          <div className="px-5 py-4">
            <form className="flex items-center gap-4">
              <div className="flex-1">
                <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wide mb-1.5">
                  Role Name <span className="text-red-400">*</span>
                </label>
                <input
                  {...register('roleName', { required: true })}
                  className={`w-full border rounded-lg px-3 py-2.5 text-sm outline-none transition-colors
                    ${errors.roleName ? 'border-red-300 bg-red-50' : 'border-gray-300 focus:border-blue-400 focus:ring-2 focus:ring-blue-50'}`}
                />
              </div>

              <div className="flex items-center gap-3 mt-5">
                <span className="text-sm font-medium text-gray-700">Is Active</span>
                <button
                  type="button"
                  onClick={() => setIsActiveToggle(isActiveToggle === 1 ? 0 : 1)}
                  className={`relative w-12 h-6 rounded-full transition-colors flex-shrink-0
                    ${isActiveToggle === 1 ? 'bg-blue-600' : 'bg-gray-300'}`}
                >
                  <span className={`absolute top-0.5 w-5 h-5 bg-white rounded-full shadow-sm transition-transform
                    ${isActiveToggle === 1 ? 'translate-x-6' : 'translate-x-0.5'}`} />
                </button>
              </div>

              <button
                type="button"
                onClick={handleSubmit(data => updateRoleMutation.mutate({ ...data, isActive: isActiveToggle }))}
                disabled={updateRoleMutation.isPending}
                className="mt-5 px-6 py-2.5 bg-blue-600 hover:bg-blue-700 disabled:opacity-50 text-white text-sm font-semibold rounded-lg transition-colors"
              >
                {updateRoleMutation.isPending ? 'Saving...' : 'Save'}
              </button>
            </form>
          </div>
        </div>

        {/* Assign permissions card */}
        <div className="bg-white border border-gray-200 rounded-xl shadow-sm overflow-hidden">
          <div className="flex items-center gap-2.5 px-5 py-3.5 border-b border-gray-100">
            <div className="w-7 h-7 bg-violet-100 rounded-lg flex items-center justify-center">
              <Key size={13} className="text-violet-600" />
            </div>
            <h2 className="text-sm font-semibold text-gray-800">Assign Permissions</h2>
          </div>
          <div className="px-5 py-4">
            <div className="flex items-end gap-3 flex-wrap">

              {/* Module */}
              <div className="w-52">
                <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wide mb-1.5">
                  Module Name <span className="text-red-400">*</span>
                </label>
                <select
                  value={selectedModuleId ?? ''}
                  onChange={e => {
                    setSelectedModuleId(e.target.value ? Number(e.target.value) : null)
                    setSelectedMenuId(null)
                    setSelectedPermissions([])
                  }}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2.5 text-sm outline-none focus:border-blue-400 focus:ring-2 focus:ring-blue-50 bg-white"
                >
                  <option value="">-- Select --</option>
                  {modules.map(m => <option key={m.id} value={m.id}>{m.moduleName}</option>)}
                </select>
              </div>

              {/* Menu */}
              <div className="w-52">
                <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wide mb-1.5">
                  Menu Name <span className="text-red-400">*</span>
                </label>
                <select
                  value={selectedMenuId ?? ''}
                  onChange={e => {
                    setSelectedMenuId(e.target.value ? Number(e.target.value) : null)
                    setSelectedPermissions([])
                  }}
                  disabled={selectedModuleId === null}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2.5 text-sm outline-none focus:border-blue-400 focus:ring-2 focus:ring-blue-50 bg-white disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  <option value="">-- Select --</option>
                  {leafMenus.map(m => <option key={m.id} value={m.id}>{m.menuName}</option>)}
                </select>
              </div>

              {/* Permission multi-select */}
              <div className="flex-1 min-w-64">
                <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wide mb-1.5">
                  Permission Name <span className="text-red-400">*</span>
                </label>
                <PermissionMultiSelect
                  options={availablePermissions}
                  selected={selectedPermissions}
                  onChange={setSelectedPermissions}
                  disabled={selectedMenuId === null}
                />
              </div>

  

              {/* Add button */}
              <button
                onClick={handleAssign}
                disabled={assignMutation.isPending || selectedPermissions.length === 0 || selectedMenuId === null}
                className="w-10 h-10 bg-blue-600 hover:bg-blue-700 disabled:opacity-40 disabled:cursor-not-allowed text-white rounded-lg flex items-center justify-center transition-colors flex-shrink-0 text-lg font-bold shadow-sm"
                title="Assign"
              >
                {assignMutation.isPending
                  ? <span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                  : '+'}
              </button>
            </div>
          </div>
        </div>

        {/* Permission list card */}
        <div className="bg-white border border-gray-200 rounded-xl shadow-sm overflow-hidden">
          <div className="flex items-center justify-between px-5 py-3.5 border-b border-gray-100">
            <div className="flex items-center gap-2">
              <h2 className="text-sm font-semibold text-gray-800">Permission List</h2>
              {assignedPermissions.length > 0 && (
                <span className="text-xs bg-blue-100 text-blue-700 px-2 py-0.5 rounded-full font-medium">
                  {assignedPermissions.length}
                </span>
              )}
            </div>
            <div className="flex items-center gap-2 border border-gray-200 rounded-lg px-3 py-1.5 w-56 focus-within:border-blue-400 focus-within:ring-2 focus-within:ring-blue-50 transition-all">
              <Search size={13} className="text-gray-400 flex-shrink-0" />
              <input
                type="text"
                placeholder="Search by Menu Name..."
                value={searchQuery}
                onChange={e => setSearchQuery(e.target.value)}
                className="text-sm outline-none bg-transparent w-full text-gray-600 placeholder-gray-400"
              />
              {searchQuery && (
                <button onClick={() => setSearchQuery('')} className="text-gray-400 hover:text-gray-600">
                  <X size={12} />
                </button>
              )}
            </div>
          </div>

          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-gray-100 bg-gray-50/80">
                <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wide">Module Name</th>
                <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wide">Menu Name</th>
                <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wide">Permission</th>
                <th className="text-center px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wide w-20">Action</th>
              </tr>
            </thead>
            <tbody>
              {detailsLoading ? (
                <tr><td colSpan={4} className="text-center py-10 text-gray-400 text-sm">Loading...</td></tr>
              ) : filteredAssignments.length === 0 ? (
                <tr>
                  <td colSpan={4} className="text-center py-14">
                    <div className="flex flex-col items-center gap-2">
                      <div className="w-14 h-14 bg-gray-100 rounded-2xl flex items-center justify-center">
                        <Key size={22} className="text-gray-300" />
                      </div>
                      <p className="text-sm font-semibold text-gray-400">
                        {searchQuery ? 'No results found' : 'Nothing added yet'}
                      </p>
                      <p className="text-xs text-gray-300">
                        {searchQuery ? 'Try a different keyword' : 'Use the form above to assign permissions'}
                      </p>
                    </div>
                  </td>
                </tr>
              ) : (
                filteredAssignments.map((detail, idx) => (
                  <tr key={detail.id} className={`border-b border-gray-50 hover:bg-gray-50/80 transition-colors ${idx % 2 === 0 ? '' : 'bg-gray-50/40'}`}>
                    <td className="px-5 py-3.5 font-semibold text-gray-800">{detail.moduleName}</td>
                    <td className="px-5 py-3.5 text-gray-600">{detail.menuName}</td>
                    <td className="px-5 py-3.5">
                      <div className="flex flex-wrap gap-1.5">
                        {detail.permissions.map(p => (
                          <span key={p.id} className="inline-flex items-center px-2 py-0.5 bg-blue-50 text-blue-700 border border-blue-100 rounded-md text-xs font-medium">
                            {p.permissionName}
                          </span>
                        ))}
                      </div>
                    </td>
                    <td className="px-5 py-3.5 text-center">
                      <button
                        onClick={() => { if (confirm('Remove this assignment?')) deleteMutation.mutate(detail.id) }}
                        className="p-1.5 rounded-lg text-red-400 hover:text-red-600 hover:bg-red-50 transition-colors inline-flex items-center justify-center"
                      >
                        <Trash2 size={15} />
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

      </div>
    </div>
  )
}