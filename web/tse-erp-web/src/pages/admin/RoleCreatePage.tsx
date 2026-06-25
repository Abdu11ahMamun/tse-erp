// src/pages/admin/RoleCreatePage.tsx

import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { ArrowLeft, Shield, CheckCircle } from 'lucide-react'
import { roleApi } from '../../api/roleApi'
import type { CreateRoleRequest } from '../../types/role.types'

function ConfirmModal({ isOpen, onConfirm, onCancel, isLoading, roleName }: {
  isOpen: boolean; onConfirm: () => void; onCancel: () => void
  isLoading: boolean; roleName: string
}) {
  if (!isOpen) return null
  return (
    <div className="fixed inset-0 z-40 flex items-center justify-center">
      <div className="absolute inset-0 bg-black/40 backdrop-blur-sm" onClick={onCancel} />
      <div className="relative bg-white rounded-2xl shadow-2xl w-full max-w-sm mx-4 z-50 p-6">
        <div className="w-14 h-14 bg-blue-100 rounded-2xl flex items-center justify-center mx-auto mb-4">
          <Shield size={24} className="text-blue-600" />
        </div>
        <h3 className="text-base font-bold text-gray-800 text-center mb-1">Confirm Create Role</h3>
        <p className="text-sm text-gray-500 text-center mb-2">You are about to create:</p>
        <p className="text-sm font-bold text-blue-600 text-center bg-blue-50 rounded-lg py-2 px-4 mb-6">"{roleName}"</p>
        <div className="flex gap-3">
          <button onClick={onCancel} disabled={isLoading}
            className="flex-1 py-2.5 border border-gray-200 hover:bg-gray-50 text-gray-600 text-sm font-medium rounded-xl transition-colors">
            Cancel
          </button>
          <button onClick={onConfirm} disabled={isLoading}
            className="flex-1 py-2.5 bg-blue-600 hover:bg-blue-700 disabled:opacity-50 text-white text-sm font-semibold rounded-xl transition-colors flex items-center justify-center gap-2">
            {isLoading ? <><span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />Creating...</> : <><CheckCircle size={14} />Confirm</>}
          </button>
        </div>
      </div>
    </div>
  )
}

export default function RoleCreatePage() {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [showConfirm, setShowConfirm] = useState(false)
  const [pendingData, setPendingData] = useState<CreateRoleRequest | null>(null)

  const { register, handleSubmit, watch, formState: { errors } } = useForm<CreateRoleRequest>({
    defaultValues: { roleName: '', isActive: 1 },
  })

  const roleName = watch('roleName')

  const createMutation = useMutation({
    mutationFn: roleApi.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['roles'] })
      navigate('/admin/roles', { state: { toast: 'Role created successfully!' } })
    },
    onError: () => setShowConfirm(false),
  })

  const onSubmit = (data: CreateRoleRequest) => {
    setPendingData({ ...data, isActive: Number(data.isActive) as 0 | 1 })
    setShowConfirm(true)
  }

  return (
    <div className="h-full bg-gray-50 flex flex-col">
      <ConfirmModal
        isOpen={showConfirm}
        onConfirm={() => pendingData && createMutation.mutate(pendingData)}
        onCancel={() => setShowConfirm(false)}
        isLoading={createMutation.isPending}
        roleName={roleName}
      />

      {/* Breadcrumb header */}
      <div className="bg-white border-b border-gray-200 px-6 py-4 flex items-center gap-3">
        <button onClick={() => navigate('/admin/roles')}
          className="p-2 rounded-lg text-gray-400 hover:text-gray-700 hover:bg-gray-100 transition-colors">
          <ArrowLeft size={16} />
        </button>
        <div className="flex items-center gap-2 text-sm">
          <span className="text-gray-400">Admin</span>
          <span className="text-gray-300">/</span>
          <span className="text-gray-400 cursor-pointer hover:text-blue-600" onClick={() => navigate('/admin/roles')}>Roles</span>
          <span className="text-gray-300">/</span>
          <span className="text-gray-700 font-medium">Create</span>
        </div>
      </div>

      {/* Main content */}
      <div className="flex-1 p-6">
        <div className="max-w-2xl mx-auto">

          {/* Page title */}
          <div className="mb-6">
            <h1 className="text-xl font-bold text-gray-800">Create New Role</h1>
            <p className="text-sm text-gray-500 mt-0.5">Define a new role to assign permissions to users</p>
          </div>

          {/* Form card */}
          <div className="bg-white rounded-xl border border-gray-200 shadow-sm overflow-hidden">

            {/* Card header */}
            <div className="px-6 py-4 border-b border-gray-100 flex items-center gap-3">
              <div className="w-8 h-8 bg-blue-600 rounded-lg flex items-center justify-center">
                <Shield size={15} className="text-white" />
              </div>
              <h2 className="text-sm font-semibold text-gray-800">Role Information</h2>
            </div>

            {/* Form body */}
            <div className="p-6 space-y-5">
              <form onSubmit={handleSubmit(onSubmit)}>

                <div className="grid grid-cols-2 gap-5">
                  {/* Role Name */}
                  <div className="col-span-2">
                    <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wide mb-1.5">
                      Role Name <span className="text-red-400">*</span>
                    </label>
                    <input
                      {...register('roleName', { required: 'Role name is required' })}
                      placeholder="e.g. admin, manager, viewer"
                      className={`w-full border rounded-lg px-3 py-2.5 text-sm outline-none transition-colors
                        ${errors.roleName
                          ? 'border-red-300 bg-red-50 focus:border-red-400'
                          : 'border-gray-200 focus:border-blue-400 focus:ring-2 focus:ring-blue-50'
                        }`}
                    />
                    {errors.roleName && (
                      <p className="text-xs text-red-500 mt-1">{errors.roleName.message}</p>
                    )}
                  </div>

                  {/* Status */}
                  <div className="col-span-2">
                    <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wide mb-2">
                      Status <span className="text-red-400">*</span>
                    </label>
                    <div className="flex gap-3">
                      {[{ label: 'Active', value: 1 }, { label: 'Inactive', value: 0 }].map(opt => (
                        <label key={opt.value}
                          className="flex items-center gap-2 border border-gray-200 rounded-lg px-4 py-2.5 cursor-pointer hover:border-blue-300 transition-colors has-[:checked]:border-blue-500 has-[:checked]:bg-blue-50">
                          <input type="radio" value={opt.value} {...register('isActive')} className="accent-blue-600" />
                          <span className="text-sm text-gray-700">{opt.label}</span>
                        </label>
                      ))}
                    </div>
                  </div>
                </div>

                {createMutation.isError && (
                  <div className="mt-4 bg-red-50 border border-red-200 rounded-lg px-4 py-3">
                    <p className="text-sm text-red-600">
                      {(createMutation.error as any)?.response?.data?.message || 'Failed to create role.'}
                    </p>
                  </div>
                )}

              </form>
            </div>

            {/* Card footer */}
            <div className="px-6 py-4 bg-gray-50 border-t border-gray-100 flex items-center justify-end gap-3">
              <button
                onClick={() => navigate('/admin/roles')}
                className="px-5 py-2 border border-gray-200 hover:bg-white text-gray-600 text-sm font-medium rounded-lg transition-colors"
              >
                Cancel
              </button>
              <button
                onClick={handleSubmit(onSubmit)}
                className="px-6 py-2 bg-blue-600 hover:bg-blue-700 text-white text-sm font-semibold rounded-lg transition-colors flex items-center gap-2"
              >
                <Shield size={14} />
                Create Role
              </button>
            </div>
          </div>

        </div>
      </div>
    </div>
  )
}