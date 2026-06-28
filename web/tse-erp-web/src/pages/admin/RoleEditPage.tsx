// src/pages/admin/RoleEditPage.tsx

import { useState, useMemo, useRef, useEffect } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { ArrowLeft, Trash2, ChevronDown, X, Search, Shield, Key, Plus, ChevronRight, Pencil } from 'lucide-react'
import axiosInstance from '../../api/axiosInstance'
import { moduleApi } from '../../api/moduleApi'
import { menuApi } from '../../api/menuApi'
import { roleApi } from '../../api/roleApi'
import { roleDetailApi } from '../../api/roleDetailApi'
import type { CreateRoleRequest } from '../../types/role.types'

interface PermissionItem { id: number; permissionName: string }
interface AssignedPermission {
  id: number; moduleId: number; moduleName: string
  menuId: number; menuName: string; permissions: PermissionItem[]
}
interface RoleDetails { roleId: number; roleName: string; assignedPermissions: AssignedPermission[] }

function getChipColor(name: string) {
  const n = name.toLowerCase()
  if (n.includes('delete') || n.includes('remove')) return 'bg-red-100 text-red-700 border-red-200'
  if (n.includes('create') || n.includes('add')) return 'bg-green-100 text-green-700 border-green-200'
  if (n.includes('edit') || n.includes('update')) return 'bg-amber-100 text-amber-700 border-amber-200'
  if (n.includes('view') || n.includes('list')) return 'bg-blue-100 text-blue-700 border-blue-200'
  return 'bg-gray-100 text-gray-600 border-gray-200'
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

function ConfirmModal({ isOpen, title, message, onConfirm, onCancel, confirmLabel = 'Confirm', danger = false }: {
  isOpen: boolean; title: string; message: string
  onConfirm: () => void; onCancel: () => void
  confirmLabel?: string; danger?: boolean
}) {
  if (!isOpen) return null
  return (
    <div className="fixed inset-0 z-40 flex items-center justify-center">
      <div className="absolute inset-0 bg-black/30 backdrop-blur-sm" onClick={onCancel} />
      <div className="relative bg-white rounded-xl shadow-xl w-full max-w-sm mx-4 z-50 p-6">
        <h3 className="text-base font-semibold text-gray-800 mb-2">{title}</h3>
        <p className="text-sm text-gray-500 mb-6">{message}</p>
        <div className="flex gap-3">
          <button onClick={onCancel}
            className="flex-1 py-2 border border-gray-200 hover:bg-gray-50 text-gray-600 text-sm font-medium rounded-lg">
            Cancel
          </button>
          <button onClick={onConfirm}
            className={`flex-1 py-2 text-white text-sm font-semibold rounded-lg transition-colors
              ${danger ? 'bg-red-600 hover:bg-red-700' : 'bg-blue-600 hover:bg-blue-700'}`}>
            {confirmLabel}
          </button>
        </div>
      </div>
    </div>
  )
}

function SkeletonRow() {
  return (
    <tr className="border-b border-gray-50">
      <td className="px-5 py-3.5"><div className="h-4 w-28 bg-gray-100 rounded animate-pulse" /></td>
      <td className="px-5 py-3.5"><div className="h-4 w-24 bg-gray-100 rounded animate-pulse" /></td>
      <td className="px-5 py-3.5"><div className="flex gap-1.5"><div className="h-6 w-20 bg-gray-100 rounded-full animate-pulse" /><div className="h-6 w-16 bg-gray-100 rounded-full animate-pulse" /></div></td>
      <td className="px-5 py-3.5"><div className="h-6 w-12 bg-gray-100 rounded animate-pulse mx-auto" /></td>
    </tr>
  )
}

function PermissionMultiSelect({ options, selected, onChange, disabled }: {
  options: PermissionItem[]; selected: number[]
  onChange: (ids: number[]) => void; disabled: boolean
}) {
  const [open, setOpen] = useState(false)
  const ref = useRef<HTMLDivElement>(null)
  const [dropdownStyle, setDropdownStyle] = useState<React.CSSProperties>({})

  useEffect(() => {
    const handler = (e: MouseEvent) => {
      if (ref.current && !ref.current.contains(e.target as Node)) setOpen(false)
    }
    document.addEventListener('mousedown', handler)
    return () => document.removeEventListener('mousedown', handler)
  }, [])

  const openDropdown = () => {
    if (disabled) return
    if (ref.current) {
      const rect = ref.current.getBoundingClientRect()
      setDropdownStyle({ position: 'fixed', top: rect.bottom + window.scrollY + 4, left: rect.left, width: rect.width, zIndex: 9999 })
    }
    setOpen(!open)
  }

  const toggle = (id: number) => onChange(selected.includes(id) ? selected.filter(s => s !== id) : [...selected, id])
  const remove = (id: number, e: React.MouseEvent) => { e.stopPropagation(); onChange(selected.filter(s => s !== id)) }
  const selectedOptions = options.filter(o => selected.includes(o.id))

  return (
    <div ref={ref} className="relative flex-1 min-w-0">
      <div onClick={openDropdown}
        className={`min-h-[40px] border rounded-lg px-3 py-1.5 flex flex-wrap gap-1.5 items-center cursor-pointer bg-white transition-all
          ${disabled ? 'opacity-50 cursor-not-allowed bg-gray-50 border-gray-200' : 'border-gray-300 hover:border-blue-400'}
          ${open ? 'border-blue-500 ring-2 ring-blue-100' : ''}`}>
        {selectedOptions.length === 0
          ? <span className="text-sm text-gray-400 py-0.5">Select permissions...</span>
          : selectedOptions.map(opt => (
            <span key={opt.id} className="flex items-center gap-1 bg-blue-100 text-blue-700 text-xs font-medium px-2 py-0.5 rounded-md border border-blue-200">
              {opt.permissionName}
              <button onClick={e => remove(opt.id, e)} className="hover:text-blue-900 ml-0.5"><X size={10} /></button>
            </span>
          ))}
        <ChevronDown size={14} className={`ml-auto text-gray-400 flex-shrink-0 transition-transform ${open ? 'rotate-180' : ''}`} />
      </div>
      {open && (
        <div style={dropdownStyle} className="bg-white border border-gray-200 rounded-xl shadow-xl max-h-52 overflow-y-auto">
          {options.length === 0
            ? <div className="px-4 py-4 text-sm text-gray-400 text-center">No permissions available</div>
            : options.map(opt => (
              <label key={opt.id} className={`flex items-center gap-3 px-4 py-2.5 cursor-pointer hover:bg-gray-50 transition-colors ${selected.includes(opt.id) ? 'bg-blue-50' : ''}`}>
                <div className={`w-4 h-4 rounded border-2 flex items-center justify-center flex-shrink-0
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
  const [editingRowId, setEditingRowId] = useState<number | null>(null) // which row is being edited
  const [pendingAssign, setPendingAssign] = useState<{ moduleId: number; menuId: number; permissionIds: number[] } | null>(null)
  const [confirmModal, setConfirmModal] = useState<{
    open: boolean; title: string; message: string; onConfirm: () => void; danger?: boolean; confirmLabel?: string
  }>({ open: false, title: '', message: '', onConfirm: () => {} })

  const showToast = (msg: string, type: 'success' | 'error' = 'success') => {
    setToast({ message: msg, type })
    setTimeout(() => setToast(null), 3000)
  }

  const openConfirm = (title: string, message: string, onConfirm: () => void, danger = false, confirmLabel = 'Confirm') => {
    setConfirmModal({ open: true, title, message, onConfirm, danger, confirmLabel })
  }

  // ── Queries ──
  const { data: roleDetails, isLoading: detailsLoading } = useQuery<RoleDetails>({
    queryKey: ['role-details-new', roleId],
    queryFn: async () => (await axiosInstance.get(`/roles/${roleId}/details`)).data,
    enabled: !!roleId,
  })

  const { data: role } = useQuery({
    queryKey: ['roles', roleId],
    queryFn: () => roleApi.getById(roleId),
    enabled: !!roleId,
  })

  useEffect(() => { if (role) setIsActiveToggle(role.isActive) }, [role])

  const { data: modules = [] } = useQuery({ queryKey: ['modules'], queryFn: moduleApi.getAll })

  const { data: moduleMenus = [] } = useQuery({
    queryKey: ['menus', 'module', selectedModuleId],
    queryFn: () => menuApi.getByModule(Number(selectedModuleId)),
    enabled: selectedModuleId !== null,
  })

  const leafMenus = moduleMenus.filter(m => m.isParent === 0)

  const { data: availablePermissions = [] } = useQuery<PermissionItem[]>({
    queryKey: ['available-permissions', roleId, selectedModuleId, selectedMenuId],
    queryFn: async () => (await axiosInstance.get(
      `/roles/${roleId}/available-permissions?moduleId=${selectedModuleId}&menuId=${selectedMenuId}`
    )).data,
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
      a.moduleName.toLowerCase().includes(q) ||
      a.permissions.some(p => p.permissionName.toLowerCase().includes(q))
    )
  }, [assignedPermissions, searchQuery])

  // ── Mutations ──
  const updateRoleMutation = useMutation({
    mutationFn: (data: CreateRoleRequest) => roleApi.update(roleId, { ...data, id: roleId }),
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['roles'] }); showToast('Role updated!') },
    onError: (e: any) => showToast(e?.message || 'Update failed.', 'error'),
  })

  const assignMutation = useMutation({
    mutationFn: async (payload: { moduleId: number; menuId: number; permissionIds: number[] }) =>
      (await axiosInstance.post(`/roles/${roleId}/permissions`, payload)).data as RoleDetails,
    onSuccess: (updated) => {
      queryClient.setQueryData(['role-details-new', roleId], updated)
      queryClient.invalidateQueries({ queryKey: ['available-permissions', roleId, selectedModuleId, selectedMenuId] })
      setSelectedModuleId(null); setSelectedMenuId(null); setSelectedPermissions([])
      setEditingRowId(null); setPendingAssign(null)
      showToast('Permissions assigned!')
    },
    onError: (e: any) => showToast(e?.message || 'Assign failed.', 'error'),
  })

  const deleteGroupMutation = useMutation({
    mutationFn: (detailId: number) => roleDetailApi.delete(detailId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['role-details-new', roleId] })
      if (editingRowId !== null) { setEditingRowId(null); setSelectedModuleId(null); setSelectedMenuId(null); setSelectedPermissions([]) }
      showToast('Permission group removed.')
    },
    onError: () => showToast('Remove failed.', 'error'),
  })

  const removePermissionMutation = useMutation({
    mutationFn: async ({ roleDetailId, permissionId }: { roleDetailId: number; permissionId: number }) =>
      (await axiosInstance.delete(`/role-details/${roleDetailId}/permissions/${permissionId}`)).data,
    onSuccess: (response) => {
      const updated = response.data as RoleDetails
      queryClient.setQueryData(['role-details-new', roleId], updated)
      queryClient.invalidateQueries({ queryKey: ['available-permissions', roleId, selectedModuleId, selectedMenuId] })
      showToast('Permission removed.')
    },
    onError: () => showToast('Remove failed.', 'error'),
  })

  // ── Edit row — pre-select module+menu ──
  const handleEditRow = (detail: AssignedPermission) => {
    if (editingRowId === detail.id) {
      // cancel edit
      setEditingRowId(null); setSelectedModuleId(null); setSelectedMenuId(null); setSelectedPermissions([])
      return
    }
    setEditingRowId(detail.id)
    setSelectedModuleId(detail.moduleId)
    setSelectedMenuId(detail.menuId)
    setSelectedPermissions([])
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  const handleAssign = () => {
    if (!selectedModuleId) return showToast('Select a module.', 'error')
    if (!selectedMenuId) return showToast('Select a menu.', 'error')
    if (selectedPermissions.length === 0) return showToast('Select at least one permission.', 'error')
    const payload = { moduleId: selectedModuleId, menuId: selectedMenuId, permissionIds: selectedPermissions }
    setPendingAssign(payload)
    openConfirm(
      'Add Permissions',
      `Add ${selectedPermissions.length} permission(s) to this role?`,
      () => assignMutation.mutate(payload),
      false,
      'Add'
    )
  }

  const isEditing = editingRowId !== null

  return (
    <div className="h-full bg-gray-50 flex flex-col">
      {toast && <Toast message={toast.message} type={toast.type} />}

      <ConfirmModal
        isOpen={confirmModal.open}
        title={confirmModal.title}
        message={confirmModal.message}
        danger={confirmModal.danger}
        confirmLabel={confirmModal.confirmLabel}
        onConfirm={() => { confirmModal.onConfirm(); setConfirmModal(m => ({ ...m, open: false })) }}
        onCancel={() => { setConfirmModal(m => ({ ...m, open: false })); setPendingAssign(null) }}
      />

      {/* Top bar */}
      <div className="bg-white border-b border-gray-200 px-6 py-3.5 flex items-center justify-between flex-shrink-0">
        <div className="flex items-center gap-2 text-sm">
          <button onClick={() => navigate('/admin/roles')}
            className="p-1.5 rounded-lg text-gray-400 hover:text-gray-700 hover:bg-gray-100 transition-colors mr-1">
            <ArrowLeft size={16} />
          </button>
          <span className="text-gray-400 hover:text-blue-600 cursor-pointer" onClick={() => navigate('/admin/roles')}>Roles</span>
          <ChevronRight size={13} className="text-gray-300" />
          <span className="text-gray-400">Edit Role</span>
          <ChevronRight size={13} className="text-gray-300" />
          <span className="font-semibold text-gray-800">{roleDetails?.roleName ?? role?.roleName ?? '...'}</span>
        </div>
      </div>

      <div className="flex-1 overflow-y-auto p-5 space-y-4">

        {/* Role info */}
        <div className="bg-white border border-gray-200 rounded-xl shadow-sm overflow-hidden">
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
                <input {...register('roleName', { required: true })}
                  className={`w-full border rounded-lg px-3 py-2.5 text-sm outline-none transition-colors
                    ${errors.roleName ? 'border-red-300 bg-red-50' : 'border-gray-300 focus:border-blue-400 focus:ring-2 focus:ring-blue-50'}`} />
              </div>
              <div className="flex items-center gap-3">
                <span className="text-sm font-medium text-gray-700">Is Active</span>
                <button type="button" onClick={() => setIsActiveToggle(isActiveToggle === 1 ? 0 : 1)}
                  className={`relative w-12 h-6 rounded-full transition-colors flex-shrink-0 ${isActiveToggle === 1 ? 'bg-blue-600' : 'bg-gray-300'}`}>
                  <span className={`absolute top-0.5 w-5 h-5 bg-white rounded-full shadow-sm transition-transform ${isActiveToggle === 1 ? 'translate-x-6' : 'translate-x-0.5'}`} />
                </button>
              </div>
              <button type="button" disabled={updateRoleMutation.isPending}
                onClick={handleSubmit(data => updateRoleMutation.mutate({ ...data, isActive: isActiveToggle }))}
                className="px-6 py-2.5 bg-blue-600 hover:bg-blue-700 disabled:opacity-50 text-white text-sm font-semibold rounded-lg transition-colors">
                {updateRoleMutation.isPending ? 'Saving...' : 'Save'}
              </button>
            </form>
          </div>
        </div>

        {/* Assign permissions */}
        <div className={`bg-white border rounded-xl shadow-sm transition-all ${isEditing ? 'border-amber-300 ring-2 ring-amber-100' : 'border-gray-200'}`}>
          <div className="flex items-center gap-2.5 px-5 py-3.5 border-b border-gray-100">
            <div className={`w-7 h-7 rounded-lg flex items-center justify-center ${isEditing ? 'bg-amber-100' : 'bg-violet-100'}`}>
              <Key size={13} className={isEditing ? 'text-amber-600' : 'text-violet-600'} />
            </div>
            <h2 className="text-sm font-semibold text-gray-800">
              {isEditing ? (
                <span className="flex items-center gap-2">
                  Editing permissions
                  <span className="text-xs bg-amber-100 text-amber-700 px-2 py-0.5 rounded-full font-medium border border-amber-200">
                    {assignedPermissions.find(a => a.id === editingRowId)?.menuName}
                  </span>
                  <button onClick={() => { setEditingRowId(null); setSelectedModuleId(null); setSelectedMenuId(null); setSelectedPermissions([]) }}
                    className="text-xs text-gray-400 hover:text-gray-600 ml-1">
                    cancel
                  </button>
                </span>
              ) : 'Assign Permissions'}
            </h2>
          </div>
          <div className="px-5 py-4">
            <div className="flex items-end gap-3 flex-wrap">
              <div className="w-52">
                <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wide mb-1.5">Module Name <span className="text-red-400">*</span></label>
                <select value={selectedModuleId ?? ''}
                  onChange={e => { setSelectedModuleId(e.target.value ? Number(e.target.value) : null); setSelectedMenuId(null); setSelectedPermissions([]) }}
                  disabled={isEditing}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2.5 text-sm outline-none focus:border-blue-400 focus:ring-2 focus:ring-blue-50 bg-white disabled:opacity-60 disabled:cursor-not-allowed">
                  <option value="">-- Select --</option>
                  {modules.map(m => <option key={m.id} value={m.id}>{m.moduleName}</option>)}
                </select>
              </div>

              <div className="w-52">
                <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wide mb-1.5">Menu Name <span className="text-red-400">*</span></label>
                <select value={selectedMenuId ?? ''}
                  onChange={e => { setSelectedMenuId(e.target.value ? Number(e.target.value) : null); setSelectedPermissions([]) }}
                  disabled={selectedModuleId === null || isEditing}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2.5 text-sm outline-none focus:border-blue-400 focus:ring-2 focus:ring-blue-50 bg-white disabled:opacity-60 disabled:cursor-not-allowed">
                  <option value="">-- Select --</option>
                  {leafMenus.map(m => <option key={m.id} value={m.id}>{m.menuName}</option>)}
                </select>
              </div>

              <div className="flex-1 min-w-64">
                <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wide mb-1.5">Permission Name <span className="text-red-400">*</span></label>
                <PermissionMultiSelect
                  options={availablePermissions}
                  selected={selectedPermissions}
                  onChange={setSelectedPermissions}
                  disabled={selectedMenuId === null}
                />
              </div>

              <button onClick={handleAssign}
                disabled={assignMutation.isPending || selectedPermissions.length === 0 || selectedMenuId === null}
                className="flex items-center gap-2 px-4 h-10 bg-blue-600 hover:bg-blue-700 disabled:opacity-40 disabled:cursor-not-allowed text-white text-sm font-semibold rounded-lg transition-colors flex-shrink-0">
                {assignMutation.isPending
                  ? <span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                  : <><Plus size={14} /> Add Permission</>}
              </button>
            </div>
          </div>
        </div>

        {/* Permission list */}
        <div className="bg-white border border-gray-200 rounded-xl shadow-sm overflow-hidden">
          <div className="flex items-center justify-between px-5 py-3.5 border-b border-gray-100">
            <div className="flex items-center gap-2">
              <h2 className="text-sm font-semibold text-gray-800">Permission List</h2>
              {assignedPermissions.length > 0 && (
                <span className="text-xs bg-blue-100 text-blue-700 px-2 py-0.5 rounded-full font-medium">{assignedPermissions.length}</span>
              )}
            </div>
            <div className="flex items-center gap-2 border border-gray-200 rounded-lg px-3 py-1.5 w-64 focus-within:border-blue-400 focus-within:ring-2 focus-within:ring-blue-50 transition-all">
              <Search size={13} className="text-gray-400 flex-shrink-0" />
              <input type="text" placeholder="Search module, menu, permission..."
                value={searchQuery} onChange={e => setSearchQuery(e.target.value)}
                className="text-sm outline-none bg-transparent w-full text-gray-600 placeholder-gray-400" />
              {searchQuery && <button onClick={() => setSearchQuery('')} className="text-gray-400 hover:text-gray-600"><X size={12} /></button>}
            </div>
          </div>

          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-gray-100 bg-gray-50/80">
                <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wide">Module Name</th>
                <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wide">Menu Name</th>
                <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wide">Permission</th>
                <th className="text-center px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wide w-24">Action</th>
              </tr>
            </thead>
            <tbody>
              {detailsLoading ? (
                <>{[1,2,3].map(i => <SkeletonRow key={i} />)}</>
              ) : filteredAssignments.length === 0 ? (
                <tr>
                  <td colSpan={4} className="text-center py-14">
                    <div className="flex flex-col items-center gap-2">
                      <div className="w-14 h-14 bg-gray-100 rounded-2xl flex items-center justify-center">
                        <Key size={22} className="text-gray-300" />
                      </div>
                      <p className="text-sm font-semibold text-gray-400">
                        {searchQuery ? 'No results found' : 'No permissions assigned yet.'}
                      </p>
                      <p className="text-xs text-gray-300">
                        {searchQuery ? 'Try a different keyword' : 'Use the form above to assign permissions'}
                      </p>
                    </div>
                  </td>
                </tr>
              ) : (
                filteredAssignments.map((detail, idx) => {
                  const isEditingThis = editingRowId === detail.id
                  return (
                    <tr key={detail.id}
                      className={`border-b border-gray-50 transition-colors
                        ${isEditingThis ? 'bg-amber-50/60' : idx % 2 === 0 ? 'hover:bg-gray-50/80' : 'bg-gray-50/40 hover:bg-gray-50/80'}`}>
                      <td className="px-5 py-3.5 font-semibold text-gray-800">{detail.moduleName}</td>
                      <td className="px-5 py-3.5 text-gray-600">{detail.menuName}</td>
                      <td className="px-5 py-3.5">
                        <div className="flex flex-wrap gap-1.5">
                          {detail.permissions.map(p => (
                            <span key={p.id}
                              className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium border ${getChipColor(p.permissionName)}`}>
                              {p.permissionName}
                              {isEditingThis && (
                                <button
                                  onClick={() => openConfirm(
                                    'Remove Permission',
                                    `Remove "${p.permissionName}" from "${detail.menuName}"?`,
                                    () => removePermissionMutation.mutate({ roleDetailId: detail.id, permissionId: p.id }),
                                    true, 'Remove'
                                  )}
                                  className="hover:opacity-70 ml-0.5 flex-shrink-0">
                                  <X size={10} />
                                </button>
                              )}
                            </span>
                          ))}
                        </div>
                      </td>
                      <td className="px-5 py-3.5">
                        <div className="flex items-center justify-center gap-1">
                          <button
                            onClick={() => handleEditRow(detail)}
                            className={`p-1.5 rounded-lg transition-colors inline-flex items-center justify-center
                              ${isEditingThis
                                ? 'bg-amber-100 text-amber-600 hover:bg-amber-200'
                                : 'text-gray-400 hover:text-amber-600 hover:bg-amber-50'}`}
                            title={isEditingThis ? 'Cancel Edit' : 'Edit'}
                          >
                            <Pencil size={14} />
                          </button>
                          <button
                            onClick={() => openConfirm(
                              'Remove Group',
                              `Remove all permissions under "${detail.menuName}"?`,
                              () => deleteGroupMutation.mutate(detail.id),
                              true, 'Remove All'
                            )}
                            className="p-1.5 rounded-lg text-gray-400 hover:text-red-600 hover:bg-red-50 transition-colors inline-flex items-center justify-center"
                            title="Delete Group"
                          >
                            <Trash2 size={14} />
                          </button>
                        </div>
                      </td>
                    </tr>
                  )
                })
              )}
            </tbody>
          </table>
        </div>

      </div>
    </div>
  )
}