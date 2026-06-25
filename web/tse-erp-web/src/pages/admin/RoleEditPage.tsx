// src/pages/admin/RoleEditPage.tsx

import { useState, useMemo, useRef, useEffect } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { ArrowLeft, Trash2, ChevronDown, X, Search, List } from 'lucide-react'
import { roleApi } from '../../api/roleApi'
import { roleDetailApi } from '../../api/roleDetailApi'
import { moduleApi } from '../../api/moduleApi'
import { permissionApi } from '../../api/permissionApi'
import { menuApi } from '../../api/menuApi'
import type { CreateRoleRequest } from '../../types/role.types'

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

// ── Multi-select permission dropdown ────────────────────────────────────────
function PermissionMultiSelect({
  options,
  selected,
  onChange,
  disabled,
}: {
  options: { id: number; permissionName: string }[]
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
    <div ref={ref} className="relative flex-1">
      <div
        onClick={() => !disabled && setOpen(!open)}
        className={`min-h-[38px] border rounded-lg px-2 py-1.5 flex flex-wrap gap-1 items-center cursor-pointer bg-white transition-colors
          ${disabled ? 'opacity-50 cursor-not-allowed bg-gray-50' : 'hover:border-blue-400'}
          ${open ? 'border-blue-400 ring-2 ring-blue-50' : 'border-gray-300'}`}
      >
        {selectedOptions.length === 0 ? (
          <span className="text-sm text-gray-400 px-1">Select permissions...</span>
        ) : (
          selectedOptions.map(opt => (
            <span key={opt.id} className="flex items-center gap-1 bg-blue-100 text-blue-700 text-xs font-medium px-2 py-0.5 rounded-md">
              {opt.permissionName}
              <button onClick={e => remove(opt.id, e)} className="hover:text-blue-900">
                <X size={10} />
              </button>
            </span>
          ))
        )}
        <ChevronDown size={14} className={`ml-auto text-gray-400 flex-shrink-0 transition-transform ${open ? 'rotate-180' : ''}`} />
      </div>

      {open && options.length > 0 && (
        <div className="absolute top-full left-0 right-0 mt-1 bg-white border border-gray-200 rounded-lg shadow-lg z-20 max-h-48 overflow-y-auto">
          {options.map(opt => (
            <label key={opt.id}
              className={`flex items-center gap-2.5 px-3 py-2 cursor-pointer hover:bg-gray-50 transition-colors
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
          ))}
        </div>
      )}

      {open && options.length === 0 && (
        <div className="absolute top-full left-0 right-0 mt-1 bg-white border border-gray-200 rounded-lg shadow-lg z-20 px-3 py-3 text-sm text-gray-400 text-center">
          No permissions available
        </div>
      )}
    </div>
  )
}

// ── Main Page ────────────────────────────────────────────────────────────────
export default function RoleEditPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const roleId = Number(id)

  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' } | null>(null)
  const [selectedModuleId, setSelectedModuleId] = useState<number | ''>('')
  const [selectedMenuId, setSelectedMenuId] = useState<number | ''>('')
  const [selectedPermissions, setSelectedPermissions] = useState<number[]>([])
  const [searchQuery, setSearchQuery] = useState('')

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

  // ── Role form ──
  const { register, handleSubmit, watch } = useForm<CreateRoleRequest>({
    values: role ? { roleName: role.roleName, isActive: role.isActive } : undefined,
  })
  const isActiveValue = watch('isActive')

  // ── Already assigned permissions for this module+menu ──
  const alreadyAssignedPermIds = useMemo(() => {
    if (!selectedModuleId || !selectedMenuId) return new Set<number>()
    const ids = new Set<number>()
    assignments
      .filter(a => a.moduleId === Number(selectedModuleId) && a.menuId === Number(selectedMenuId))
      .forEach(a => parsePermissionIds(a.permissionId).forEach(id => ids.add(id)))
    return ids
  }, [assignments, selectedModuleId, selectedMenuId])

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

  const moduleMap = useMemo(() => {
    const map = new Map<number, string>()
    modules.forEach(m => map.set(m.id, m.moduleName))
    return map
  }, [modules])

  // ── Filtered assignments ──
  const filteredAssignments = useMemo(() => {
    if (!searchQuery.trim()) return assignments
    const q = searchQuery.toLowerCase()
    return assignments.filter(a =>
      (menuMap.get(a.menuId) ?? '').toLowerCase().includes(q) ||
      (moduleMap.get(a.moduleId) ?? '').toLowerCase().includes(q)
    )
  }, [assignments, searchQuery, menuMap, moduleMap])

  // ── Mutations ──
  const updateRoleMutation = useMutation({
    mutationFn: (data: CreateRoleRequest) => roleApi.update(roleId, { ...data, id: roleId }),
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
      showToast('Assignment removed.')
    },
    onError: () => showToast('Failed to remove.', 'error'),
  })

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

  const getPermissionNames = (raw: string) =>
    parsePermissionIds(raw).map(id => permissionMap.get(id) ?? `#${id}`).join(', ')

  return (
    <div className="h-full bg-gray-50 flex flex-col">
      {toast && <Toast message={toast.message} type={toast.type} />}

      {/* Top bar */}
      <div className="bg-white border-b border-gray-200 px-6 py-4 flex items-center justify-between flex-shrink-0">
        <div className="flex items-center gap-3">
          <button onClick={() => navigate('/admin/roles')}
            className="p-2 rounded-lg text-gray-400 hover:text-gray-700 hover:bg-gray-100 transition-colors">
            <ArrowLeft size={16} />
          </button>
          <div>
            <h1 className="text-base font-bold text-gray-800">Role Edit</h1>
            <p className="text-xs text-gray-400 mt-0.5">Admin / Roles / {role?.roleName ?? 'Edit'}</p>
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

        {/* ── Role name + status row ── */}
        <div className="bg-white border border-gray-200 rounded-xl p-5 shadow-sm">
          <form className="flex items-end gap-4 flex-wrap">

            {/* Role Name */}
            <div className="flex-1 min-w-48">
              <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wide mb-1.5">
                Role Name <span className="text-red-400">*</span>
              </label>
              <input
                {...register('roleName', { required: true })}
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm outline-none focus:border-blue-400 focus:ring-2 focus:ring-blue-50 transition-colors"
              />
            </div>

            {/* Is Active toggle */}
            <div className="flex items-center gap-3 pb-2">
              <span className="text-sm text-gray-600 font-medium">Is Active</span>
              <button
                type="button"
                onClick={() => {
                  const form = document.querySelector('form')
                  if (form) {
                    const input = form.querySelector('input[name="isActive"]') as HTMLInputElement
                    if (input) input.value = isActiveValue == 1 ? '0' : '1'
                  }
                }}
                className={`relative w-11 h-6 rounded-full transition-colors flex-shrink-0
                  ${Number(isActiveValue) === 1 ? 'bg-blue-600' : 'bg-gray-300'}`}
              >
                <span className={`absolute top-0.5 w-5 h-5 bg-white rounded-full shadow transition-transform
                  ${Number(isActiveValue) === 1 ? 'translate-x-5' : 'translate-x-0.5'}`} />
                <input type="hidden" {...register('isActive')} />
              </button>
            </div>

            {/* Save button */}
            <button
              type="button"
              onClick={handleSubmit(data => updateRoleMutation.mutate({ ...data, isActive: Number(data.isActive) as 0 | 1 }))}
              disabled={updateRoleMutation.isPending}
              className="px-5 py-2 bg-blue-600 hover:bg-blue-700 disabled:opacity-50 text-white text-sm font-semibold rounded-lg transition-colors"
            >
              {updateRoleMutation.isPending ? 'Saving...' : 'Save'}
            </button>
          </form>
        </div>

        {/* ── Module + Menu + Permission row ── */}
        <div className="bg-white border border-gray-200 rounded-xl p-5 shadow-sm">
          <div className="flex items-end gap-3 flex-wrap">

            {/* Module */}
            <div className="w-52">
              <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wide mb-1.5">
                Module Name <span className="text-red-400">*</span>
              </label>
              <select
                value={selectedModuleId}
                onChange={e => {
                  setSelectedModuleId(e.target.value ? Number(e.target.value) : '')
                  setSelectedMenuId('')
                  setSelectedPermissions([])
                }}
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm outline-none focus:border-blue-400 focus:ring-2 focus:ring-blue-50 bg-white"
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
                value={selectedMenuId}
                onChange={e => { setSelectedMenuId(e.target.value ? Number(e.target.value) : ''); setSelectedPermissions([]) }}
                disabled={!selectedModuleId}
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm outline-none focus:border-blue-400 focus:ring-2 focus:ring-blue-50 bg-white disabled:opacity-50 disabled:cursor-not-allowed"
              >
                <option value="">-- Select --</option>
                {moduleMenus.map(m => <option key={m.id} value={m.id}>{m.menuName}</option>)}
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
                disabled={!selectedMenuId}
              />
            </div>

            {/* Add button */}
            <button
              onClick={handleAssign}
              disabled={assignMutation.isPending || selectedPermissions.length === 0 || !selectedMenuId}
              className="w-10 h-10 bg-blue-600 hover:bg-blue-700 disabled:opacity-40 disabled:cursor-not-allowed text-white rounded-lg flex items-center justify-center transition-colors flex-shrink-0 text-xl font-bold"
              title="Assign"
            >
              {assignMutation.isPending ? (
                <span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
              ) : '+'}
            </button>
          </div>
        </div>

        {/* ── Permission list ── */}
        <div className="bg-white border border-gray-200 rounded-xl shadow-sm overflow-hidden">
          <div className="flex items-center justify-between px-5 py-3.5 border-b border-gray-100">
            <h2 className="text-sm font-semibold text-gray-800">Permission List</h2>
            {/* Search */}
            <div className="flex items-center gap-2 border border-gray-200 rounded-lg px-3 py-1.5 w-56">
              <Search size={13} className="text-gray-400 flex-shrink-0" />
              <input
                type="text"
                placeholder="Search by Menu Name..."
                value={searchQuery}
                onChange={e => setSearchQuery(e.target.value)}
                className="text-sm outline-none bg-transparent w-full text-gray-600 placeholder-gray-400"
              />
            </div>
          </div>

          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-gray-100 bg-gray-50">
                <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wide">Module Name</th>
                <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wide">Menu Name</th>
                <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wide">Permission</th>
                <th className="text-center px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wide">Action</th>
              </tr>
            </thead>
            <tbody>
              {assignLoading ? (
                <tr><td colSpan={4} className="text-center py-10 text-gray-400">Loading...</td></tr>
              ) : filteredAssignments.length === 0 ? (
                <tr>
                  <td colSpan={4} className="text-center py-12">
                    <div className="flex flex-col items-center gap-2">
                      <div className="w-12 h-12 bg-gray-100 rounded-xl flex items-center justify-center">
                        <List size={20} className="text-gray-300" />
                      </div>
                      <p className="text-sm font-medium text-gray-400">
                        {searchQuery ? 'No results found' : 'Nothing added yet'}
                      </p>
                      <p className="text-xs text-gray-300">
                        {searchQuery ? 'Try a different search' : 'Use the form above to assign permissions'}
                      </p>
                    </div>
                  </td>
                </tr>
              ) : (
                filteredAssignments.map(detail => (
                  <tr key={detail.id} className="border-b border-gray-50 hover:bg-gray-50 transition-colors">
                    <td className="px-5 py-3 font-medium text-gray-800">
                      {moduleMap.get(detail.moduleId) ?? `Module ${detail.moduleId}`}
                    </td>
                    <td className="px-5 py-3 text-gray-600">
                      {menuMap.get(detail.menuId) ?? `Menu ${detail.menuId}`}
                    </td>
                    <td className="px-5 py-3 text-gray-500">
                      {getPermissionNames(detail.permissionId)}
                    </td>
                    <td className="px-5 py-3 text-center">
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