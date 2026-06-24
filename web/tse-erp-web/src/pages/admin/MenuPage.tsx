// src/pages/admin/MenuPage.tsx

import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { Pencil, Trash2, Plus, X, ChevronRight, ChevronDown } from 'lucide-react'
import { menuApi } from '../../api/menuApi'
import { moduleApi } from '../../api/moduleApi'
import type { AfmMenu, CreateMenuRequest, MenuTreeNode } from '../../types/menu.types'

// ── Toast ──────────────────────────────────────────────────────────────────
function Toast({ message, type }: { message: string; type: 'success' | 'error' }) {
  return (
    <div className={`fixed top-5 right-5 z-50 flex items-center gap-2.5 px-4 py-3 rounded-lg shadow-lg text-sm font-medium
      ${type === 'success' ? 'bg-green-600 text-white' : 'bg-red-600 text-white'}`}>
      <span>{type === 'success' ? '✓' : '✕'}</span>
      {message}
    </div>
  )
}

// ── Modal ──────────────────────────────────────────────────────────────────
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

// ── Build tree from flat list ──────────────────────────────────────────────
function buildMenuTree(flat: AfmMenu[]): MenuTreeNode[] {
  const map = new Map<number, MenuTreeNode>()
  flat.forEach(item => map.set(item.id, { ...item, children: [] }))
  const roots: MenuTreeNode[] = []
  map.forEach(node => {
    if (node.parentMenuId === null) {
      roots.push(node)
    } else {
      const parent = map.get(node.parentMenuId)
      if (parent) parent.children.push(node)
      else roots.push(node)
    }
  })
  return roots
}

// ── Tree row ──────────────────────────────────────────────────────────────
function MenuTreeRow({
  node, depth, onEdit, onDelete, expandedIds, toggleExpanded
}: {
  node: MenuTreeNode
  depth: number
  onEdit: (m: AfmMenu) => void
  onDelete: (id: number) => void
  expandedIds: Set<number>
  toggleExpanded: (id: number) => void
}) {
  const isExpanded = expandedIds.has(node.id)
  const hasChildren = node.children.length > 0

  return (
    <>
      <tr className="border-b border-gray-50 hover:bg-gray-50 transition-colors">
        <td className="px-4 py-2.5">
          <div className="flex items-center" style={{ paddingLeft: `${depth * 20}px` }}>
            <button
              onClick={() => hasChildren && toggleExpanded(node.id)}
              className={`w-5 h-5 flex items-center justify-center mr-1.5 flex-shrink-0 ${hasChildren ? 'text-gray-400 hover:text-gray-600 cursor-pointer' : 'invisible'}`}
            >
              {isExpanded ? <ChevronDown size={13} /> : <ChevronRight size={13} />}
            </button>
            <span className="text-sm font-medium text-gray-800">{node.menuName}</span>
            {node.isParent === 1 && (
              <span className="ml-2 text-xs bg-blue-50 text-blue-600 border border-blue-100 px-1.5 py-0.5 rounded-md">Parent</span>
            )}
          </div>
        </td>
        <td className="px-4 py-2.5 text-sm text-gray-500">{node.routeName ?? '—'}</td>
        <td className="px-4 py-2.5 text-sm text-gray-500">{node.sortOrder ?? '—'}</td>
        <td className="px-4 py-2.5">
          <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium
            ${node.isTopMenu === 1 ? 'bg-purple-100 text-purple-700' : 'bg-gray-100 text-gray-500'}`}>
            {node.isTopMenu === 1 ? 'Top' : 'Sub'}
          </span>
        </td>
        <td className="px-4 py-2.5">
          <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium
            ${node.isActive === 1 ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-500'}`}>
            {node.isActive === 1 ? 'Active' : 'Inactive'}
          </span>
        </td>
        <td className="px-4 py-2.5">
          <div className="flex items-center justify-end gap-1">
            <button
              onClick={() => onEdit(node)}
              className="p-1.5 rounded-lg text-gray-400 hover:text-blue-600 hover:bg-blue-50 transition-colors"
            >
              <Pencil size={14} />
            </button>
            <button
              onClick={() => { if (confirm('Delete this menu?')) onDelete(node.id) }}
              className="p-1.5 rounded-lg text-gray-400 hover:text-red-600 hover:bg-red-50 transition-colors"
            >
              <Trash2 size={14} />
            </button>
          </div>
        </td>
      </tr>
      {isExpanded && node.children.map(child => (
        <MenuTreeRow
          key={child.id}
          node={child}
          depth={depth + 1}
          onEdit={onEdit}
          onDelete={onDelete}
          expandedIds={expandedIds}
          toggleExpanded={toggleExpanded}
        />
      ))}
    </>
  )
}

