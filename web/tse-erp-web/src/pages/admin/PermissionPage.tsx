// src/pages/admin/PermissionPage.tsx

import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { Pencil, Trash2, Plus, X } from 'lucide-react'
import { permissionApi } from '../../api/permissionApi'
import { moduleApi } from '../../api/moduleApi'
import type { AfmPermission, CreatePermissionRequest } from '../../types/permission.types'

function Toast({ message, type }: { message: string; type: 'success' | 'error' }) {
  return (
    <div className={`fixed top-5 right-5 z-50 flex items-center gap-2.5 px-4 py-3 rounded-lg shadow-lg text-sm font-medium
      ${type === 'success' ? 'bg-green-600 text-white' : 'bg-red-600 text-white'}`}>
      <span>{type === 'success' ? '✓' : '✕'}</span>
      {message}
    </div>
  )
}

interface ModalProps {
  isOpen: boolean
  onClose: () => void
  title: string
  children: React.ReactNode
}

function Modal({ isOpen, onClose, title, children }: ModalProps) {
  if (!isOpen) return null
  return (
    <div className="fixed inset-0 z-40 flex items-center justify-center">
      <div className="absolute inset-0 bg-black/30" onClick={onClose} />
      <div className="relative bg-white rounded-xl shadow-xl w-full max-w-md mx-4 z-50">
        <div className="flex items-center justify-between px-5 py-4 border-b border-gray-100">
          <div className="flex items-center gap-2.5">
            <div className="w-1 h-5 bg-blue-600 rounded-full" />
            <h2 className="text-sm font-semibold text-gray-800">{title}</h2>
          </div>
          <button onClick={onClose} className="text-gray-400 hover:text-gray-600 transition-colors">
            <X size={16} />
          </button>
        </div>
        <div className="px-5 py-4">{children}</div>
      </div>
    </div>
  )
}

