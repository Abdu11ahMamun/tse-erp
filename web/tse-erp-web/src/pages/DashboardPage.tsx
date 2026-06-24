// src/pages/DashboardPage.tsx

import { useQuery } from '@tanstack/react-query'
import { Users, Shield, Layers, Key, ArrowRight, Activity, CheckCircle, XCircle } from 'lucide-react'
import { userApi } from '../api/userApi'
import { roleApi } from '../api/roleApi'
import { moduleApi } from '../api/moduleApi'
import { permissionApi } from '../api/permissionApi'
import { useAuthStore } from '../store/authStore'
import { useNavigate } from 'react-router-dom'

interface StatCardProps {
  label: string
  value: number
  icon: React.ReactNode
  gradient: string
  isLoading: boolean
  onClick: () => void
}

function StatCard({ label, value, icon, gradient, isLoading, onClick }: StatCardProps) {
  return (
    <div
      onClick={onClick}
      className={`relative overflow-hidden rounded-2xl p-5 cursor-pointer transition-all hover:-translate-y-0.5 hover:shadow-lg ${gradient}`}
    >
      {/* Background circle decoration */}
      <div className="absolute -right-4 -top-4 w-24 h-24 rounded-full bg-white/10" />
      <div className="absolute -right-1 -bottom-6 w-16 h-16 rounded-full bg-white/10" />

      <div className="relative">
        <div className="flex items-start justify-between mb-4">
          <div className="w-10 h-10 bg-white/20 rounded-xl flex items-center justify-center">
            {icon}
          </div>
          <ArrowRight size={16} className="text-white/60 mt-1" />
        </div>

        {isLoading ? (
          <div className="h-9 w-16 bg-white/20 rounded-lg animate-pulse mb-1" />
        ) : (
          <p className="text-3xl font-bold text-white mb-1">{value}</p>
        )}
        <p className="text-sm text-white/80 font-medium">{label}</p>
      </div>
    </div>
  )
}

