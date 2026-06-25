// src/pages/admin/RoleCreatePage.tsx

import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { ArrowLeft, Shield } from 'lucide-react'
import { roleApi } from '../../api/roleApi'
import type { CreateRoleRequest } from '../../types/role.types'

function ConfirmModal({ isOpen, onConfirm, onCancel, isLoading }: {
  isOpen: boolean
  onConfirm: () => void
  onCancel: () => void
  isLoading: boolean
}) {
  if (!isOpen) return null
  return (
    <div className="fixed inset-0 z-40 flex items-center justify-center">
      <div className="absolute inset-0 bg-black/30" onClick={onCancel} />
      <div className="relative bg-white rounded-xl shadow-xl w-full max-w-sm mx-4 z-50 p-6">
        <div className="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center mx-auto mb-4">
          <Shield size={20} className="text-blue-600" />
        </div>
        <h3 className="text-base font-semibold text-gray-800 text-center mb-1">Confirm Create</h3>
        <p className="text-sm text-gray-500 text-center mb-6">
          Are you sure you want to create this role?
        </p>
        <div className="flex gap-3">
          <button
            onClick={onCancel}
            disabled={isLoading}
            className="flex-1 py-2 border border-gray-200 hover:bg-gray-50 text-gray-600 text-sm font-medium rounded-lg transition-colors"
          >
            Cancel
          </button>
          <button
            onClick={onConfirm}
            disabled={isLoading}
            className="flex-1 py-2 bg-blue-600 hover:bg-blue-700 disabled:opacity-50 text-white text-sm font-medium rounded-lg transition-colors"
          >
            {isLoading ? 'Creating...' : 'Confirm'}
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

  const { register, handleSubmit, formState: { errors } } = useForm<CreateRoleRequest>({
    defaultValues: { roleName: '', isActive: 1 },
  })

  const createMutation = useMutation({
    mutationFn: roleApi.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['roles'] })
      navigate('/admin/roles', { state: { toast: 'Role created successfully!' } })
    },
    onError: (error: any) => {
      setShowConfirm(false)
      // error will show inline
    },
  })

  const onSubmit = (data: CreateRoleRequest) => {
    setPendingData({ ...data, isActive: Number(data.isActive) as 0 | 1 })
    setShowConfirm(true)
  }

  const handleConfirm = () => {
    if (pendingData) createMutation.mutate(pendingData)
  }

  return (
    <div className="p-6 max-w-xl">
      <ConfirmModal
        isOpen={showConfirm}
        onConfirm={handleConfirm}
        onCancel={() => setShowConfirm(false)}
        isLoading={createMutation.isPending}
      />

      {/* Header */}
      <div className="flex items-center gap-3 mb-6">
        <button
          onClick={() => navigate('/admin/roles')}
          className="p-1.5 rounded-lg text-gray-400 hover:text-gray-600 hover:bg-gray-100 transition-colors"
        >
          <ArrowLeft size={18} />
        </button>
        <div>
          <h1 className="text-xl font-semibold text-gray-800">New Role</h1>
          <p className="text-sm text-gray-500 mt-0.5">Create a new user role</p>
        </div>
      </div>

      {/* Form card */}
      <div className="bg-white border border-gray-200 rounded-xl shadow-sm p-6">
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">

          {/* Role Name */}
          <div>
            <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wide mb-1.5">
              Role Name <span className="text-red-400">*</span>
            </label>
            <input
              {...register('roleName', { required: 'Role name is required' })}
              placeholder="e.g. admin, manager, viewer"
              className={`w-full border rounded-lg px-3 py-2.5 text-sm outline-none transition-colors
                ${errors.roleName
                  ? 'border-red-300 bg-red-50'
                  : 'border-gray-200 focus:border-blue-400 focus:ring-2 focus:ring-blue-50'
                }`}
            />
            {errors.roleName && (
              <p className="text-xs text-red-500 mt-1">{errors.roleName.message}</p>
            )}
          </div>

          {/* Status */}
          <div>
            <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wide mb-2">
              Status <span className="text-red-400">*</span>
            </label>
            <div className="flex gap-2">
              {[{ label: 'Active', value: 1 }, { label: 'Inactive', value: 0 }].map(opt => (
                <label
                  key={opt.value}
                  className="flex-1 flex items-center gap-2 border border-gray-200 rounded-lg px-3 py-2.5 cursor-pointer hover:border-blue-300 transition-colors has-[:checked]:border-blue-500 has-[:checked]:bg-blue-50"
                >
                  <input type="radio" value={opt.value} {...register('isActive')} className="accent-blue-600" />
                  <span className="text-sm text-gray-700">{opt.label}</span>
                </label>
              ))}
            </div>
          </div>

          {/* Error */}
          {createMutation.isError && (
            <div className="bg-red-50 border border-red-200 rounded-lg px-3.5 py-3">
              <p className="text-sm text-red-600">
                {(createMutation.error as any)?.response?.data?.message || 'Failed to create role.'}
              </p>
            </div>
          )}

          {/* Actions */}
          <div className="flex gap-3 pt-1">
            <button
              type="button"
              onClick={() => navigate('/admin/roles')}
              className="px-5 py-2.5 border border-gray-200 hover:bg-gray-50 text-gray-600 text-sm font-medium rounded-lg transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="flex-1 py-2.5 bg-blue-600 hover:bg-blue-700 text-white text-sm font-semibold rounded-lg transition-colors"
            >
              Create Role
            </button>
          </div>

        </form>
      </div>
    </div>
  )
}