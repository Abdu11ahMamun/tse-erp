// src/pages/admin/UserPage.tsx

import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { Pencil, Trash2, Plus, X, User } from 'lucide-react'
import { userApi } from '../../api/userApi'
import { roleApi } from '../../api/roleApi'
import type { AfmUser, CreateUserRequest, UpdateUserRequest } from '../../types/user.types'

function Toast({ message, type }: { message: string; type: 'success' | 'error' }) {
  return (
    <div className={`fixed top-5 right-5 z-50 flex items-center gap-2.5 px-4 py-3 rounded-lg shadow-lg text-sm font-medium
      ${type === 'success' ? 'bg-green-600 text-white' : 'bg-red-600 text-white'}`}>
      <span>{type === 'success' ? '✓' : '✕'}</span>
      {message}
    </div>
  )
}

function Modal({ isOpen, onClose, title, children }: {
  isOpen: boolean; onClose: () => void; title: string; children: React.ReactNode
}) {
  if (!isOpen) return null
  return (
    <div className="fixed inset-0 z-40 flex items-center justify-center">
      <div className="absolute inset-0 bg-black/30" onClick={onClose} />
      <div className="relative bg-white rounded-xl shadow-xl w-full max-w-lg mx-4 z-50 max-h-[90vh] overflow-y-auto">
        <div className="flex items-center justify-between px-5 py-4 border-b border-gray-100 sticky top-0 bg-white">
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

export default function UserPage() {
  const queryClient = useQueryClient()
  const [editingUser, setEditingUser] = useState<AfmUser | null>(null)
  const [modalOpen, setModalOpen] = useState(false)
  const [formMode, setFormMode] = useState<'create' | 'edit'>('create')
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' } | null>(null)
  const [currentPage, setCurrentPage] = useState(1)
  const itemsPerPage = 10

  const showToast = (message: string, type: 'success' | 'error' = 'success') => {
    setToast({ message, type })
    setTimeout(() => setToast(null), 3000)
  }

  const { data: users = [], isLoading } = useQuery({
    queryKey: ['users'],
    queryFn: userApi.getAll,
  })

  const { data: roles = [] } = useQuery({
    queryKey: ['roles'],
    queryFn: roleApi.getAll,
  })

  const totalPages = Math.ceil(users.length / itemsPerPage)
  const paginatedUsers = users.slice(
    (currentPage - 1) * itemsPerPage,
    currentPage * itemsPerPage
  )

  const { register, handleSubmit, reset, formState: { errors } } = useForm<CreateUserRequest>({
    defaultValues: {
      userName: '', fullName: '', email: '', password: '',
      mobileNo: '', roleId: 0, userTypeId: 1, isActive: 1, gender: '',
    },
  })

  const createMutation = useMutation({
    mutationFn: userApi.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] })
      closeModal()
      showToast('User created successfully!')
    },
    onError: (error: any) => {
      const message = error?.response?.data?.message || error?.message || 'Failed to create user.'
      showToast(message, 'error')
    },
  })

  const updateMutation = useMutation({
    mutationFn: ({ id, payload }: { id: number; payload: UpdateUserRequest }) =>
      userApi.update(id, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] })
      closeModal()
      showToast('User updated successfully!')
    },
    onError: (error: any) => {
      const message = error?.response?.data?.message || error?.message || 'Failed to update user.'
      showToast(message, 'error')
    },
  })

  const deleteMutation = useMutation({
    mutationFn: userApi.delete,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] })
      showToast('User deleted.')
    },
    onError: () => showToast('Failed to delete user.', 'error'),
  })

  const onSubmit = (data: CreateUserRequest) => {
    const base = {
      userName: data.userName,
      fullName: data.fullName,
      email: data.email,
      mobileNo: data.mobileNo || null,
      fromDate: data.fromDate || null,
      toDate: data.toDate || null,
      roleId: Number(data.roleId),
      userTypeId: Number(data.userTypeId),
      isActive: Number(data.isActive) as 0 | 1,
      gender: data.gender || null,
    }

    if (formMode === 'edit' && editingUser) {
      updateMutation.mutate({ id: editingUser.id, payload: { id: editingUser.id, ...base } })
    } else {
      createMutation.mutate({ ...base, password: data.password })
    }
  }

  const openCreateModal = () => {
    setFormMode('create')
    setEditingUser(null)
    reset({
      userName: '', fullName: '', email: '', password: '',
      mobileNo: '', roleId: 0, userTypeId: 1, isActive: 1, gender: '',
    })
    setModalOpen(true)
  }

  const openEditModal = (user: AfmUser) => {
    setFormMode('edit')
    setEditingUser(user)
    reset({
      userName: user.userName,
      fullName: user.fullName,
      email: user.email,
      password: '',
      mobileNo: user.mobileNo ?? '',
      roleId: user.roleId,
      userTypeId: user.userTypeId,
      isActive: user.isActive,
      gender: user.gender ?? '',
    })
    setModalOpen(true)
  }

  const closeModal = () => {
    setModalOpen(false)
    setEditingUser(null)
    reset()
  }

  const getRoleName = (id: number) => roles.find(r => r.id === id)?.roleName ?? '—'
  const isSubmitting = createMutation.isPending || updateMutation.isPending

  return (
    <div className="p-6">
      {toast && <Toast message={toast.message} type={toast.type} />}

      {/* Modal */}
      <Modal isOpen={modalOpen} onClose={closeModal} title={formMode === 'edit' ? 'Edit User' : 'New User'}>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">

          {/* Full Name + Username */}
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wide mb-1.5">
                Full Name <span className="text-red-400">*</span>
              </label>
              <input
                {...register('fullName', { required: 'Required' })}
                placeholder="e.g. John Doe"
                className={`w-full border rounded-lg px-3 py-2 text-sm outline-none transition-colors
                  ${errors.fullName ? 'border-red-300 bg-red-50' : 'border-gray-200 focus:border-blue-400 focus:ring-2 focus:ring-blue-50'}`}
              />
              {errors.fullName && <p className="text-xs text-red-500 mt-1">{errors.fullName.message}</p>}
            </div>
            <div>
              <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wide mb-1.5">
                Username <span className="text-red-400">*</span>
              </label>
              <input
                {...register('userName', { required: 'Required' })}
                placeholder="e.g. john123"
                className={`w-full border rounded-lg px-3 py-2 text-sm outline-none transition-colors
                  ${errors.userName ? 'border-red-300 bg-red-50' : 'border-gray-200 focus:border-blue-400 focus:ring-2 focus:ring-blue-50'}`}
              />
              {errors.userName && <p className="text-xs text-red-500 mt-1">{errors.userName.message}</p>}
            </div>
          </div>

          {/* Email */}
          <div>
            <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wide mb-1.5">
              Email <span className="text-red-400">*</span>
            </label>
            <input
              {...register('email', { required: 'Required' })}
              type="email"
              placeholder="e.g. john@example.com"
              className={`w-full border rounded-lg px-3 py-2 text-sm outline-none transition-colors
                ${errors.email ? 'border-red-300 bg-red-50' : 'border-gray-200 focus:border-blue-400 focus:ring-2 focus:ring-blue-50'}`}
            />
            {errors.email && <p className="text-xs text-red-500 mt-1">{errors.email.message}</p>}
          </div>

          {/* Password — only for create */}
          {formMode === 'create' && (
            <div>
              <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wide mb-1.5">
                Password <span className="text-red-400">*</span>
              </label>
              <input
                {...register('password', { required: formMode === 'create' ? 'Required' : false })}
                type="password"
                placeholder="Enter password"
                className={`w-full border rounded-lg px-3 py-2 text-sm outline-none transition-colors
                  ${errors.password ? 'border-red-300 bg-red-50' : 'border-gray-200 focus:border-blue-400 focus:ring-2 focus:ring-blue-50'}`}
              />
              {errors.password && <p className="text-xs text-red-500 mt-1">{errors.password.message}</p>}
            </div>
          )}

          {/* Mobile + Gender */}
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wide mb-1.5">
                Mobile No
              </label>
              <input
                {...register('mobileNo')}
                placeholder="e.g. 01700000000"
                className="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm outline-none focus:border-blue-400 focus:ring-2 focus:ring-blue-50"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wide mb-1.5">
                Gender
              </label>
              <select
                {...register('gender')}
                className="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm outline-none focus:border-blue-400 focus:ring-2 focus:ring-blue-50 bg-white"
              >
                <option value="">-- Select --</option>
                <option value="Male">Male</option>
                <option value="Female">Female</option>
                <option value="Other">Other</option>
              </select>
            </div>
          </div>

          {/* Role */}
          <div>
            <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wide mb-1.5">
              Role <span className="text-red-400">*</span>
            </label>
            <select
              {...register('roleId', { validate: v => Number(v) > 0 || 'Select a role' })}
              className={`w-full border rounded-lg px-3 py-2 text-sm outline-none bg-white transition-colors
                ${errors.roleId ? 'border-red-300 bg-red-50' : 'border-gray-200 focus:border-blue-400 focus:ring-2 focus:ring-blue-50'}`}
            >
              <option value={0}>-- Select Role --</option>
              {roles.map(r => <option key={r.id} value={r.id}>{r.roleName}</option>)}
            </select>
            {errors.roleId && <p className="text-xs text-red-500 mt-1">{errors.roleId.message}</p>}
          </div>

          {/* From Date + To Date */}
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wide mb-1.5">
                From Date
              </label>
              <input
                {...register('fromDate')}
                type="date"
                className="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm outline-none focus:border-blue-400 focus:ring-2 focus:ring-blue-50"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wide mb-1.5">
                To Date
              </label>
              <input
                {...register('toDate')}
                type="date"
                className="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm outline-none focus:border-blue-400 focus:ring-2 focus:ring-blue-50"
              />
            </div>
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
                  className="flex-1 flex items-center gap-2 border border-gray-200 rounded-lg px-3 py-2 cursor-pointer hover:border-blue-300 transition-colors has-[:checked]:border-blue-500 has-[:checked]:bg-blue-50"
                >
                  <input type="radio" value={opt.value} {...register('isActive')} className="accent-blue-600" />
                  <span className="text-sm text-gray-700">{opt.label}</span>
                </label>
              ))}
            </div>
          </div>

          {/* Buttons */}
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
                onClick={() => { if (confirm('Delete this user?')) { deleteMutation.mutate(editingUser!.id); closeModal() } }}
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
          <h1 className="text-xl font-semibold text-gray-800">Users</h1>
          <p className="text-sm text-gray-500 mt-0.5">Manage system users</p>
        </div>
        <button
          onClick={openCreateModal}
          className="flex items-center gap-2 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium rounded-lg transition-colors"
        >
          <Plus size={15} />
          Add User
        </button>
      </div>

      {/* Table */}
      <div className="bg-white border border-gray-200 rounded-xl shadow-sm overflow-hidden">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-gray-100 bg-gray-50">
              <th className="text-left px-4 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wide">#</th>
              <th className="text-left px-4 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wide">User</th>
              <th className="text-left px-4 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wide">Email</th>
              <th className="text-left px-4 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wide">Role</th>
              <th className="text-left px-4 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wide">Status</th>
              <th className="text-right px-4 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wide">Actions</th>
            </tr>
          </thead>
          <tbody>
            {isLoading ? (
              <tr><td colSpan={6} className="text-center py-10 text-gray-400">Loading...</td></tr>
            ) : users.length === 0 ? (
              <tr><td colSpan={6} className="text-center py-10 text-gray-400">No users found</td></tr>
            ) : (
              paginatedUsers.map((user, index) => (
                <tr key={user.id} className="border-b border-gray-50 hover:bg-gray-50 transition-colors">
                  <td className="px-4 py-3 text-gray-400">{(currentPage - 1) * itemsPerPage + index + 1}</td>
                  <td className="px-4 py-3">
                    <div className="flex items-center gap-2.5">
                      <div className="w-7 h-7 bg-blue-100 rounded-full flex items-center justify-center flex-shrink-0">
                        <User size={13} className="text-blue-600" />
                      </div>
                      <div>
                        <p className="font-medium text-gray-800 leading-none">{user.fullName}</p>
                        <p className="text-xs text-gray-400 mt-0.5">@{user.userName}</p>
                      </div>
                    </div>
                  </td>
                  <td className="px-4 py-3 text-gray-500">{user.email}</td>
                  <td className="px-4 py-3">
                    <span className="text-xs bg-purple-50 text-purple-700 border border-purple-100 px-2 py-0.5 rounded-md font-medium">
                      {getRoleName(user.roleId)}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium
                      ${user.isActive === 1 ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-500'}`}>
                      {user.isActive === 1 ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex items-center justify-end gap-1">
                      <button
                        onClick={() => openEditModal(user)}
                        className="p-1.5 rounded-lg text-gray-400 hover:text-blue-600 hover:bg-blue-50 transition-colors"
                      >
                        <Pencil size={14} />
                      </button>
                      <button
                        onClick={() => { if (confirm('Delete this user?')) deleteMutation.mutate(user.id) }}
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
              Showing {(currentPage - 1) * itemsPerPage + 1}–{Math.min(currentPage * itemsPerPage, users.length)} of {users.length}
            </p>
            <div className="flex items-center gap-1">
              <button
                onClick={() => setCurrentPage(p => Math.max(1, p - 1))}
                disabled={currentPage === 1}
                className="px-3 py-1.5 text-xs border border-gray-200 rounded-lg disabled:opacity-40 hover:bg-gray-50 transition-colors"
              >Previous</button>
              {Array.from({ length: totalPages }, (_, i) => i + 1).map(page => (
                <button
                  key={page}
                  onClick={() => setCurrentPage(page)}
                  className={`w-8 h-8 text-xs rounded-lg transition-colors
                    ${currentPage === page ? 'bg-blue-600 text-white' : 'border border-gray-200 hover:bg-gray-50 text-gray-600'}`}
                >{page}</button>
              ))}
              <button
                onClick={() => setCurrentPage(p => Math.min(totalPages, p + 1))}
                disabled={currentPage === totalPages}
                className="px-3 py-1.5 text-xs border border-gray-200 rounded-lg disabled:opacity-40 hover:bg-gray-50 transition-colors"
              >Next</button>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}