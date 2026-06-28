// src/pages/admin/RoleViewPage.tsx

import { useState, useMemo } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { ArrowLeft, Pencil, Shield, Key, Search, X, List } from 'lucide-react'
import axiosInstance from '../../api/axiosInstance'
import { roleApi } from '../../api/roleApi'

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
    queryFn: async () => {
      const res = await axiosInstance.get(`/roles/${roleId}/details`)
      return res.data
    },
    enabled: !!roleId,
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

  return (
    <div className="h-full bg-gray-50 flex flex-col">

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
            <span className="font-semibold text-gray-800">{roleDetails?.roleName ?? role?.roleName ?? 'View'}</span>
          </div>
        </div>
        <div className="flex items-center gap-2">
          <button
            onClick={() => navigate(`/admin/roles/${roleId}/edit`)}
            className="flex items-center gap-2 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium rounded-lg transition-colors"
          >
            <Pencil size={14} />
            Edit Role
          </button>
          <button
            onClick={() => navigate('/admin/roles')}
            className="flex items-center gap-2 px-4 py-2 bg-gray-800 hover:bg-gray-900 text-white text-sm font-medium rounded-lg transition-colors"
          >
            <List size={14} />
            List
          </button>
        </div>
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
              <div className="h-8 w-64 bg-gray-100 rounded-lg animate-pulse" />
            ) : (
              <div className="flex items-center gap-6 flex-wrap">
                <div>
                  <p className="text-xs font-semibold text-gray-400 uppercase tracking-wide mb-1">Role Name</p>
                  <p className="text-base font-bold text-gray-800">{roleDetails?.roleName ?? role?.roleName}</p>
                </div>
                <div className="w-px h-10 bg-gray-200" />
                <div>
                  <p className="text-xs font-semibold text-gray-400 uppercase tracking-wide mb-1">Status</p>
                  <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold
                    ${role?.isActive === 1 ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-500'}`}>
                    {role?.isActive === 1 ? 'Active' : 'Inactive'}
                  </span>
                </div>
                <div className="w-px h-10 bg-gray-200" />
                <div>
                  <p className="text-xs font-semibold text-gray-400 uppercase tracking-wide mb-1">Created At</p>
                  <p className="text-sm text-gray-600">
                    {role?.createdAt ? new Date(role.createdAt).toLocaleDateString('en-GB', {
                      day: '2-digit', month: 'short', year: 'numeric'
                    }) : '—'}
                  </p>
                </div>
                <div className="w-px h-10 bg-gray-200" />
                <div>
                  <p className="text-xs font-semibold text-gray-400 uppercase tracking-wide mb-1">Total Assignments</p>
                  <p className="text-sm font-bold text-blue-600">{assignedPermissions.length}</p>
                </div>
              </div>
            )}
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
              </tr>
            </thead>
            <tbody>
              {isLoading ? (
                <tr><td colSpan={3} className="text-center py-10 text-gray-400 text-sm">Loading...</td></tr>
              ) : filteredAssignments.length === 0 ? (
                <tr>
                  <td colSpan={3} className="text-center py-14">
                    <div className="flex flex-col items-center gap-2">
                      <div className="w-14 h-14 bg-gray-100 rounded-2xl flex items-center justify-center">
                        <Key size={22} className="text-gray-300" />
                      </div>
                      <p className="text-sm font-semibold text-gray-400">
                        {searchQuery ? 'No results found' : 'No permissions assigned yet'}
                      </p>
                      <p className="text-xs text-gray-300">
                        {searchQuery ? 'Try a different keyword' : 'Go to Edit page to assign permissions'}
                      </p>
                      {!searchQuery && (
                        <button
                          onClick={() => navigate(`/admin/roles/${roleId}/edit`)}
                          className="mt-2 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white text-xs font-medium rounded-lg transition-colors"
                        >
                          Assign Permissions
                        </button>
                      )}
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