// ── Main Page ──────────────────────────────────────────────────────────────
export default function MenuPage() {
  const queryClient = useQueryClient()
  const [editingMenu, setEditingMenu] = useState<AfmMenu | null>(null)
  const [modalOpen, setModalOpen] = useState(false)
  const [formMode, setFormMode] = useState<'create' | 'edit'>('create')
  const [filterModuleId, setFilterModuleId] = useState<number | ''>('')
  const [expandedIds, setExpandedIds] = useState<Set<number>>(new Set())
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' } | null>(null)

  const showToast = (message: string, type: 'success' | 'error' = 'success') => {
    setToast({ message, type })
    setTimeout(() => setToast(null), 3000)
  }

  const toggleExpanded = (id: number) => {
    setExpandedIds(prev => {
      const next = new Set(prev)
      next.has(id) ? next.delete(id) : next.add(id)
      return next
    })
  }

  const { data: menus = [], isLoading } = useQuery({
    queryKey: ['menus'],
    queryFn: menuApi.getAll,
  })

  const { data: modules = [] } = useQuery({
    queryKey: ['modules'],
    queryFn: moduleApi.getAll,
  })

  // Filter by module then build tree
  const filteredMenus = filterModuleId
    ? menus.filter(m => m.moduleId === Number(filterModuleId))
    : menus

  const tree = buildMenuTree(filteredMenus)

  // Parent menu options — only parent menus of same module
  const watchedModuleId = useForm<CreateMenuRequest>().watch

  const { register, handleSubmit, reset, watch, formState: { errors } } = useForm<CreateMenuRequest>({
    defaultValues: {
      menuName: '', moduleId: 0, isParent: 0,
      parentMenuId: null, sortOrder: '', routeName: '',
      isTopMenu: 0, isActive: 1,
    },
  })

  const selectedModuleId = watch('moduleId')
  const parentMenuOptions = menus.filter(
    m => Number(m.moduleId) === Number(selectedModuleId) && m.isParent === 1 && m.id !== editingMenu?.id
  )

  const createMutation = useMutation({
    mutationFn: menuApi.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['menus'] })
      closeModal()
      showToast('Menu created successfully!')
    },
    onError: (error: any) => {
      showToast(error?.response?.data?.message || 'Failed to create menu.', 'error')
    },
  })

  const updateMutation = useMutation({
    mutationFn: ({ id, payload }: { id: number; payload: CreateMenuRequest }) =>
      menuApi.update(id, { ...payload, id }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['menus'] })
      closeModal()
      showToast('Menu updated successfully!')
    },
    onError: (error: any) => {
      showToast(error?.response?.data?.message || 'Failed to update menu.', 'error')
    },
  })

  const deleteMutation = useMutation({
    mutationFn: menuApi.delete,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['menus'] })
      showToast('Menu deleted.')
    },
    onError: () => showToast('Failed to delete menu.', 'error'),
  })

  const onSubmit = (data: CreateMenuRequest) => {
    const payload: CreateMenuRequest = {
      ...data,
      moduleId: Number(data.moduleId),
      isParent: Number(data.isParent) as 0 | 1,
      isTopMenu: Number(data.isTopMenu) as 0 | 1,
      isActive: Number(data.isActive) as 0 | 1,
      parentMenuId: data.parentMenuId ? Number(data.parentMenuId) : null,
      sortOrder: data.sortOrder || null,
      routeName: data.routeName || null,
    }
    if (formMode === 'edit' && editingMenu) {
      updateMutation.mutate({ id: editingMenu.id, payload })
    } else {
      createMutation.mutate(payload)
    }
  }

  const openCreateModal = () => {
    setFormMode('create')
    setEditingMenu(null)
    reset({
      menuName: '', moduleId: 0, isParent: 0,
      parentMenuId: null, sortOrder: '', routeName: '',
      isTopMenu: 0, isActive: 1,
    })
    setModalOpen(true)
  }

  const openEditModal = (menu: AfmMenu) => {
    setFormMode('edit')
    setEditingMenu(menu)
    reset({
      menuName: menu.menuName,
      moduleId: menu.moduleId,
      isParent: menu.isParent,
      parentMenuId: menu.parentMenuId,
      sortOrder: menu.sortOrder ?? '',
      routeName: menu.routeName ?? '',
      isTopMenu: menu.isTopMenu,
      isActive: menu.isActive,
    })
    setModalOpen(true)
  }

  const closeModal = () => {
    setModalOpen(false)
    setEditingMenu(null)
    reset()
  }

  const isSubmitting = createMutation.isPending || updateMutation.isPending

  return (
    <div className="p-6">
      {toast && <Toast message={toast.message} type={toast.type} />}

      {/* Modal */}
      <Modal isOpen={modalOpen} onClose={closeModal} title={formMode === 'edit' ? 'Edit Menu' : 'New Menu'}>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">

          {/* Menu Name */}
          <div>
            <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wide mb-1.5">
              Menu Name <span className="text-red-400">*</span>
            </label>
            <input
              {...register('menuName', { required: 'Required' })}
              placeholder="e.g. Chart of Accounts"
              className={`w-full border rounded-lg px-3 py-2 text-sm outline-none transition-colors
                ${errors.menuName ? 'border-red-300 bg-red-50' : 'border-gray-200 focus:border-blue-400 focus:ring-2 focus:ring-blue-50'}`}
            />
            {errors.menuName && <p className="text-xs text-red-500 mt-1">{errors.menuName.message}</p>}
          </div>

          {/* Module */}
          <div>
            <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wide mb-1.5">
              Module <span className="text-red-400">*</span>
            </label>
            <select
              {...register('moduleId', { validate: v => Number(v) > 0 || 'Select a module' })}
              className={`w-full border rounded-lg px-3 py-2 text-sm outline-none bg-white transition-colors
                ${errors.moduleId ? 'border-red-300 bg-red-50' : 'border-gray-200 focus:border-blue-400 focus:ring-2 focus:ring-blue-50'}`}
            >
              <option value={0}>-- Select Module --</option>
              {modules.map(m => <option key={m.id} value={m.id}>{m.moduleName}</option>)}
            </select>
            {errors.moduleId && <p className="text-xs text-red-500 mt-1">{errors.moduleId.message}</p>}
          </div>

          {/* Parent Menu */}
          <div>
            <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wide mb-1.5">
              Parent Menu
            </label>
            <select
              {...register('parentMenuId')}
              className="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm outline-none bg-white focus:border-blue-400 focus:ring-2 focus:ring-blue-50"
            >
              <option value="">-- None (Top Level) --</option>
              {parentMenuOptions.map(m => <option key={m.id} value={m.id}>{m.menuName}</option>)}
            </select>
            {Number(selectedModuleId) === 0 && (
              <p className="text-xs text-gray-400 mt-1">Select a module first to see parent options</p>
            )}
          </div>

          {/* Route Name + Sort Order */}
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wide mb-1.5">
                Route Name
              </label>
              <input
                {...register('routeName')}
                placeholder="e.g. /accounting/coa"
                className="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm outline-none focus:border-blue-400 focus:ring-2 focus:ring-blue-50"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wide mb-1.5">
                Sort Order
              </label>
              <input
                {...register('sortOrder')}
                placeholder="e.g. 1"
                className="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm outline-none focus:border-blue-400 focus:ring-2 focus:ring-blue-50"
              />
            </div>
          </div>

          {/* Flags row */}
          <div className="grid grid-cols-3 gap-3">
            {/* Is Parent */}
            <div>
              <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wide mb-2">
                Is Parent
              </label>
              <div className="flex gap-2">
                {[{ label: 'Yes', value: 1 }, { label: 'No', value: 0 }].map(opt => (
                  <label key={opt.value} className="flex-1 flex items-center gap-1.5 border border-gray-200 rounded-lg px-2 py-1.5 cursor-pointer hover:border-blue-300 transition-colors has-[:checked]:border-blue-500 has-[:checked]:bg-blue-50">
                    <input type="radio" value={opt.value} {...register('isParent')} className="accent-blue-600" />
                    <span className="text-xs text-gray-700">{opt.label}</span>
                  </label>
                ))}
              </div>
            </div>

            {/* Is Top Menu */}
            <div>
              <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wide mb-2">
                Top Menu
              </label>
              <div className="flex gap-2">
                {[{ label: 'Yes', value: 1 }, { label: 'No', value: 0 }].map(opt => (
                  <label key={opt.value} className="flex-1 flex items-center gap-1.5 border border-gray-200 rounded-lg px-2 py-1.5 cursor-pointer hover:border-blue-300 transition-colors has-[:checked]:border-blue-500 has-[:checked]:bg-blue-50">
                    <input type="radio" value={opt.value} {...register('isTopMenu')} className="accent-blue-600" />
                    <span className="text-xs text-gray-700">{opt.label}</span>
                  </label>
                ))}
              </div>
            </div>

            {/* Is Active */}
            <div>
              <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wide mb-2">
                Status
              </label>
              <div className="flex gap-2">
                {[{ label: 'Active', value: 1 }, { label: 'Off', value: 0 }].map(opt => (
                  <label key={opt.value} className="flex-1 flex items-center gap-1.5 border border-gray-200 rounded-lg px-2 py-1.5 cursor-pointer hover:border-blue-300 transition-colors has-[:checked]:border-blue-500 has-[:checked]:bg-blue-50">
                    <input type="radio" value={opt.value} {...register('isActive')} className="accent-blue-600" />
                    <span className="text-xs text-gray-700">{opt.label}</span>
                  </label>
                ))}
              </div>
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
                onClick={() => { if (confirm('Delete this menu?')) { deleteMutation.mutate(editingMenu!.id); closeModal() } }}
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
          <h1 className="text-xl font-semibold text-gray-800">Menus</h1>
          <p className="text-sm text-gray-500 mt-0.5">Manage navigation menus</p>
        </div>
        <button
          onClick={openCreateModal}
          className="flex items-center gap-2 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium rounded-lg transition-colors"
        >
          <Plus size={15} />
          Add Menu
        </button>
      </div>

      {/* Module filter */}
      <div className="flex items-center gap-3 mb-4">
        <select
          value={filterModuleId}
          onChange={e => setFilterModuleId(e.target.value ? Number(e.target.value) : '')}
          className="border border-gray-200 rounded-lg px-3 py-2 text-sm outline-none focus:border-blue-400 focus:ring-2 focus:ring-blue-50 bg-white min-w-48"
        >
          <option value="">All Modules</option>
          {modules.map(m => <option key={m.id} value={m.id}>{m.moduleName}</option>)}
        </select>
        <span className="text-xs text-gray-400">{filteredMenus.length} menus</span>
      </div>

      {/* Table */}
      <div className="bg-white border border-gray-200 rounded-xl shadow-sm overflow-hidden">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-gray-100 bg-gray-50">
              <th className="text-left px-4 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wide">Menu Name</th>
              <th className="text-left px-4 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wide">Route</th>
              <th className="text-left px-4 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wide">Sort</th>
              <th className="text-left px-4 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wide">Type</th>
              <th className="text-left px-4 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wide">Status</th>
              <th className="text-right px-4 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wide">Actions</th>
            </tr>
          </thead>
          <tbody>
            {isLoading ? (
              <tr><td colSpan={6} className="text-center py-10 text-gray-400">Loading...</td></tr>
            ) : tree.length === 0 ? (
              <tr><td colSpan={6} className="text-center py-10 text-gray-400">No menus found</td></tr>
            ) : (
              tree.map(node => (
                <MenuTreeRow
                  key={node.id}
                  node={node}
                  depth={0}
                  onEdit={openEditModal}
                  onDelete={id => deleteMutation.mutate(id)}
                  expandedIds={expandedIds}
                  toggleExpanded={toggleExpanded}
                />
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  )
}