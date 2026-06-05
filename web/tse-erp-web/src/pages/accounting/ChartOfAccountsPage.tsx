// src/pages/accounting/ChartOfAccountsPage.tsx

import { useQuery } from '@tanstack/react-query';
import { coaApi } from '../../api/coaApi';
import CoaTree from '../../components/accounting/CoaTree';
import CoaForm from '../../components/accounting/CoaForm';

export default function ChartOfAccountsPage() {
  const { data: coaList = [], isLoading, isError } = useQuery({
    queryKey: ['coa'],
    queryFn: coaApi.getAll,
  });

  return (
    <div className="flex flex-col h-full">
      {/* Page header */}
      <div className="flex items-center justify-between px-6 py-3 border-b border-gray-200 bg-white">
        <h1 className="text-lg font-semibold text-gray-800">Chart of Account</h1>
        <button className="px-4 py-1.5 bg-sky-500 hover:bg-sky-600 text-white text-sm rounded transition-colors">
          View COA Report
        </button>
      </div>

      {/* Filter bar */}
      <div className="flex items-center gap-3 px-6 py-3 bg-white border-b border-gray-100">
        {/* Company — hardcoded until multi-company is implemented */}
        <div className="w-60">
          <select className="form-input text-sm" disabled>
            <option>Test Company Limited</option>
          </select>
        </div>

        {/* Account head search — future use */}
        <div className="w-72">
          <select className="form-input text-sm">
            <option value="">-- Select Account Head --</option>
            {coaList.map((c) => (
              <option key={c.id} value={c.id}>
                {c.accountCode} - {c.accountHead}
              </option>
            ))}
          </select>
        </div>
      </div>

      {/* Main content — tree + form */}
      {isError ? (
        <div className="flex items-center justify-center flex-1 text-red-500 text-sm">
          Failed to load Chart of Accounts. Is the backend running?
        </div>
      ) : (
        <div className="flex flex-1 overflow-hidden">
          {/* Left — Tree */}
          <div className="w-[55%] border-r border-gray-200 bg-white overflow-hidden p-4">
            <CoaTree data={coaList} isLoading={isLoading} />
          </div>

          {/* Right — Form */}
          <div className="w-[45%] bg-gray-50 overflow-hidden p-4">
            <CoaForm allCoa={coaList} />
          </div>
        </div>
      )}
    </div>
  );
}