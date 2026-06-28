// src/pages/admin/RoleViewPage.tsx

import { useState, useMemo } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { ArrowLeft, Pencil, Shield, Key, Search, X, ChevronRight, AlertTriangle, Hash, Layers } from 'lucide-react'
import axiosInstance from '../../api/axiosInstance'
import { roleApi } from '../../api/roleApi'

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

function SkeletonRow() {
  return (
    <tr className="border-b border-gray-50">
      <td className="px-5 py-4"><div className="h-4 w-28 bg-gray-100 rounded animate-pulse" /></td>
      <td className="px-5 py-4"><div className="h-4 w-24 bg-gray-100 rounded animate-pulse" /></td>
      <td className="px-5 py-4">
        <div className="flex gap-1.5">
          <div className="h-6 w-20 bg-gray-100 rounded-full animate-pulse" />
          <div className="h-6 w-16 bg-gray-100 rounded-full animate-pulse" />
          <div className="h-6 w-14 bg-gray-100 rounded-full animate-pulse" />
        </div>
      </td>
    </tr>
  )
}

function StatCard({ label, value, color }: { label: string; value: string | number; color: string }) {
  return (
    <div className="flex flex-col gap-1">
      <p className="text-xs font-semibold text-gray-400 uppercase tracking-wide">{label}</p>
      <p className={`text-base font-bold ${color}`}>{value}</p>
    </div>
  )
}

