// src/pages/admin/RoleDetailPage.tsx

import { useState, useMemo } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Trash2, Check, Shield, ChevronRight, Hash, Key } from 'lucide-react'
import { roleDetailApi } from '../../api/roleDetailApi'
import { roleApi } from '../../api/roleApi'
import { moduleApi } from '../../api/moduleApi'
import { permissionApi } from '../../api/permissionApi'
import { menuApi } from '../../api/menuApi'
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

function StepBadge({ n, done, active }: { n: number; done: boolean; active: boolean }) {
  return (
    <span className={`inline-flex items-center justify-center w-5 h-5 rounded-full text-xs font-bold mr-2 flex-shrink-0 transition-all
      ${done ? 'bg-green-500 text-white' : active ? 'bg-blue-600 text-white' : 'bg-gray-200 text-gray-400'}`}>
      {done ? <Check size={10} strokeWidth={3} /> : n}
    </span>
  )
}

function AssignmentCard({
  detail, index, moduleName, menuName, permissionNames, onDelete, deleting, colorIndex,
}: {
  detail: AfmRoleDetail
  index: number
  moduleName: string
  menuName: string
  permissionNames: string[]
  onDelete: () => void
  deleting: boolean
  colorIndex: number
}) {
  const color = MODULE_COLORS[colorIndex % MODULE_COLORS.length]

  return (
    <div className={`border rounded-xl p-4 transition-all hover:shadow-md ${color.bg} ${color.border}`}>
      <div className="flex items-start justify-between gap-3">
        <div className="flex items-start gap-3 min-w-0 flex-1">
          <div className={`w-2 h-2 rounded-full mt-2 flex-shrink-0 ${color.dot}`} />
          <div className="min-w-0 flex-1">
            <div className="flex items-center gap-2 flex-wrap mb-2.5">
              <span className={`text-sm font-bold ${color.text}`}>{moduleName}</span>
              <span className="flex items-center gap-1 text-xs text-gray-500 bg-white px-2 py-0.5 rounded-md border border-gray-200 font-medium">
                <Hash size={10} />
                {menuName}
              </span>
              <span className="text-xs text-gray-400 bg-white px-2 py-0.5 rounded-md border border-gray-200">
                {permissionNames.length} permission{permissionNames.length !== 1 ? 's' : ''}
              </span>
            </div>
            <div className="flex flex-wrap gap-1.5">
              {permissionNames.length > 0 ? (
                permissionNames.map((name, i) => (
                  <span key={i} className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-md text-xs font-medium border ${color.badge}`}>
                    <Key size={9} />
                    {name}
                  </span>
                ))
              ) : (
                parsePermissionIds(detail.permissionId).map(id => (
                  <span key={id} className="px-2 py-0.5 bg-white text-gray-500 rounded-md text-xs border border-gray-200">#{id}</span>
                ))
              )}
            </div>
          </div>
        </div>
        <button
          onClick={onDelete}
          disabled={deleting}
          className="p-1.5 rounded-lg text-gray-300 hover:text-red-500 hover:bg-white transition-all flex-shrink-0 border border-transparent hover:border-red-100"
        >
          <Trash2 size={14} />
        </button>
      </div>
    </div>
  )
}

export default function RoleDetailPage() {
  const queryClient = useQueryClient()

  const [selectedRoleId, setSelectedRoleId] = useState<number | ''>('')
  const [selectedModuleId, setSelectedModuleId] = useState<number | ''>('')
  const [menuId, setMenuId] = useState<number | ''>('')
  const [selectedPermissions, setSelectedPermissions] = useState<number[]>([])
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' } | null>(null)
  const [deletingId, setDeletingId] = useState<number | null>(null)

  const showToast = (message: string, type: 'success' | 'error' = 'success') => {
    setToast({ message, type })
    setTimeout(() => setToast(null), 3000)
  }

  const { data: roles = [] } = useQuery({ queryKey: ['roles'], queryFn: roleApi.getAll })
  const { data: modules = [] } = useQuery({ queryKey: ['modules'], queryFn: moduleApi.getAll })
  const { data: allPermissions = [] } = useQuery({ queryKey: ['permissions'], queryFn: permissionApi.getAll })
  const { data: allMenus = [] } = useQuery({ queryKey: ['menus'], queryFn: menuApi.getAll })

  const { data: availablePermissions = [] } = useQuery({
    queryKey: ['permissions', 'module', selectedModuleId],
    queryFn: () => permissionApi.getByModule(Number(selectedModuleId)),
    enabled: !!selectedModuleId,
  })

  const { data: availableMenus = [] } = useQuery({
    queryKey: ['menus', 'module', selectedModuleId],
    queryFn: () => menuApi.getByModule(Number(selectedModuleId)),
    enabled: !!selectedModuleId,
  })

  const { data: assignedDetails = [], isLoading: assignedLoading } = useQuery({
    queryKey: ['role-details', selectedRoleId],
    queryFn: () => roleDetailApi.getByRole(Number(selectedRoleId)),
    enabled: !!selectedRoleId,
  })

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
    assignedDetails.forEach(d => {
      if (!map.has(d.moduleId)) { map.set(d.moduleId, i); i++ }
    })
    return map
  }, [assignedDetails])

  const createMutation = useMutation({
    mutationFn: roleDetailApi.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['role-details', selectedRoleId] })
      resetLeftForm()
      showToast('Permissions assigned successfully!')
    },
    onError: (error: any) => {
      const message = error?.response?.data?.message || 'Failed to assign permissions.'
      showToast(message, 'error')
    },
  })

  const deleteMutation = useMutation({
    mutationFn: roleDetailApi.delete,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['role-details', selectedRoleId] })
      setDeletingId(null)
      showToast('Assignment removed.')
    },
    onError: () => { setDeletingId(null); showToast('Failed to remove.', 'error') },
  })

  const resetLeftForm = () => {
    setSelectedModuleId('')
    setMenuId('')
    setSelectedPermissions([])
  }

  const togglePermission = (id: number) =>
    setSelectedPermissions(prev =>
      prev.includes(id) ? prev.filter(p => p !== id) : [...prev, id]
    )

  const toggleAll = () =>
    setSelectedPermissions(
      selectedPermissions.length === availablePermissions.length
        ? [] : availablePermissions.map(p => p.id)
    )

  const handleSave = () => {
    if (!selectedRoleId) return showToast('Select a role first.', 'error')
    if (!selectedModuleId) return showToast('Select a module.', 'error')
    if (!menuId) return showToast('Select a menu.', 'error')
    if (selectedPermissions.length === 0) return showToast('Select at least one permission.', 'error')
    createMutation.mutate({
      roleId: Number(selectedRoleId),
      moduleId: Number(selectedModuleId),
      menuId: Number(menuId),
      permissionId: JSON.stringify(selectedPermissions),
    })
  }

  const handleDelete = (detail: AfmRoleDetail) => {
    if (!confirm('Remove this permission assignment?')) return
    setDeletingId(detail.id)
    deleteMutation.mutate(detail.id)
  }

  const getModuleName = (id: number) => modules.find(m => m.id === id)?.moduleName ?? `Module ${id}`
  const getRoleName = (id: number) => roles.find(r => r.id === id)?.roleName ?? `Role ${id}`
  const getMenuName = (id: number) => menuMap.get(id) ?? `Menu ${id}`
  const getPermissionNames = (detail: AfmRoleDetail) =>
    parsePermissionIds(detail.permissionId).map(id => permissionMap.get(id) ?? `#${id}`)

  const step1Done = !!selectedRoleId
  const step2Done = !!selectedModuleId
  const step3Done = !!menuId
  const step4Done = selectedPermissions.length > 0

  return (
    <div className="flex h-full bg-gray-50">
      {toast && <Toast message={toast.message} type={toast.type} />}

      {/* ══ LEFT PANEL ══════════════════════════════════════════════════════ */}
      <div className="w-[320px] flex-shrink-0 bg-white border-r border-gray-200 flex flex-col h-full">

        <div className="px-5 py-4 border-b border-gray-100 bg-gradient-to-r from-blue-600 to-blue-500">
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 bg-white/20 rounded-xl flex items-center justify-center">
              <Shield size={18} className="text-white" />
            </div>
            <div>
              <h2 className="text-sm font-bold text-white">Assign Permissions</h2>
              <p className="text-xs text-blue-100 mt-0.5">Complete 4 steps below</p>
            </div>
          </div>
        </div>

        <div className="flex-1 overflow-y-auto px-5 py-5 space-y-4">

          {/* Step 1 — Role */}
          <div className="space-y-2">
            <label className="flex items-center text-xs font-semibold text-gray-600 uppercase tracking-wide">
              <StepBadge n={1} done={step1Done} active={true} />
              Select Role
            </label>
            <select
              value={selectedRoleId}
              onChange={e => { setSelectedRoleId(e.target.value ? Number(e.target.value) : ''); resetLeftForm() }}
              className="w-full border border-gray-200 rounded-lg px-3 py-2.5 text-sm outline-none focus:border-blue-400 focus:ring-2 focus:ring-blue-50 bg-white"
            >
              <option value="">-- Select Role --</option>
              {roles.map(r => <option key={r.id} value={r.id}>{r.roleName}</option>)}
            </select>
          </div>

          <div className={`ml-2.5 w-px h-4 transition-colors ${step1Done ? 'bg-blue-300' : 'bg-gray-200'}`} />

          {/* Step 2 — Module */}
          <div className={`space-y-2 transition-opacity duration-200 ${step1Done ? 'opacity-100' : 'opacity-35 pointer-events-none'}`}>
            <label className="flex items-center text-xs font-semibold text-gray-600 uppercase tracking-wide">
              <StepBadge n={2} done={step2Done} active={step1Done} />
              Select Module
            </label>
            <select
              value={selectedModuleId}
              onChange={e => {
                setSelectedModuleId(e.target.value ? Number(e.target.value) : '')
                setMenuId('')
                setSelectedPermissions([])
              }}
              className="w-full border border-gray-200 rounded-lg px-3 py-2.5 text-sm outline-none focus:border-blue-400 focus:ring-2 focus:ring-blue-50 bg-white"
            >
              <option value="">-- Select Module --</option>
              {modules.map(m => <option key={m.id} value={m.id}>{m.moduleName}</option>)}
            </select>
          </div>

          <div className={`ml-2.5 w-px h-4 transition-colors ${step2Done ? 'bg-blue-300' : 'bg-gray-200'}`} />

          {/* Step 3 — Menu */}
          <div className={`space-y-2 transition-opacity duration-200 ${step2Done ? 'opacity-100' : 'opacity-35 pointer-events-none'}`}>
            <label className="flex items-center text-xs font-semibold text-gray-600 uppercase tracking-wide">
              <StepBadge n={3} done={step3Done} active={step2Done} />
              Select Menu
            </label>
            <select
              value={menuId}
              onChange={e => setMenuId(e.target.value ? Number(e.target.value) : '')}
              className="w-full border border-gray-200 rounded-lg px-3 py-2.5 text-sm outline-none focus:border-blue-400 focus:ring-2 focus:ring-blue-50 bg-white"
            >
              <option value="">-- Select Menu --</option>
              {availableMenus.map(m => (
                <option key={m.id} value={m.id}>{m.menuName}</option>
              ))}
            </select>
            {selectedModuleId && availableMenus.length === 0 && (
              <p className="text-xs text-amber-600 flex items-center gap-1.5 bg-amber-50 px-2.5 py-1.5 rounded-lg border border-amber-100">
                <span className="w-3.5 h-3.5 rounded-full bg-amber-400 flex items-center justify-center text-white text-xs font-bold flex-shrink-0">!</span>
                No menus found for this module
              </p>
            )}
          </div>

          <div className={`ml-2.5 w-px h-4 transition-colors ${step3Done ? 'bg-blue-300' : 'bg-gray-200'}`} />

          {/* Step 4 — Permissions */}
          <div className={`space-y-2 transition-opacity duration-200 ${step3Done ? 'opacity-100' : 'opacity-35 pointer-events-none'}`}>
            <div className="flex items-center justify-between">
              <label className="flex items-center text-xs font-semibold text-gray-600 uppercase tracking-wide">
                <StepBadge n={4} done={step4Done} active={step3Done} />
                Permissions
                {selectedPermissions.length > 0 && (
                  <span className="ml-2 bg-blue-600 text-white text-xs px-1.5 py-0.5 rounded-full font-bold leading-none">
                    {selectedPermissions.length}
                  </span>
                )}
              </label>
              {availablePermissions.length > 0 && (
                <button onClick={toggleAll} className="text-xs text-blue-600 hover:text-blue-700 font-semibold">
                  {selectedPermissions.length === availablePermissions.length ? 'Deselect All' : 'Select All'}
                </button>
              )}
            </div>

            {!selectedModuleId ? (
              <div className="text-xs text-gray-400 py-5 text-center border border-dashed border-gray-200 rounded-xl bg-gray-50">
                Select a module first
              </div>
            ) : availablePermissions.length === 0 ? (
              <div className="text-xs text-gray-400 py-5 text-center border border-dashed border-gray-200 rounded-xl bg-gray-50">
                No permissions in this module
              </div>
            ) : (
              <div className="space-y-1 max-h-52 overflow-y-auto rounded-xl border border-gray-200 p-2 bg-gray-50">
                {availablePermissions.map(perm => {
                  const checked = selectedPermissions.includes(perm.id)
                  return (
                    <label
                      key={perm.id}
                      className={`flex items-center gap-2.5 px-3 py-2 rounded-lg cursor-pointer transition-all
                        ${checked ? 'bg-blue-50 border border-blue-200' : 'hover:bg-white border border-transparent'}`}
                    >
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

          {/* Assign button */}
          <div className="pt-2 pb-1">
            <button
              onClick={handleSave}
              disabled={createMutation.isPending || !step4Done}
              className="w-full py-3 bg-blue-600 hover:bg-blue-700 active:bg-blue-800 disabled:opacity-40 disabled:cursor-not-allowed text-white text-sm font-bold rounded-xl transition-all flex items-center justify-center gap-2 shadow-md shadow-blue-200"
            >
              {createMutation.isPending ? (
                <span className="flex items-center gap-2">
                  <span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                  Saving...
                </span>
              ) : (
                <>
                  <Shield size={15} />
                  Assign Permissions
                </>
              )}
            </button>
          </div>

        </div>
      </div>

      {/* ══ RIGHT PANEL ═════════════════════════════════════════════════════ */}
      <div className="flex-1 overflow-y-auto">

        <div className="sticky top-0 bg-gray-50 border-b border-gray-200 px-6 py-4 z-10">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-lg font-bold text-gray-800">Role Details</h1>
              <p className="text-xs text-gray-500 mt-0.5">
                {selectedRoleId ? (
                  <>Assignments for <span className="font-semibold text-blue-600">{getRoleName(Number(selectedRoleId))}</span></>
                ) : 'Select a role to view assignments'}
              </p>
            </div>
            {assignedDetails.length > 0 && (
              <span className="text-xs font-semibold text-gray-500 bg-white border border-gray-200 px-3 py-1.5 rounded-full">
                {assignedDetails.length} assignment{assignedDetails.length !== 1 ? 's' : ''}
              </span>
            )}
          </div>
        </div>

        <div className="p-6">
          {!selectedRoleId ? (
            <div className="flex flex-col items-center justify-center h-72 text-center">
              <div className="w-20 h-20 bg-white rounded-2xl flex items-center justify-center mb-4 border-2 border-dashed border-gray-200">
                <Shield size={32} className="text-gray-200" />
              </div>
              <p className="text-sm font-semibold text-gray-400">No role selected</p>
              <p className="text-xs text-gray-300 mt-1">Choose a role from the left panel</p>
            </div>
          ) : assignedLoading ? (
            <div className="flex items-center justify-center h-40 gap-2 text-gray-400 text-sm">
              <span className="w-4 h-4 border-2 border-gray-200 border-t-blue-400 rounded-full animate-spin" />
              Loading assignments...
            </div>
          ) : assignedDetails.length === 0 ? (
            <div className="flex flex-col items-center justify-center h-72 text-center">
              <div className="w-20 h-20 bg-white rounded-2xl flex items-center justify-center mb-4 border-2 border-dashed border-gray-200">
                <Key size={32} className="text-gray-200" />
              </div>
              <p className="text-sm font-semibold text-gray-400">No permissions assigned</p>
              <p className="text-xs text-gray-300 mt-1">Use the left panel to assign permissions</p>
            </div>
          ) : (
            <div className="grid gap-3">
              {assignedDetails.map((detail, index) => (
                <AssignmentCard
                  key={detail.id}
                  detail={detail}
                  index={index}
                  moduleName={getModuleName(detail.moduleId)}
                  menuName={getMenuName(detail.menuId)}
                  permissionNames={getPermissionNames(detail)}
                  onDelete={() => handleDelete(detail)}
                  deleting={deletingId === detail.id}
                  colorIndex={moduleColorMap.get(detail.moduleId) ?? index}
                />
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}