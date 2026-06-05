// src/types/coa.types.ts

export interface AfmCoa {
  id: number;
  slNo: number | null;
  companyId: number;
  accountCode: string;
  accountHead: string;
  ledgerHeadType: 'Control Account' | 'Active Account';
  accountType: 'Asset' | 'Liability' | 'Income' | 'Expenditure';
  parentAccountHeadId: number | null;
  accountUsage: 'Ledger' | 'Cash' | 'Bank' | 'Inter Company' | 'Control Ledger';
  isCostCenterMandatory: 1 | 2;
  isBudgetHead: 1 | 2;
  openingDate: string | null;
  openingBalance: number | null;
  balanceDr: number | null;
  balanceCr: number | null;
  currentDr: number | null;
  currentCr: number | null;
  status: 0 | 1;
  createdBy: number | null;
  updatedBy: number | null;
  createdAt: string | null;
  updatedAt: string | null;
}

export interface CreateCoaRequest {
  companyId: number;
  accountCode: string;
  accountHead: string;
  ledgerHeadType: string;
  accountType: string;
  parentAccountHeadId?: number | null;
  accountUsage: string;
  isCostCenterMandatory?: number;
  isBudgetHead?: number;
  status: number;
}

export interface UpdateCoaRequest extends CreateCoaRequest {
  id: number;
}

// Tree node — UI-only, built from flat API list
export interface CoaTreeNode extends AfmCoa {
  children: CoaTreeNode[];
  level: number;
}