export default function RoleViewPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const roleId = Number(id)
  const [searchQuery, setSearchQuery] = useState('')

  const { data: role } = useQuery({
    queryKey: ['roles', roleId],
    queryFn: () => roleApi.getById(roleId),
    enabled: !!roleId,
  })

  const { data: roleDetails, isLoading } = useQuery<RoleDetails>({
    queryKey: ['role-details-new', roleId],
    queryFn: async () => (await axiosInstance.get(`/roles/${roleId}/details`)).data,
    enabled: !!roleId,
  })

  const assignedPermissions = roleDetails?.assignedPermissions ?? []

  const totalPermissions = useMemo(
    () => assignedPermissions.reduce((sum, a) => sum + a.permissions.length, 0),
    [assignedPermissions]
  )

  const filteredAssignments = useMemo(() => {
    if (!searchQuery.trim()) return assignedPermissions
    const q = searchQuery.toLowerCase()
    return assignedPermissions.filter(a =>
      a.menuName.toLowerCase().includes(q) ||
      a.moduleName.toLowerCase().includes(q) ||
      a.permissions.some(p => p.permissionName.toLowerCase().includes(q))
    )
  }, [assignedPermissions, searchQuery])

  const isMissingMenu = (name: string) => !name || name.toLowerCase() === 'unknown'

  return (
    <div className="h-full bg-gray-50 flex flex-col">

      {/* Top bar */}
      <div className="bg-white border-b border-gray-200 px-6 py-3.5 flex items-center justify-between flex-shrink-0">
        <div className="flex items-center gap-2 text-sm">
          <button onClick={() => navigate('/admin/roles')}
            className="p-1.5 rounded-lg text-gray-400 hover:text-gray-700 hover:bg-gray-100 transition-colors mr-1">
            <ArrowLeft size={16} />
          </button>
          <span className="text-gray-400 hover:text-blue-600 cursor-pointer" onClick={() => navigate('/admin/roles')}>Roles</span>
          <ChevronRight size={13} className="text-gray-300" />
          <span className="text-gray-400">View Role</span>
          <ChevronRight size={13} className="text-gray-300" />
          <span className="font-semibold text-gray-800">{roleDetails?.roleName ?? role?.roleName ?? '...'}</span>
        </div>
        <button
          onClick={() => navigate(`/admin/roles/${roleId}/edit`)}
          className="flex items-center gap-2 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium rounded-lg transition-colors"
        >
          <Pencil size={14} />
          Edit Role
        </button>
      </div>

      <div className="flex-1 overflow-y-auto p-5 space-y-4">

        {/* Role info card */}
        <div className="bg-white border border-gray-200 rounded-xl shadow-sm overflow-hidden">
          <div className="flex items-center gap-2.5 px-5 py-3.5 border-b border-gray-100">
            <div className="w-7 h-7 bg-blue-600 rounded-lg flex items-center justify-center">
              <Shield size={13} className="text-white" />
            </div>
            <h2 className="text-sm font-semibold text-gray-800">Role Information</h2>
          </div>
          <div className="px-5 py-4">
            {isLoading ? (
              <div className="flex gap-8">
                {[1,2,3,4,5].map(i => (
                  <div key={i} className="flex flex-col gap-2">
                    <div className="h-3 w-16 bg-gray-100 rounded animate-pulse" />
                    <div className="h-5 w-20 bg-gray-100 rounded animate-pulse" />
                  </div>
                ))}
              </div>
            ) : (
              <div className="flex items-stretch gap-6 flex-wrap">
                <StatCard label="Role Name" value={roleDetails?.roleName ?? role?.roleName ?? '—'} color="text-gray-800" />
                <div className="w-px bg-gray-200 self-stretch" />
                <div className="flex flex-col gap-1">
                  <p className="text-xs font-semibold text-gray-400 uppercase tracking-wide">Status</p>
                  <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold w-fit
                    ${role?.isActive === 1 ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-500'}`}>
                    {role?.isActive === 1 ? 'Active' : 'Inactive'}
                  </span>
                </div>
                <div className="w-px bg-gray-200 self-stretch" />
                <StatCard
                  label="Created At"
                  value={role?.createdAt ? new Date(role.createdAt).toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' }) : '—'}
                  color="text-gray-600"
                />
                <div className="w-px bg-gray-200 self-stretch" />
                <div className="flex flex-col gap-1">
                  <p className="text-xs font-semibold text-gray-400 uppercase tracking-wide">Assigned Menus</p>
                  <div className="flex items-center gap-1.5">
                    <Layers size={13} className="text-violet-500" />
                    <p className="text-base font-bold text-violet-600">{assignedPermissions.length}</p>
                  </div>
                </div>
                <div className="w-px bg-gray-200 self-stretch" />
                <div className="flex flex-col gap-1">
                  <p className="text-xs font-semibold text-gray-400 uppercase tracking-wide">Assigned Permissions</p>
                  <div className="flex items-center gap-1.5">
                    <Key size={13} className="text-blue-500" />
                    <p className="text-base font-bold text-blue-600">{totalPermissions}</p>
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>

        {/* Permission list card */}
        <div className="bg-white border border-gray-200 rounded-xl shadow-sm overflow-hidden">
          <div className="flex items-center justify-between px-5 py-3.5 border-b border-gray-100">
            <div className="flex items-center gap-3">
              <div className="flex items-center gap-2">
                <h2 className="text-sm font-semibold text-gray-800">Permission Groups</h2>
                {assignedPermissions.length > 0 && (
                  <span className="text-xs bg-violet-100 text-violet-700 px-2 py-0.5 rounded-full font-medium border border-violet-200">
                    {assignedPermissions.length}
                  </span>
                )}
              </div>
              {totalPermissions > 0 && (
                <>
                  <div className="w-px h-4 bg-gray-200" />
                  <div className="flex items-center gap-1.5">
                    <Hash size={12} className="text-gray-400" />
                    <span className="text-xs text-gray-500 font-medium">Total Permissions</span>
                    <span className="text-xs bg-blue-100 text-blue-700 px-2 py-0.5 rounded-full font-medium border border-blue-200">
                      {totalPermissions}
                    </span>
                  </div>
                </>
              )}
            </div>
            <div className="flex items-center gap-2 border border-gray-200 rounded-lg px-3 py-1.5 w-64 focus-within:border-blue-400 focus-within:ring-2 focus-within:ring-blue-50 transition-all">
              <Search size={13} className="text-gray-400 flex-shrink-0" />
              <input
                type="text"
                placeholder="Search module, menu or permission..."
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
              </tr>
            </thead>
            <tbody>
              {isLoading ? (
                <>{[1,2,3].map(i => <SkeletonRow key={i} />)}</>
              ) : filteredAssignments.length === 0 ? (
                <tr>
                  <td colSpan={3} className="text-center py-16">
                    <div className="flex flex-col items-center gap-3">
                      <div className="w-16 h-16 bg-gray-100 rounded-2xl flex items-center justify-center">
                        <Key size={24} className="text-gray-300" />
                      </div>
                      <div>
                        <p className="text-sm font-semibold text-gray-400">
                          {searchQuery ? 'No results found' : 'No permissions assigned to this role.'}
                        </p>
                        <p className="text-xs text-gray-300 mt-1">
                          {searchQuery ? 'Try a different keyword' : 'Go to Edit page to assign permissions'}
                        </p>
                      </div>
                      {!searchQuery && (
                        <button
                          onClick={() => navigate(`/admin/roles/${roleId}/edit`)}
                          className="mt-1 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white text-xs font-medium rounded-lg transition-colors"
                        >
                          Assign Permissions
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              ) : (
                filteredAssignments.map((detail, idx) => (
                  <tr key={detail.id}
                    className={`border-b border-gray-50 hover:bg-gray-50/80 transition-colors ${idx % 2 === 0 ? '' : 'bg-gray-50/40'}`}>
                    <td className="px-5 py-4 font-semibold text-gray-800 whitespace-nowrap">{detail.moduleName}</td>
                    <td className="px-5 py-4 whitespace-nowrap">
                      {isMissingMenu(detail.menuName) ? (
                        <span className="inline-flex items-center gap-1 text-xs font-medium text-amber-600 bg-amber-50 border border-amber-200 px-2 py-0.5 rounded-md">
                          <AlertTriangle size={11} />
                          Missing Menu
                        </span>
                      ) : (
                        <span className="text-gray-600">{detail.menuName}</span>
                      )}
                    </td>
                    <td className="px-5 py-4">
                      <div className="flex flex-wrap gap-1.5">
                        {detail.permissions.map(p => (
                          <span key={p.id}
                            className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border ${getChipColor(p.permissionName)}`}>
                            {p.permissionName}
                          </span>
                        ))}
                      </div>
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