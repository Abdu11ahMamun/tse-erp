// src/store/coaStore.ts

import { create } from 'zustand';
import type { AfmCoa } from '../types/coa.types';

interface CoaStore {
  // Selected node from tree (for edit or add-child)
  selectedCoa: AfmCoa | null;
  setSelectedCoa: (coa: AfmCoa | null) => void;

  // Form mode
  formMode: 'create' | 'edit';
  setFormMode: (mode: 'create' | 'edit') => void;

  // Expanded tree node ids
  expandedIds: Set<number>;
  toggleExpanded: (id: number) => void;
  setExpandedIds: (ids: Set<number>) => void;
}

export const useCoaStore = create<CoaStore>((set) => ({
  selectedCoa: null,
  setSelectedCoa: (coa) => set({ selectedCoa: coa }),

  formMode: 'create',
  setFormMode: (mode) => set({ formMode: mode }),

  expandedIds: new Set(),
  toggleExpanded: (id) =>
    set((state) => {
      const next = new Set(state.expandedIds);
      if (next.has(id)) {
        next.delete(id);
      } else {
        next.add(id);
      }
      return { expandedIds: next };
    }),
  setExpandedIds: (ids) => set({ expandedIds: ids }),
}));