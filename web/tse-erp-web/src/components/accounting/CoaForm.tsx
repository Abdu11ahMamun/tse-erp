// src/components/accounting/CoaForm.tsx

import { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import type { AfmCoa, CreateCoaRequest } from '../../types/coa.types'
import { coaApi } from '../../api/coaApi'
import { useCoaStore } from '../../store/coaStore'

const HARDCODED_COMPANY_ID = 1

interface CoaFormProps {
  allCoa: AfmCoa[]
}

export default function CoaForm({ allCoa }: CoaFormProps) {
  const { selectedCoa, formMode, setSelectedCoa, setFormMode } = useCoaStore()
  const queryClient = useQueryClient()

  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' } | null>(null)

  const showToast = (message: string, type: 'success' | 'error' = 'success') => {
    setToast({ message, type })
    setTimeout(() => setToast(null), 3000)
  }

  const {
    register,
    handleSubmit,
    reset,
    watch,
    formState: { errors },
  } = useForm<CreateCoaRequest>({
    defaultValues: {
      companyId: HARDCODED_COMPANY_ID,
      accountCode: '',
      accountHead: '',
      ledgerHeadType: 'Control Account',
      accountType: 'Asset',
      parentAccountHeadId: null,
      accountUsage: 'Ledger',
      isCostCenterMandatory: 2,
      isBudgetHead: 2,
      status: 1,
    },
  })

  const watchedAccountType = watch('accountType')
  const watchedParentId = watch('parentAccountHeadId')

  useEffect(() => {
    if (formMode === 'edit' && selectedCoa) {
      reset({
        companyId: selectedCoa.companyId,
        accountCode: selectedCoa.accountCode,
        accountHead: selectedCoa.accountHead,
        ledgerHeadType: selectedCoa.ledgerHeadType,
        accountType: selectedCoa.accountType,
        parentAccountHeadId: selectedCoa.parentAccountHeadId,
        accountUsage: selectedCoa.accountUsage,
        isCostCenterMandatory: selectedCoa.isCostCenterMandatory,
        isBudgetHead: selectedCoa.isBudgetHead,
        status: selectedCoa.status,
      })
    }
  }, [selectedCoa, formMode, reset])

  const parentOptions = allCoa.filter(
    (c) => c.accountType === watchedAccountType && c.id !== selectedCoa?.id
  )

  const parentCoa = allCoa.find((c) => c.id === Number(watchedParentId))

  const createMutation = useMutation({
    mutationFn: (data: CreateCoaRequest) => coaApi.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['coa'] })
      resetForm()
      showToast('Account created successfully!')
    },
    onError: () => showToast('Failed to create account.', 'error'),
  })

  const updateMutation = useMutation({
    mutationFn: (data: CreateCoaRequest) =>
      coaApi.update(selectedCoa!.id, { ...data, id: selectedCoa!.id }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['coa'] })
      showToast('Account updated successfully!')
    },
    onError: () => showToast('Failed to update account.', 'error'),
  })

  const deleteMutation = useMutation({
    mutationFn: () => coaApi.delete(selectedCoa!.id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['coa'] })
      resetForm()
      showToast('Account deleted.')
    },
    onError: () => showToast('Failed to delete account.', 'error'),
  })

  const resetForm = () => {
    setSelectedCoa(null)
    setFormMode('create')
    reset({
      companyId: HARDCODED_COMPANY_ID,
      accountCode: '',
      accountHead: '',
      ledgerHeadType: 'Control Account',
      accountType: 'Asset',
      parentAccountHeadId: null,
      accountUsage: 'Ledger',
      isCostCenterMandatory: 2,
      isBudgetHead: 2,
      status: 1,
    })
  }

  const onSubmit = (data: CreateCoaRequest) => {
    const payload: CreateCoaRequest = {
      ...data,
      companyId: HARDCODED_COMPANY_ID,
      parentAccountHeadId: data.parentAccountHeadId ? Number(data.parentAccountHeadId) : null,
      isCostCenterMandatory: Number(data.isCostCenterMandatory) as 1 | 2,
      isBudgetHead: Number(data.isBudgetHead) as 1 | 2,
      status: Number(data.status) as 0 | 1,
    }
    if (formMode === 'edit') updateMutation.mutate(payload)
    else createMutation.mutate(payload)
  }

  const isSubmitting = createMutation.isPending || updateMutation.isPending

  return (
    <div className="h-full flex flex-col bg-white">

      {/* Toast */}
      {toast && (
        <div className={`fixed top-5 right-5 z-50 flex items-center gap-2.5 px-4 py-3 rounded-lg shadow-lg text-sm font-medium
          ${toast.type === 'success' ? 'bg-green-600 text-white' : 'bg-red-600 text-white'}`}>
          <span>{toast.type === 'success' ? '✓' : '✕'}</span>
          {toast.message}
        </div>
      )}

      {/* Form Header */}
      <div className="flex items-center justify-between px-5 py-3.5 border-b border-gray-100">
        <div className="flex items-center gap-2.5">
          <div className="w-1 h-5 bg-blue-600 rounded-full" />
          <h2 className="text-sm font-semibold text-gray-800">
            {formMode === 'edit' ? 'Edit Account Head' : 'New Account Head'}
          </h2>
        </div>
        {formMode === 'edit' && (
          <button
            type="button"
            onClick={resetForm}
            className="flex items-center gap-1 text-xs text-blue-600 hover:text-blue-700 font-medium"
          >
            <span className="text-base leading-none">+</span> New
          </button>
        )}
      </div>

      {/* Scrollable form body */}
      <div className="flex-1 overflow-y-auto px-5 py-4">
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">

          {/* Business Unit */}
          <div>
            <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wide mb-1.5">
              Business Unit <span className="text-red-400 normal-case">*</span>
            </label>
            <div className="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm text-gray-500 bg-gray-50">
              Test Company Limited
            </div>
          </div>

          {/* Account Code + Account Head side by side */}
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wide mb-1.5">
                Account Code <span className="text-red-400">*</span>
              </label>
              <input
                {...register('accountCode', { required: 'Required' })}
                placeholder="e.g. 100000"
                className={`w-full border rounded-lg px-3 py-2 text-sm outline-none transition-colors
                  ${errors.accountCode
                    ? 'border-red-300 bg-red-50 focus:border-red-400'
                    : 'border-gray-200 bg-white focus:border-blue-400 focus:ring-2 focus:ring-blue-50'
                  }`}
              />
              {errors.accountCode && (
                <p className="text-xs text-red-500 mt-1">{errors.accountCode.message}</p>
              )}
            </div>

            <div>
              <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wide mb-1.5">
                Account Head <span className="text-red-400">*</span>
              </label>
              <input
                {...register('accountHead', { required: 'Required' })}
                placeholder="e.g. ASSETS"
                className={`w-full border rounded-lg px-3 py-2 text-sm outline-none transition-colors
                  ${errors.accountHead
                    ? 'border-red-300 bg-red-50 focus:border-red-400'
                    : 'border-gray-200 bg-white focus:border-blue-400 focus:ring-2 focus:ring-blue-50'
                  }`}
              />
              {errors.accountHead && (
                <p className="text-xs text-red-500 mt-1">{errors.accountHead.message}</p>
              )}
            </div>
          </div>

          {/* Ledger Head Type */}
          <div>
            <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wide mb-2">
              Ledger Head <span className="text-red-400">*</span>
            </label>
            <div className="flex gap-2">
              {(['Control Account', 'Active Account'] as const).map((opt) => (
                <label
                  key={opt}
                  className="flex-1 flex items-center gap-2 border border-gray-200 rounded-lg px-3 py-2 cursor-pointer hover:border-blue-300 hover:bg-blue-50 transition-colors has-[:checked]:border-blue-500 has-[:checked]:bg-blue-50"
                >
                  <input type="radio" value={opt} {...register('ledgerHeadType')} className="accent-blue-600" />
                  <span className="text-sm text-gray-700">{opt}</span>
                </label>
              ))}
            </div>
          </div>

          {/* Account Type */}
          <div>
            <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wide mb-2">
              Account Type <span className="text-red-400">*</span>
            </label>
            <div className="grid grid-cols-2 gap-2">
              {(['Asset', 'Liability', 'Income', 'Expenditure'] as const).map((opt) => (
                <label
                  key={opt}
                  className="flex items-center gap-2 border border-gray-200 rounded-lg px-3 py-2 cursor-pointer hover:border-blue-300 hover:bg-blue-50 transition-colors has-[:checked]:border-blue-500 has-[:checked]:bg-blue-50"
                >
                  <input type="radio" value={opt} {...register('accountType')} className="accent-blue-600" />
                  <span className="text-sm text-gray-700">{opt}</span>
                </label>
              ))}
            </div>
          </div>

          {/* Parent Account Head */}
          <div>
            <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wide mb-1.5">
              Parent Account Head
            </label>
            <select
              {...register('parentAccountHeadId')}
              className="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm bg-white outline-none focus:border-blue-400 focus:ring-2 focus:ring-blue-50 transition-colors"
            >
              <option value="">-- Select Parent --</option>
              {parentOptions.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.accountCode} - {c.accountHead}
                </option>
              ))}
            </select>
          </div>

          {/* Parent Account Code — readonly */}
          <div>
            <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wide mb-1.5">
              Parent Account Code
            </label>
            <div className="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm bg-gray-50 text-gray-500">
              {parentCoa ? parentCoa.accountCode : 'Auto-filled from parent'}
            </div>
          </div>

          {/* Account Usage */}
          <div>
            <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wide mb-2">
              Account Usage <span className="text-red-400">*</span>
            </label>
            <div className="grid grid-cols-2 gap-2">
              {(['Bank', 'Cash', 'Ledger', 'Inter Company'] as const).map((opt) => (
                <label
                  key={opt}
                  className="flex items-center gap-2 border border-gray-200 rounded-lg px-3 py-2 cursor-pointer hover:border-blue-300 hover:bg-blue-50 transition-colors has-[:checked]:border-blue-500 has-[:checked]:bg-blue-50"
                >
                  <input type="radio" value={opt} {...register('accountUsage')} className="accent-blue-600" />
                  <span className="text-sm text-gray-700">{opt}</span>
                </label>
              ))}
            </div>
          </div>

          {/* Cost Center + Budget Head */}
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wide mb-2">
                Cost Center <span className="text-red-400">*</span>
              </label>
              <div className="flex gap-2">
                {[{ label: 'Yes', value: 1 }, { label: 'No', value: 2 }].map((opt) => (
                  <label
                    key={opt.value}
                    className="flex-1 flex items-center gap-2 border border-gray-200 rounded-lg px-3 py-2 cursor-pointer hover:border-blue-300 transition-colors has-[:checked]:border-blue-500 has-[:checked]:bg-blue-50"
                  >
                    <input type="radio" value={opt.value} {...register('isCostCenterMandatory')} className="accent-blue-600" />
                    <span className="text-sm text-gray-700">{opt.label}</span>
                  </label>
                ))}
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wide mb-2">
                Budget Head <span className="text-red-400">*</span>
              </label>
              <div className="flex gap-2">
                {[{ label: 'Yes', value: 1 }, { label: 'No', value: 2 }].map((opt) => (
                  <label
                    key={opt.value}
                    className="flex-1 flex items-center gap-2 border border-gray-200 rounded-lg px-3 py-2 cursor-pointer hover:border-blue-300 transition-colors has-[:checked]:border-blue-500 has-[:checked]:bg-blue-50"
                  >
                    <input type="radio" value={opt.value} {...register('isBudgetHead')} className="accent-blue-600" />
                    <span className="text-sm text-gray-700">{opt.label}</span>
                  </label>
                ))}
              </div>
            </div>
          </div>

          {/* Is Active */}
          <div>
            <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wide mb-2">
              Is Active <span className="text-red-400">*</span>
            </label>
            <div className="flex gap-2">
              {[{ label: 'Active', value: 1 }, { label: 'Inactive', value: 0 }].map((opt) => (
                <label
                  key={opt.value}
                  className="flex items-center gap-2 border border-gray-200 rounded-lg px-3 py-2 cursor-pointer hover:border-blue-300 transition-colors has-[:checked]:border-blue-500 has-[:checked]:bg-blue-50"
                >
                  <input type="radio" value={opt.value} {...register('status')} className="accent-blue-600" />
                  <span className="text-sm text-gray-700">{opt.label}</span>
                </label>
              ))}
            </div>
          </div>

          {/* Action buttons */}
          <div className="flex gap-2 pt-1 pb-2">
            <button
              type="submit"
              disabled={isSubmitting}
              className="flex-1 py-2.5 bg-blue-600 hover:bg-blue-700 disabled:opacity-50 text-white text-sm font-medium rounded-lg transition-colors"
            >
              {isSubmitting ? 'Saving...' : formMode === 'edit' ? 'Update' : 'Submit'}
            </button>

            {formMode === 'edit' && (
              <button
                type="button"
                onClick={() => { if (confirm('Delete this account?')) deleteMutation.mutate() }}
                disabled={deleteMutation.isPending}
                className="px-4 py-2.5 bg-red-50 hover:bg-red-100 disabled:opacity-50 text-red-600 text-sm font-medium rounded-lg border border-red-200 transition-colors"
              >
                {deleteMutation.isPending ? '...' : 'Delete'}
              </button>
            )}
          </div>

        </form>
      </div>
    </div>
  )
}