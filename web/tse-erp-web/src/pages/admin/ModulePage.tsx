// src/pages/admin/ModulePage.tsx

import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { Pencil, Trash2, Plus, X } from 'lucide-react'
import { moduleApi } from '../../api/moduleApi'
import type { AfmModule, CreateModuleRequest } from '../../types/module.types'

function Toast({ message, type }: { message: string; type: 'success' | 'error' }) {
  return (
    <div className={`fixed top-5 right-5 z-50 flex items-center gap-2.5 px-4 py-3 rounded-lg shadow-lg text-sm font-medium
      ${type === 'success' ? 'bg-green-600 text-white' : 'bg-red-600 text-white'}`}>
      <span>{type === 'success' ? '✓' : '✕'}</span>
      {message}
    </div>
  )
}

export default function ModulePage() {
  const queryClient = useQueryClient()
  const [editingModule, setEditingModule] = useState<AfmModule | null>(null)
  const [showForm, setShowForm] = useState(false)
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' } | null>(null)
  const [currentPage, setCurrentPage] = useState(1)
  const itemsPerPage = 10

  const showToast = (message: string, type: 'success' | 'error' = 'success') => {
    setToast({ message, type })
    setTimeout(() => setToast(null), 3000)
  }

  const { data: modules = [], isLoading } = useQuery({
    queryKey: ['modules'],
    queryFn: moduleApi.getAll,
  })

  const totalPages = Math.ceil(modules.length / itemsPerPage)
  const paginatedModules = modules.slice(
    (currentPage - 1) * itemsPerPage,
    currentPage * itemsPerPage
  )

  const { register, handleSubmit, reset, formState: { errors } } = useForm<CreateModuleRequest>({
    defaultValues: { moduleName: '', isActive: 1 },
  })

  const createMutation = useMutation({
    mutationFn: moduleApi.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['modules'] })
      reset()
      setShowForm(false)
      showToast('Module created successfully!')
    },
    onError: () => showToast('Failed to create module.', 'error'),
  })

  const updateMutation = useMutation({
    mutationFn: ({ id, payload }: { id: number; payload: CreateModuleRequest }) =>
      moduleApi.update(id, { ...payload, id }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['modules'] })
      setEditingModule(null)
      setShowForm(false)
      reset()
      showToast('Module updated successfully!')
    },
    onError: () => showToast('Failed to update module.', 'error'),
  })

  const deleteMutation = useMutation({
    mutationFn: moduleApi.delete,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['modules'] })
      showToast('Module deleted.')
    },
    onError: () => showToast('Failed to delete module.', 'error'),
  })

  const onSubmit = (data: CreateModuleRequest) => {
    if (editingModule) {
      updateMutation.mutate({ id: editingModule.id, payload: data })
    } else {
      createMutation.mutate(data)
    }
  }

  const handleEdit = (mod: AfmModule) => {
    setEditingModule(mod)
    reset({ moduleName: mod.moduleName, isActive: mod.isActive })
    setShowForm(true)
  }

  const handleCancel = () => {
    setEditingModule(null)
    setShowForm(false)
    reset()
  }

  const isSubmitting = createMutation.isPending || updateMutation.isPending

  return (
    <div className="p-6 max-w-4xl mx-auto">
      {toast && <Toast message={toast.message} type={toast.type} />}

      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-xl font-semibold text-gray-800">Modules</h1>
          <p className="text-sm text-gray-500 mt-0.5">Manage system modules</p>
        </div>
        {!showForm && (
          <button
            onClick={() => setShowForm(true)}
            className="flex items-center gap-2 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium rounded-lg transition-colors"
          >
            <Plus size={15} />
            Add Module
          </button>
        )}
      </div>

      {/* Form */}
      {showForm && (
        <div className="bg-white border border-gray-200 rounded-xl p-5 mb-6 shadow-sm">
          <div className="flex items-center justify-between mb-4">
            <div className="flex items-center gap-2.5">
              <div className="w-1 h-5 bg-blue-600 rounded-full" />
              <h2 className="text-sm font-semibold text-gray-800">
                {editingModule ? 'Edit Module' : 'New Module'}
              </h2>
            </div>
            <button onClick={handleCancel} className="text-gray-400 hover:text-gray-600">
              <X size={16} />
            </button>
          </div>

          <form onSubmit={handleSubmit(onSubmit)} className="flex gap-3 items-end">
            <div className="flex-1">
              <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wide mb-1.5">
                Module Name <span className="text-red-400">*</span>
              </label>
              <input
                {...register('moduleName', { required: 'Required' })}
                placeholder="e.g. Accounting"
                className={`w-full border rounded-lg px-3 py-2 text-sm outline-none transition-colors
                  ${errors.moduleName
                    ? 'border-red-300 bg-red-50'
                    : 'border-gray-200 focus:border-blue-400 focus:ring-2 focus:ring-blue-50'
                  }`}
              />
              {errors.moduleName && (
                <p className="text-xs text-red-500 mt-1">{errors.moduleName.message}</p>
              )}
            </div>

            <div>
              <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wide mb-1.5">
                Status <span className="text-red-400">*</span>
              </label>
              <select
                {...register('isActive')}
                className="border border-gray-200 rounded-lg px-3 py-2 text-sm outline-none focus:border-blue-400 focus:ring-2 focus:ring-blue-50"
              >
                <option value={1}>Active</option>
                <option value={0}>Inactive</option>
              </select>
            </div>

            <div className="flex gap-2">
              <button
                type="submit"
                disabled={isSubmitting}
                className="px-5 py-2 bg-blue-600 hover:bg-blue-700 disabled:opacity-50 text-white text-sm font-medium rounded-lg transition-colors"
              >
                {isSubmitting ? 'Saving...' : editingModule ? 'Update' : 'Save'}
              </button>
              <button
                type="button"
                onClick={handleCancel}
                className="px-4 py-2 border border-gray-200 hover:bg-gray-50 text-gray-600 text-sm rounded-lg transition-colors"
              >
                Cancel
              </button>
            </div>
          </form>
        </div>
      )}

      {/* Table */}
      <div className="bg-white border border-gray-200 rounded-xl shadow-sm overflow-hidden">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-gray-100 bg-gray-50">
              <th className="text-left px-4 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wide">#</th>
              <th className="text-left px-4 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wide">Module Name</th>
              <th className="text-left px-4 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wide">Status</th>
              <th className="text-left px-4 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wide">Created At</th>
              <th className="text-right px-4 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wide">Actions</th>
            </tr>
          </thead>
          <tbody>
            {isLoading ? (
              <tr>
                <td colSpan={5} className="text-center py-10 text-gray-400">Loading...</td>
              </tr>
            ) : modules.length === 0 ? (
              <tr>
                <td colSpan={5} className="text-center py-10 text-gray-400">No modules found</td>
              </tr>
            ) : (
              paginatedModules.map((mod, index) => (
                <tr key={mod.id} className="border-b border-gray-50 hover:bg-gray-50 transition-colors">
                  <td className="px-4 py-3 text-gray-400">{(currentPage - 1) * itemsPerPage + index + 1}</td>
                  <td className="px-4 py-3 font-medium text-gray-800">{mod.moduleName}</td>
                  <td className="px-4 py-3">
                    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium
                      ${mod.isActive === 1
                        ? 'bg-green-100 text-green-700'
                        : 'bg-gray-100 text-gray-500'
                      }`}>
                      {mod.isActive === 1 ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-gray-500">
                    {mod.createdAt ? new Date(mod.createdAt).toLocaleDateString('en-GB', {
                        day: '2-digit', month: 'short', year: 'numeric'
                    }) : '—'}
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex items-center justify-end gap-1">
                      <button
                        onClick={() => handleEdit(mod)}
                        className="p-1.5 rounded-lg text-gray-400 hover:text-blue-600 hover:bg-blue-50 transition-colors"
                      >
                        <Pencil size={14} />
                      </button>
                      <button
                        onClick={() => {
                          if (confirm('Delete this module?')) deleteMutation.mutate(mod.id)
                        }}
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

        {/* Pagination */}
        {totalPages > 1 && (
          <div className="flex items-center justify-between px-4 py-3 border-t border-gray-100">
            <p className="text-xs text-gray-500">
              Showing {(currentPage - 1) * itemsPerPage + 1}–{Math.min(currentPage * itemsPerPage, modules.length)} of {modules.length}
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
                    ${currentPage === page
                      ? 'bg-blue-600 text-white'
                      : 'border border-gray-200 hover:bg-gray-50 text-gray-600'
                    }`}
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