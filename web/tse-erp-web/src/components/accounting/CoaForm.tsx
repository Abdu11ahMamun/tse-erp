// src/components/accounting/CoaForm.tsx

import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import type { AfmCoa, CreateCoaRequest } from '../../types/coa.types';
import { coaApi } from '../../api/coaApi';
import { useCoaStore } from '../../store/coaStore';

const HARDCODED_COMPANY_ID = 1; // TODO: replace with auth context when multi-company is ready

interface CoaFormProps {
  allCoa: AfmCoa[];
}

export default function CoaForm({ allCoa }: CoaFormProps) {
  const { selectedCoa, formMode, setSelectedCoa, setFormMode } = useCoaStore();
  const queryClient = useQueryClient();

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
  });

  const watchedAccountType = watch('accountType');

  // When a node is selected in tree → fill form for edit
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
      });
    }
  }, [selectedCoa, formMode, reset]);

  // Parent dropdown — filtered by same account type
  const parentOptions = allCoa.filter(
    (c) => c.accountType === watchedAccountType && c.id !== selectedCoa?.id
  );

  // Auto-fill Parent Account Code when parent is selected
  const watchedParentId = watch('parentAccountHeadId');
  const parentCoa = allCoa.find((c) => c.id === Number(watchedParentId));

  // ─── Mutations ───────────────────────────────────────────────────────────────

  const createMutation = useMutation({
    mutationFn: (data: CreateCoaRequest) => coaApi.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['coa'] });
      resetForm();
    },
  });

  const updateMutation = useMutation({
    mutationFn: (data: CreateCoaRequest) =>
      coaApi.update(selectedCoa!.id, { ...data, id: selectedCoa!.id }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['coa'] });
    },
  });

  const deleteMutation = useMutation({
    mutationFn: () => coaApi.delete(selectedCoa!.id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['coa'] });
      resetForm();
    },
  });

  // ─── Helpers ─────────────────────────────────────────────────────────────────

  const resetForm = () => {
    setSelectedCoa(null);
    setFormMode('create');
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
    });
  };

  const onSubmit = (data: CreateCoaRequest) => {
    const payload: CreateCoaRequest = {
      ...data,
      companyId: HARDCODED_COMPANY_ID,
      parentAccountHeadId: data.parentAccountHeadId ? Number(data.parentAccountHeadId) : null,
      isCostCenterMandatory: Number(data.isCostCenterMandatory) as 1 | 2,
      isBudgetHead: Number(data.isBudgetHead) as 1 | 2,
      status: Number(data.status) as 0 | 1,
    };

    if (formMode === 'edit') {
      updateMutation.mutate(payload);
    } else {
      createMutation.mutate(payload);
    }
  };

  const isSubmitting = createMutation.isPending || updateMutation.isPending;

  // ─── UI ───────────────────────────────────────────────────────────────────────

  return (
    <div className="bg-white border border-gray-200 rounded p-5 overflow-y-auto h-full">
      <div className="flex items-center justify-between mb-4">
        <h2 className="text-base font-semibold text-gray-800">
          {formMode === 'edit' ? 'Edit Account Head' : 'New Account Head'}
        </h2>
        {formMode === 'edit' && (
          <button
            type="button"
            onClick={resetForm}
            className="text-xs text-gray-500 hover:text-gray-700 underline"
          >
            + New
          </button>
        )}
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">

        {/* Business Unit — hardcoded for now */}
        <div>
          <label className="form-label">Business Unit <span className="text-red-500">*</span></label>
          <div className="form-input-readonly">Test Company Limited</div>
        </div>

        {/* Account Code */}
        <div>
          <label className="form-label">Account Code <span className="text-red-500">*</span></label>
          <input
            {...register('accountCode', { required: 'Account Code is required' })}
            placeholder="Enter Account Code"
            className="form-input"
          />
          {errors.accountCode && <p className="form-error">{errors.accountCode.message}</p>}
        </div>

        {/* Account Head */}
        <div>
          <label className="form-label">Account Head <span className="text-red-500">*</span></label>
          <input
            {...register('accountHead', { required: 'Account Head is required' })}
            placeholder="Enter Account Head"
            className="form-input"
          />
          {errors.accountHead && <p className="form-error">{errors.accountHead.message}</p>}
        </div>

        {/* Ledger Head Type */}
        <div>
          <label className="form-label">Ledger Head <span className="text-red-500">*</span></label>
          <div className="flex gap-4 mt-1">
            {(['Control Account', 'Active Account'] as const).map((opt) => (
              <label key={opt} className="radio-label">
                <input type="radio" value={opt} {...register('ledgerHeadType')} className="mr-1" />
                {opt}
              </label>
            ))}
          </div>
        </div>

        {/* Account Type */}
        <div>
          <label className="form-label">Account Type <span className="text-red-500">*</span></label>
          <div className="flex gap-4 mt-1 flex-wrap">
            {(['Asset', 'Expenditure', 'Income', 'Liability'] as const).map((opt) => (
              <label key={opt} className="radio-label">
                <input type="radio" value={opt} {...register('accountType')} className="mr-1" />
                {opt}
              </label>
            ))}
          </div>
        </div>

        {/* Parent Account Head */}
        <div>
          <label className="form-label">Parent Account Head</label>
          <select
            {...register('parentAccountHeadId')}
            className="form-input"
          >
            <option value="">-- Select Parent --</option>
            {parentOptions.map((c) => (
              <option key={c.id} value={c.id}>
                {c.accountCode} - {c.accountHead}
              </option>
            ))}
          </select>
        </div>

        {/* Parent Account Code — readonly, auto-filled */}
        <div>
          <label className="form-label">Parent Account Code</label>
          <div className="form-input-readonly">
            {parentCoa ? parentCoa.accountCode : 'Select Account Type First'}
          </div>
        </div>

        {/* Account Usage */}
        <div>
          <label className="form-label">Account Usage <span className="text-red-500">*</span></label>
          <div className="flex gap-4 mt-1 flex-wrap">
            {(['Bank', 'Cash', 'Ledger', 'Inter Company'] as const).map((opt) => (
              <label key={opt} className="radio-label">
                <input type="radio" value={opt} {...register('accountUsage')} className="mr-1" />
                {opt}
              </label>
            ))}
          </div>
        </div>

        {/* Cost Center Mandatory */}
        <div>
          <label className="form-label">Cost Center Mandatory <span className="text-red-500">*</span></label>
          <div className="flex gap-4 mt-1">
            <label className="radio-label">
              <input type="radio" value={1} {...register('isCostCenterMandatory')} className="mr-1" />
              Yes
            </label>
            <label className="radio-label">
              <input type="radio" value={2} {...register('isCostCenterMandatory')} className="mr-1" />
              No
            </label>
          </div>
        </div>

        {/* Is Budget Account Head */}
        <div>
          <label className="form-label">Is Budget Account Head <span className="text-red-500">*</span></label>
          <div className="flex gap-4 mt-1">
            <label className="radio-label">
              <input type="radio" value={1} {...register('isBudgetHead')} className="mr-1" />
              Yes
            </label>
            <label className="radio-label">
              <input type="radio" value={2} {...register('isBudgetHead')} className="mr-1" />
              No
            </label>
          </div>
        </div>

        {/* Status */}
        <div>
          <label className="form-label">Is Active <span className="text-red-500">*</span></label>
          <div className="flex gap-4 mt-1">
            <label className="radio-label">
              <input type="radio" value={1} {...register('status')} className="mr-1" />
              Yes
            </label>
            <label className="radio-label">
              <input type="radio" value={0} {...register('status')} className="mr-1" />
              No
            </label>
          </div>
        </div>

        {/* Actions */}
        <div className="flex gap-2 pt-2">
          <button
            type="submit"
            disabled={isSubmitting}
            className="btn-primary"
          >
            {isSubmitting ? 'Saving...' : formMode === 'edit' ? 'Update' : 'Submit'}
          </button>

          {formMode === 'edit' && (
            <button
              type="button"
              onClick={() => {
                if (confirm('Delete this account?')) deleteMutation.mutate();
              }}
              disabled={deleteMutation.isPending}
              className="btn-danger"
            >
              {deleteMutation.isPending ? 'Deleting...' : 'Delete'}
            </button>
          )}
        </div>

        {/* Error messages */}
        {createMutation.isError && (
          <p className="form-error">Failed to create. Check your input.</p>
        )}
        {updateMutation.isError && (
          <p className="form-error">Failed to update. Try again.</p>
        )}
        {deleteMutation.isError && (
          <p className="form-error">Failed to delete. Try again.</p>
        )}
      </form>
    </div>
  );
}