export default function PermissionPage() {
  const queryClient = useQueryClient()
  const [editingPermission, setEditingPermission] = useState<AfmPermission | null>(null)
  const [modalOpen, setModalOpen] = useState(false)
  const [formMode, setFormMode] = useState<'create' | 'edit'>('create')
  const [filterModuleId, setFilterModuleId] = useState<number | ''>('')
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' } | null>(null)
  const [currentPage, setCurrentPage] = useState(1)
  const itemsPerPage = 10

  const showToast = (message: string, type: 'success' | 'error' = 'success') => {
    setToast({ message, type })
    setTimeout(() => setToast(null), 3000)
  }

  const { data: permissions = [], isLoading } = useQuery({
    queryKey: ['permissions'],
    queryFn: permissionApi.getAll,
  })

  const { data: modules = [] } = useQuery({
    queryKey: ['modules'],
    queryFn: moduleApi.getAll,
  })

  const filteredPermissions = filterModuleId
    ? permissions.filter(p => p.moduleId === Number(filterModuleId))
    : permissions

  const totalPages = Math.ceil(filteredPermissions.length / itemsPerPage)
  const paginatedPermissions = filteredPermissions.slice(
    (currentPage - 1) * itemsPerPage,
    currentPage * itemsPerPage
  )

  const { register, handleSubmit, reset, formState: { errors } } = useForm<CreatePermissionRequest>({
    defaultValues: { permissionName: '', moduleId: 0, isActive: 1 },
  })

 const createMutation = useMutation({
  mutationFn: permissionApi.create,
  onSuccess: () => {
    queryClient.invalidateQueries({ queryKey: ['permissions'] })
    closeModal()
    showToast('Permission created successfully!')
  },
  onError: (error: any) => {
    const message = error?.response?.data?.message || 'Failed to create permission.'
    showToast(message, 'error')
  },
  })

  const updateMutation = useMutation({
    mutationFn: ({ id, payload }: { id: number; payload: CreatePermissionRequest }) =>
      permissionApi.update(id, { ...payload, id }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['permissions'] })
      closeModal()
      showToast('Permission updated successfully!')
    },
    onError: (error: any) => {
    const message = error?.response?.data?.message || 'Failed to update permission.'
    showToast(message, 'error')
    },
  })

  const deleteMutation = useMutation({
    mutationFn: permissionApi.delete,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['permissions'] })
      showToast('Permission deleted.')
    },
    onError: () => showToast('Failed to delete permission.', 'error'),
  })

  const onSubmit = (data: CreatePermissionRequest) => {
    const payload = { ...data, moduleId: Number(data.moduleId), isActive: Number(data.isActive) as 0 | 1 }
    if (formMode === 'edit' && editingPermission) {
      updateMutation.mutate({ id: editingPermission.id, payload })
    } else {
      createMutation.mutate(payload)
    }
  }

  const openCreateModal = () => {
    setFormMode('create')
    setEditingPermission(null)
    reset({ permissionName: '', moduleId: 0, isActive: 1 })
    setModalOpen(true)
  }

  const openEditModal = (perm: AfmPermission) => {
    setFormMode('edit')
    setEditingPermission(perm)
    reset({ permissionName: perm.permissionName, moduleId: perm.moduleId, isActive: perm.isActive })
    setModalOpen(true)
  }

  const closeModal = () => {
    setModalOpen(false)
    setEditingPermission(null)
    reset()
  }

  const getModuleName = (moduleId: number) =>
    modules.find(m => m.id === moduleId)?.moduleName ?? '—'

  const isSubmitting = createMutation.isPending || updateMutation.isPending

  return (
    <div className="p-6">
      {toast && <Toast message={toast.message} type={toast.type} />}

      {/* Modal */}
      <Modal
        isOpen={modalOpen}
        onClose={closeModal}
        title={formMode === 'edit' ? 'Edit Permission' : 'New Permission'}
      >
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wide mb-1.5">
              Permission Name <span className="text-red-400">*</span>
            </label>
            <input
              {...register('permissionName', { required: 'Required' })}
              placeholder="e.g. user.create"
              className={`w-full border rounded-lg px-3 py-2 text-sm outline-none transition-colors
                ${errors.permissionName
                  ? 'border-red-300 bg-red-50'
                  : 'border-gray-200 focus:border-blue-400 focus:ring-2 focus:ring-blue-50'
                }`}
            />
            {errors.permissionName && (
              <p className="text-xs text-red-500 mt-1">{errors.permissionName.message}</p>
            )}
          </div>

          <div>
            <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wide mb-1.5">
              Module <span className="text-red-400">*</span>
            </label>
            <select
              {...register('moduleId', { validate: v => Number(v) > 0 || 'Select a module' })}
              className={`w-full border rounded-lg px-3 py-2 text-sm outline-none transition-colors bg-white
                ${errors.moduleId
                  ? 'border-red-300 bg-red-50'
                  : 'border-gray-200 focus:border-blue-400 focus:ring-2 focus:ring-blue-50'
                }`}
            >
              <option value={0}>-- Select Module --</option>
              {modules.map(m => (
                <option key={m.id} value={m.id}>{m.moduleName}</option>
              ))}
            </select>
            {errors.moduleId && (
              <p className="text-xs text-red-500 mt-1">{errors.moduleId.message}</p>
            )}
          </div>

          <div>
            <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wide mb-2">
              Status <span className="text-red-400">*</span>
            </label>
            <div className="flex gap-2">
              {[{ label: 'Active', value: 1 }, { label: 'Inactive', value: 0 }].map(opt => (
                <label
                  key={opt.value}
                  className="flex-1 flex items-center gap-2 border border-gray-200 rounded-lg px-3 py-2 cursor-pointer hover:border-blue-300 transition-colors has-[:checked]:border-blue-500 has-[:checked]:bg-blue-50"
                >
                  <input type="radio" value={opt.value} {...register('isActive')} className="accent-blue-600" />
                  <span className="text-sm text-gray-700">{opt.label}</span>
                </label>
              ))}
            </div>
          </div>

          <div className="flex gap-2 pt-1">
            <button
              type="submit"
              disabled={isSubmitting}
              className="flex-1 py-2.5 bg-blue-600 hover:bg-blue-700 disabled:opacity-50 text-white text-sm font-medium rounded-lg transition-colors"
            >
              {isSubmitting ? 'Saving...' : formMode === 'edit' ? 'Update' : 'Save'}
            </button>
            {formMode === 'edit' && (
              <button
                type="button"
                onClick={() => {
                  if (confirm('Delete this permission?')) {
                    deleteMutation.mutate(editingPermission!.id)
                    closeModal()
                  }
                }}
                className="px-4 py-2.5 bg-red-50 hover:bg-red-100 text-red-600 text-sm font-medium rounded-lg border border-red-200 transition-colors"
              >
                <Trash2 size={14} />
              </button>
            )}
            <button
              type="button"
              onClick={closeModal}
              className="px-4 py-2.5 border border-gray-200 hover:bg-gray-50 text-gray-600 text-sm rounded-lg transition-colors"
            >
              Cancel
            </button>
          </div>
        </form>
      </Modal>

      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-xl font-semibold text-gray-800">Permissions</h1>
          <p className="text-sm text-gray-500 mt-0.5">Manage module permissions</p>
        </div>
        <button
          onClick={openCreateModal}
          className="flex items-center gap-2 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium rounded-lg transition-colors"
        >
          <Plus size={15} />
          New Permission
        </button>
      </div>

      {/* Module filter */}
      <div className="mb-4">
        <select
          value={filterModuleId}
          onChange={e => { setFilterModuleId(e.target.value ? Number(e.target.value) : ''); setCurrentPage(1) }}
          className="border border-gray-200 rounded-lg px-3 py-2 text-sm outline-none focus:border-blue-400 focus:ring-2 focus:ring-blue-50 bg-white min-w-48"
        >
          <option value="">All Modules</option>
          {modules.map(m => (
            <option key={m.id} value={m.id}>{m.moduleName}</option>
          ))}
        </select>
      </div>

      {/* Table */}
      <div className="bg-white border border-gray-200 rounded-xl shadow-sm overflow-hidden">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-gray-100 bg-gray-50">
              <th className="text-left px-4 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wide">#</th>
              <th className="text-left px-4 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wide">Permission Name</th>
              <th className="text-left px-4 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wide">Module</th>
              <th className="text-left px-4 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wide">Status</th>
              <th className="text-right px-4 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wide">Actions</th>
            </tr>
          </thead>
          <tbody>
            {isLoading ? (
              <tr><td colSpan={5} className="text-center py-10 text-gray-400">Loading...</td></tr>
            ) : filteredPermissions.length === 0 ? (
              <tr><td colSpan={5} className="text-center py-10 text-gray-400">No permissions found</td></tr>
            ) : (
              paginatedPermissions.map((perm, index) => (
                <tr key={perm.id} className="border-b border-gray-50 hover:bg-gray-50 transition-colors">
                  <td className="px-4 py-3 text-gray-400">{(currentPage - 1) * itemsPerPage + index + 1}</td>
                  <td className="px-4 py-3 font-medium text-gray-800">{perm.permissionName}</td>
                  <td className="px-4 py-3 text-gray-500">{getModuleName(perm.moduleId)}</td>
                  <td className="px-4 py-3">
                    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium
                      ${perm.isActive === 1 ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-500'}`}>
                      {perm.isActive === 1 ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex items-center justify-end gap-1">
                      <button
                        onClick={() => openEditModal(perm)}
                        className="p-1.5 rounded-lg text-gray-400 hover:text-blue-600 hover:bg-blue-50 transition-colors"
                      >
                        <Pencil size={14} />
                      </button>
                      <button
                        onClick={() => { if (confirm('Delete this permission?')) deleteMutation.mutate(perm.id) }}
                        className="p-1.5 rounded-lg text-gray-400 hover:text-red-600 hover:bg-red-50 transition-colors"
                      >
                        <Trash2 size={14} />
                      </button>
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>

        {totalPages > 1 && (
          <div className="flex items-center justify-between px-4 py-3 border-t border-gray-100">
            <p className="text-xs text-gray-500">
              Showing {(currentPage - 1) * itemsPerPage + 1}–{Math.min(currentPage * itemsPerPage, filteredPermissions.length)} of {filteredPermissions.length}
            </p>
            <div className="flex items-center gap-1">
              <button
                onClick={() => setCurrentPage(p => Math.max(1, p - 1))}
                disabled={currentPage === 1}
                className="px-3 py-1.5 text-xs border border-gray-200 rounded-lg disabled:opacity-40 hover:bg-gray-50 transition-colors"
              >
                Previous
              </button>
              {Array.from({ length: totalPages }, (_, i) => i + 1).map(page => (
                <button
                  key={page}
                  onClick={() => setCurrentPage(page)}
                  className={`w-8 h-8 text-xs rounded-lg transition-colors
                    ${currentPage === page ? 'bg-blue-600 text-white' : 'border border-gray-200 hover:bg-gray-50 text-gray-600'}`}
                >
                  {page}
                </button>
              ))}
              <button
                onClick={() => setCurrentPage(p => Math.min(totalPages, p + 1))}
                disabled={currentPage === totalPages}
                className="px-3 py-1.5 text-xs border border-gray-200 rounded-lg disabled:opacity-40 hover:bg-gray-50 transition-colors"
              >
                Next
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}