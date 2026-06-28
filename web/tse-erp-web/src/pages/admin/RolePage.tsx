// src/pages/admin/RolePage.tsx

import { useEffect, useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useNavigate, useLocation } from 'react-router-dom'
import { Plus, Eye, Pencil, Trash2, Shield } from 'lucide-react'
import { roleApi } from '../../api/roleApi'
import { ListPage, StatusBadge } from '../../components/ui/ListPage'

function Toast({ message, type }: { message: string; type: 'success' | 'error' }) {
  return (
    <div className={`fixed top-5 right-5 z-50 flex items-center gap-2.5 px-4 py-3 rounded-lg shadow-xl text-sm font-medium
      ${type === 'success' ? 'bg-gray-900 text-white' : 'bg-red-600 text-white'}`}>
      <span>{type === 'success' ? '✓' : '✕'}</span>
      {message}
    </div>
  )
}

function ConfirmModal({ isOpen, onConfirm, onCancel, roleName }: {
  isOpen: boolean; onConfirm: () => void; onCancel: () => void; roleName: string
}) {
  if (!isOpen) return null
  return (
    <div className="fixed inset-0 z-40 flex items-center justify-center">
      <div className="absolute inset-0 bg-black/20 backdrop-blur-sm" onClick={onCancel} />
      <div className="relative bg-white rounded-2xl shadow-2xl w-full max-w-sm mx-4 z-50 p-6">
        <h3 className="text-base font-semibold text-gray-900 mb-1">Delete role?</h3>
        <p className="text-sm text-gray-500 mb-6">
          <span className="font-medium text-gray-700">"{roleName}"</span> will be permanently removed. This cannot be undone.
        </p>
        <div className="flex gap-2.5">
          <button onClick={onCancel}
            className="flex-1 py-2 text-sm font-medium text-gray-600 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors">
            Cancel
          </button>
          <button onClick={onConfirm}
            className="flex-1 py-2 text-sm font-semibold text-white bg-red-600 hover:bg-red-700 rounded-lg transition-colors">
            Delete
          </button>
        </div>
      </div>
    </div>
  )
}

export default function RolePage() {
  const navigate = useNavigate()
  const location = useLocation()
  const queryClient = useQueryClient()
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' } | null>(null)
  const [deleteTarget, setDeleteTarget] = useState<{ id: number; name: string } | null>(null)

  const showToast = (message: string, type: 'success' | 'error' = 'success') => {
    setToast({ message, type })
    setTimeout(() => setToast(null), 3000)
  }

  useEffect(() => {
    if (location.state?.toast) { showToast(location.state.toast); window.history.replaceState({}, '') }
  }, [])

  const { data: roles = [], isLoading } = useQuery({ queryKey: ['roles'], queryFn: roleApi.getAll })

  const deleteMutation = useMutation({
    mutationFn: roleApi.delete,
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['roles'] }); setDeleteTarget(null); showToast('Role deleted.') },
    onError: () => showToast('Failed to delete role.', 'error'),
  })

  return (
    <>
      {toast && <Toast message={toast.message} type={toast.type} />}
      <ConfirmModal
        isOpen={!!deleteTarget}
        roleName={deleteTarget?.name ?? ''}
        onConfirm={() => deleteTarget && deleteMutation.mutate(deleteTarget.id)}
        onCancel={() => setDeleteTarget(null)}
      />

      <ListPage
        title="Roles"
        description="Manage access roles and permission groups"
        data={roles}
        keyExtractor={r => r.id}
        isLoading={isLoading}
        searchPlaceholder="Search roles..."
        searchKeys={['roleName']}
        emptyIcon={<Shield size={20} />}
        emptyTitle="No roles found"
        emptyDescription="Create your first role to get started"
        primaryAction={{
          label: 'New Role',
          onClick: () => navigate('/admin/roles/create'),
          icon: <Plus size={14} />,
        }}
        filters={[
          {
            key: 'isActive',
            label: 'Status',
            options: [
              { label: 'All', value: 'all' },
              { label: 'Active', value: '1' },
              { label: 'Inactive', value: '0' },
            ],
          },
        ]}
        columns={[
          {
            key: 'roleName',
            label: 'Role Name',
            sortable: true,
            render: (row) => (
              <div className="flex items-center gap-3">
                <div className="w-7 h-7 rounded-lg bg-gray-100 flex items-center justify-center flex-shrink-0">
                  <Shield size={13} className="text-gray-500" />
                </div>
                <span className="text-[14px] font-medium text-gray-900">{row.roleName}</span>
              </div>
            ),
          },
          {
            key: 'isActive',
            label: 'Status',
            width: 'w-28',
            render: (row) => <StatusBadge active={row.isActive === 1} />,
          },
          {
            key: 'createdAt',
            label: 'Created',
            sortable: true,
            width: 'w-36',
            render: (row) => (
              <span className="text-[13px] text-gray-400">
                {row.createdAt ? new Date(row.createdAt).toLocaleDateString('en-GB', {
                  day: '2-digit', month: 'short', year: 'numeric'
                }) : '—'}
              </span>
            ),
          },
          {
            key: '_actions',
            label: '',
            width: 'w-28',
            render: (row) => (
              <div className="flex items-center justify-end gap-0.5 opacity-0 group-hover:opacity-100 transition-opacity duration-150">
                <button
                  onClick={e => { e.stopPropagation(); navigate(`/admin/roles/${row.id}/view`) }}
                  className="w-7 h-7 flex items-center justify-center rounded-md text-gray-400 hover:text-gray-700 hover:bg-gray-100 transition-colors"
                  title="View"
                >
                  <Eye size={13} />
                </button>
                <button
                  onClick={e => { e.stopPropagation(); navigate(`/admin/roles/${row.id}/edit`) }}
                  className="w-7 h-7 flex items-center justify-center rounded-md text-gray-400 hover:text-gray-700 hover:bg-gray-100 transition-colors"
                  title="Edit"
                >
                  <Pencil size={13} />
                </button>
                <button
                  onClick={e => { e.stopPropagation(); setDeleteTarget({ id: row.id, name: row.roleName }) }}
                  className="w-7 h-7 flex items-center justify-center rounded-md text-gray-400 hover:text-red-600 hover:bg-red-50 transition-colors"
                  title="Delete"
                >
                  <Trash2 size={13} />
                </button>
              </div>
            ),
          },
        ]}
      />
    </>
  )
}