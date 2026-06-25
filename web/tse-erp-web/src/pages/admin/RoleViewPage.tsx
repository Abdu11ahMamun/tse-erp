// src/pages/admin/RoleViewPage.tsx

import { useNavigate, useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { ArrowLeft, Shield, Pencil, Layers, Hash, Key } from 'lucide-react'
import { roleApi } from '../../api/roleApi'
import { roleDetailApi } from '../../api/roleDetailApi'
import { moduleApi } from '../../api/moduleApi'
import { permissionApi } from '../../api/permissionApi'
import { menuApi } from '../../api/menuApi'
import { useMemo } from 'react'

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

export default function RoleViewPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const roleId = Number(id)

  const { data: role, isLoading: roleLoading } = useQuery({
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

  const getModuleName = (id: number) => modules.find(m => m.id === id)?.moduleName ?? `Module ${id}`
  const getMenuName = (id: number) => menuMap.get(id) ?? `Menu ${id}`
  const getPermissionNames = (raw: string) =>
    parsePermissionIds(raw).map(id => permissionMap.get(id) ?? `#${id}`)

  const isLoading = roleLoading || assignLoading

  return (
    <div className="h-full bg-gray-50 flex flex-col">

      {/* Breadcrumb header */}
      <div className="bg-white border-b border-gray-200 px-6 py-4 flex items-center justify-between">
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
            <span className="text-gray-700 font-medium">{role?.roleName ?? 'View'}</span>
          </div>
        </div>
        <button
          onClick={() => navigate(`/admin/roles/${roleId}/edit`)}
          className="flex items-center gap-2 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium rounded-lg transition-colors"
        >
          <Pencil size={14} />
          Edit Role
        </button>
      </div>

      <div className="flex-1 overflow-y-auto p-6">
        <div className="max-w-4xl mx-auto space-y-5">

          {/* Role info card */}
          <div className="bg-white rounded-xl border border-gray-200 shadow-sm overflow-hidden">
            <div className="px-6 py-4 border-b border-gray-100 flex items-center gap-3">
              <div className="w-8 h-8 bg-blue-600 rounded-lg flex items-center justify-center">
                <Shield size={15} className="text-white" />
              </div>
              <h2 className="text-sm font-semibold text-gray-800">Role Details</h2>
            </div>
            <div className="px-6 py-4">
              {roleLoading ? (
                <div className="h-8 w-48 bg-gray-100 rounded-lg animate-pulse" />
              ) : (
                <div className="flex items-center gap-4">
                  <div>
                    <p className="text-xs text-gray-400 uppercase tracking-wide font-semibold mb-0.5">Role Name</p>
                    <p className="text-lg font-bold text-gray-800">{role?.roleName}</p>
                  </div>
                  <div className="w-px h-10 bg-gray-200" />
                  <div>
                    <p className="text-xs text-gray-400 uppercase tracking-wide font-semibold mb-0.5">Status</p>
                    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium
                      ${role?.isActive === 1 ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-500'}`}>
                      {role?.isActive === 1 ? 'Active' : 'Inactive'}
                    </span>
                  </div>
                  <div className="w-px h-10 bg-gray-200" />
                  <div>
                    <p className="text-xs text-gray-400 uppercase tracking-wide font-semibold mb-0.5">Created At</p>
                    <p className="text-sm text-gray-600">
                      {role?.createdAt ? new Date(role.createdAt).toLocaleDateString('en-GB', {
                        day: '2-digit', month: 'short', year: 'numeric'
                      }) : '—'}
                    </p>
                  </div>
                  <div className="w-px h-10 bg-gray-200" />
                  <div>
                    <p className="text-xs text-gray-400 uppercase tracking-wide font-semibold mb-0.5">Assignments</p>
                    <p className="text-sm font-bold text-blue-600">{assignments.length}</p>
                  </div>
                </div>
              )}
            </div>
          </div>

          {/* Permissions card */}
          <div className="bg-white rounded-xl border border-gray-200 shadow-sm overflow-hidden">
            <div className="px-6 py-4 border-b border-gray-100 flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className="w-8 h-8 bg-violet-100 rounded-lg flex items-center justify-center">
                  <Key size={15} className="text-violet-600" />
                </div>
                <h2 className="text-sm font-semibold text-gray-800">Assigned Permissions</h2>
              </div>
              {assignments.length > 0 && (
                <span className="text-xs text-gray-500 bg-gray-100 px-2.5 py-1 rounded-full font-medium">
                  {assignments.length} assignment{assignments.length !== 1 ? 's' : ''}
                </span>
              )}
            </div>

            <div className="p-6">
              {isLoading ? (
                <div className="space-y-3">
                  {[1,2,3].map(i => <div key={i} className="h-20 bg-gray-100 rounded-xl animate-pulse" />)}
                </div>
              ) : assignments.length === 0 ? (
                <div className="flex flex-col items-center justify-center py-12 text-center">
                  <div className="w-16 h-16 bg-gray-100 rounded-2xl flex items-center justify-center mb-3">
                    <Key size={24} className="text-gray-300" />
                  </div>
                  <p className="text-sm font-semibold text-gray-400">No permissions assigned yet</p>
                  <p className="text-xs text-gray-300 mt-1">Use the Edit page to assign module permissions</p>
                  <button
                    onClick={() => navigate(`/admin/roles/${roleId}/edit`)}
                    className="mt-4 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white text-xs font-medium rounded-lg transition-colors"
                  >
                    Assign Permissions
                  </button>
                </div>
              ) : (
                <div className="grid gap-3">
                  {assignments.map((detail, index) => {
                    const colorIndex = moduleColorMap.get(detail.moduleId) ?? index
                    const color = MODULE_COLORS[colorIndex % MODULE_COLORS.length]
                    const permNames = getPermissionNames(detail.permissionId)

                    return (
                      <div key={detail.id} className={`border rounded-xl p-4 ${color.bg} ${color.border}`}>
                        <div className="flex items-center gap-2 flex-wrap mb-2.5">
                          <div className="flex items-center gap-1.5">
                            <Layers size={13} className={color.text} />
                            <span className={`text-sm font-bold ${color.text}`}>{getModuleName(detail.moduleId)}</span>
                          </div>
                          <span className="flex items-center gap-1 text-xs text-gray-500 bg-white px-2 py-0.5 rounded-md border border-gray-200">
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
                    )
                  })}
                </div>
              )}
            </div>
          </div>

        </div>
      </div>
    </div>
  )
}