export default function DashboardPage() {
  const user = useAuthStore(s => s.user)
  const navigate = useNavigate()

  const { data: users = [], isLoading: usersLoading } = useQuery({
    queryKey: ['users'],
    queryFn: userApi.getAll,
  })
  const { data: roles = [], isLoading: rolesLoading } = useQuery({
    queryKey: ['roles'],
    queryFn: roleApi.getAll,
  })
  const { data: modules = [], isLoading: modulesLoading } = useQuery({
    queryKey: ['modules'],
    queryFn: moduleApi.getAll,
  })
  const { data: permissions = [], isLoading: permissionsLoading } = useQuery({
    queryKey: ['permissions'],
    queryFn: permissionApi.getAll,
  })

  const recentUsers = [...users]
    .sort((a, b) => new Date(b.createdAt ?? 0).getTime() - new Date(a.createdAt ?? 0).getTime())
    .slice(0, 5)

  const activeUsers = users.filter(u => u.isActive === 1).length
  const inactiveUsers = users.length - activeUsers
  const activePercent = users.length > 0 ? Math.round((activeUsers / users.length) * 100) : 0

  const greeting = () => {
    const h = new Date().getHours()
    if (h < 12) return 'Good morning'
    if (h < 17) return 'Good afternoon'
    return 'Good evening'
  }

  return (
    <div className="p-6 space-y-6 bg-gray-50 min-h-full">

      {/* ── Welcome banner ── */}
      <div className="relative overflow-hidden bg-gradient-to-r from-blue-600 via-blue-500 to-indigo-600 rounded-2xl px-6 py-5">
        <div className="absolute inset-0 opacity-10"
          style={{ backgroundImage: 'radial-gradient(circle at 20px 20px, white 1.5px, transparent 0)', backgroundSize: '32px 32px' }}
        />
        <div className="relative flex items-center justify-between">
          <div>
            <p className="text-blue-100 text-sm font-medium">{greeting()},</p>
            <h1 className="text-2xl font-bold text-white mt-0.5">
              {user?.username ?? 'Admin'} 👋
            </h1>
            <p className="text-blue-200 text-sm mt-1">{user?.role} · TSE-ERP Dashboard</p>
          </div>
          <div className="text-right">
            <p className="text-white/80 text-xs">
              {new Date().toLocaleDateString('en-GB', { weekday: 'long' })}
            </p>
            <p className="text-white font-semibold text-sm mt-0.5">
              {new Date().toLocaleDateString('en-GB', { day: '2-digit', month: 'long', year: 'numeric' })}
            </p>
            <div className="flex items-center gap-1.5 mt-2 justify-end">
              <div className="w-2 h-2 bg-green-400 rounded-full animate-pulse" />
              <span className="text-green-300 text-xs font-medium">System Online</span>
            </div>
          </div>
        </div>
      </div>

      {/* ── Stat cards ── */}
      <div className="grid grid-cols-4 gap-4">
        <StatCard
          label="Total Users"
          value={users.length}
          isLoading={usersLoading}
          icon={<Users size={20} className="text-white" />}
          gradient="bg-gradient-to-br from-blue-500 to-blue-700"
          onClick={() => navigate('/admin/users')}
        />
        <StatCard
          label="Total Roles"
          value={roles.length}
          isLoading={rolesLoading}
          icon={<Shield size={20} className="text-white" />}
          gradient="bg-gradient-to-br from-violet-500 to-violet-700"
          onClick={() => navigate('/admin/roles')}
        />
        <StatCard
          label="Total Modules"
          value={modules.length}
          isLoading={modulesLoading}
          icon={<Layers size={20} className="text-white" />}
          gradient="bg-gradient-to-br from-emerald-500 to-emerald-700"
          onClick={() => navigate('/admin/modules')}
        />
        <StatCard
          label="Total Permissions"
          value={permissions.length}
          isLoading={permissionsLoading}
          icon={<Key size={20} className="text-white" />}
          gradient="bg-gradient-to-br from-amber-500 to-orange-600"
          onClick={() => navigate('/admin/permissions')}
        />
      </div>

      {/* ── Bottom row ── */}
      <div className="grid grid-cols-3 gap-4">

        {/* Recent Users */}
        <div className="col-span-2 bg-white border border-gray-200 rounded-2xl overflow-hidden shadow-sm">
          <div className="flex items-center justify-between px-5 py-4 border-b border-gray-100">
            <div className="flex items-center gap-2.5">
              <div className="w-8 h-8 bg-blue-50 rounded-lg flex items-center justify-center">
                <Activity size={15} className="text-blue-600" />
              </div>
              <h2 className="text-sm font-semibold text-gray-800">Recent Users</h2>
            </div>
            <button
              onClick={() => navigate('/admin/users')}
              className="flex items-center gap-1 text-xs text-blue-600 hover:text-blue-700 font-medium bg-blue-50 px-2.5 py-1 rounded-lg transition-colors"
            >
              View all <ArrowRight size={11} />
            </button>
          </div>

          {usersLoading ? (
            <div className="space-y-3 p-5">
              {[1,2,3].map(i => <div key={i} className="h-10 bg-gray-100 rounded-xl animate-pulse" />)}
            </div>
          ) : recentUsers.length === 0 ? (
            <div className="text-center py-10 text-gray-400 text-sm">No users yet</div>
          ) : (
            <div className="divide-y divide-gray-50">
              {recentUsers.map((u, i) => (
                <div key={u.id} className="flex items-center justify-between px-5 py-3.5 hover:bg-gray-50 transition-colors">
                  <div className="flex items-center gap-3">
                    <div className="w-8 h-8 rounded-full flex items-center justify-center text-white text-xs font-bold flex-shrink-0"
                      style={{ background: ['#3b82f6','#8b5cf6','#10b981','#f59e0b','#ef4444'][i % 5] }}>
                      {u.fullName.charAt(0).toUpperCase()}
                    </div>
                    <div>
                      <p className="text-sm font-medium text-gray-800 leading-none">{u.fullName}</p>
                      <p className="text-xs text-gray-400 mt-0.5">@{u.userName}</p>
                    </div>
                  </div>
                  <div className="flex items-center gap-3">
                    <p className="text-xs text-gray-400 hidden sm:block">{u.email}</p>
                    <span className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium
                      ${u.isActive === 1 ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-500'}`}>
                      {u.isActive === 1
                        ? <><CheckCircle size={10} /> Active</>
                        : <><XCircle size={10} /> Inactive</>
                      }
                    </span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Right column */}
        <div className="space-y-4">

          {/* User Status */}
          <div className="bg-white border border-gray-200 rounded-2xl p-5 shadow-sm">
            <div className="flex items-center gap-2.5 mb-4">
              <div className="w-8 h-8 bg-emerald-50 rounded-lg flex items-center justify-center">
                <Users size={15} className="text-emerald-600" />
              </div>
              <h2 className="text-sm font-semibold text-gray-800">User Overview</h2>
            </div>

            {usersLoading ? (
              <div className="space-y-3">
                <div className="h-16 bg-gray-100 rounded-xl animate-pulse" />
              </div>
            ) : (
              <>
                {/* Donut-style percentage */}
                <div className="flex items-center gap-4 mb-4">
                  <div className="relative w-16 h-16 flex-shrink-0">
                    <svg viewBox="0 0 36 36" className="w-16 h-16 -rotate-90">
                      <circle cx="18" cy="18" r="15.9" fill="none" stroke="#f3f4f6" strokeWidth="3" />
                      <circle
                        cx="18" cy="18" r="15.9" fill="none"
                        stroke="#10b981" strokeWidth="3"
                        strokeDasharray={`${activePercent} ${100 - activePercent}`}
                        strokeLinecap="round"
                      />
                    </svg>
                    <div className="absolute inset-0 flex items-center justify-center">
                      <span className="text-xs font-bold text-gray-700">{activePercent}%</span>
                    </div>
                  </div>
                  <div className="space-y-1.5">
                    <div className="flex items-center gap-2">
                      <div className="w-2.5 h-2.5 rounded-full bg-emerald-500 flex-shrink-0" />
                      <span className="text-xs text-gray-500">Active</span>
                      <span className="text-xs font-bold text-gray-800 ml-auto">{activeUsers}</span>
                    </div>
                    <div className="flex items-center gap-2">
                      <div className="w-2.5 h-2.5 rounded-full bg-gray-200 flex-shrink-0" />
                      <span className="text-xs text-gray-500">Inactive</span>
                      <span className="text-xs font-bold text-gray-800 ml-auto">{inactiveUsers}</span>
                    </div>
                    <div className="flex items-center gap-2">
                      <div className="w-2.5 h-2.5 rounded-full bg-blue-500 flex-shrink-0" />
                      <span className="text-xs text-gray-500">Total</span>
                      <span className="text-xs font-bold text-gray-800 ml-auto">{users.length}</span>
                    </div>
                  </div>
                </div>
              </>
            )}
          </div>

          {/* Quick links */}
          <div className="bg-white border border-gray-200 rounded-2xl p-5 shadow-sm">
            <div className="flex items-center gap-2.5 mb-3">
              <div className="w-8 h-8 bg-violet-50 rounded-lg flex items-center justify-center">
                <ArrowRight size={15} className="text-violet-600" />
              </div>
              <h2 className="text-sm font-semibold text-gray-800">Quick Links</h2>
            </div>
            <div className="space-y-1">
              {[
                { label: 'Chart of Accounts', path: '/accounting/coa', color: 'text-blue-600 bg-blue-50' },
                { label: 'Role Details', path: '/admin/role-details', color: 'text-violet-600 bg-violet-50' },
                { label: 'Menu Management', path: '/admin/menus', color: 'text-emerald-600 bg-emerald-50' },
                { label: 'Manage Users', path: '/admin/users', color: 'text-amber-600 bg-amber-50' },
              ].map(link => (
                <button
                  key={link.path}
                  onClick={() => navigate(link.path)}
                  className="w-full flex items-center justify-between px-3 py-2.5 rounded-xl text-sm text-gray-600 hover:bg-gray-50 transition-colors group"
                >
                  <span className="font-medium">{link.label}</span>
                  <div className={`w-6 h-6 rounded-lg flex items-center justify-center ${link.color} opacity-0 group-hover:opacity-100 transition-opacity`}>
                    <ArrowRight size={11} />
                  </div>
                </button>
              ))}
            </div>
          </div>

        </div>
      </div>
    </div>
  )
}