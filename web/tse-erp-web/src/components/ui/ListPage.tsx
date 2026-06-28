// src/components/ui/ListPage.tsx
// Universal list page system — used by Modules, Roles, Permissions, Menus, Users

import React, { useState, useRef, useEffect } from 'react'
import {
  Search, SlidersHorizontal, X, ChevronLeft, ChevronRight,
  ArrowUpDown, ArrowUp, ArrowDown, MoreHorizontal, Download,
  Eye, EyeOff
} from 'lucide-react'

// ─────────────────────────────────────────────
// Types
// ─────────────────────────────────────────────

export interface Column<T> {
  key: string
  label: string
  width?: string
  sortable?: boolean
  render: (row: T, index: number) => React.ReactNode
}

export interface FilterOption {
  key: string
  label: string
  options: { label: string; value: string }[]
}

export interface ListPageProps<T> {
  title: string
  description?: string
  data: T[]
  columns: Column<T>[]
  keyExtractor: (row: T) => string | number
  isLoading?: boolean
  primaryAction?: { label: string; onClick: () => void; icon?: React.ReactNode }
  filters?: FilterOption[]
  searchPlaceholder?: string
  searchKeys?: (keyof T)[]
  emptyIcon?: React.ReactNode
  emptyTitle?: string
  emptyDescription?: string
  onRowClick?: (row: T) => void
  rowsPerPageOptions?: number[]
}

// ─────────────────────────────────────────────
// Sub-components
// ─────────────────────────────────────────────

export function StatusBadge({ active }: { active: boolean }) {
  return (
    <span className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium
      ${active ? 'bg-emerald-50 text-emerald-700' : 'bg-gray-100 text-gray-500'}`}>
      <span className={`w-1.5 h-1.5 rounded-full flex-shrink-0 ${active ? 'bg-emerald-500' : 'bg-gray-400'}`} />
      {active ? 'Active' : 'Inactive'}
    </span>
  )
}

function SkeletonRows({ columns, rows = 8 }: { columns: number; rows?: number }) {
  return (
    <>
      {Array.from({ length: rows }).map((_, i) => (
        <tr key={i} style={{ opacity: 1 - i * 0.1 }}>
          {Array.from({ length: columns }).map((_, j) => (
            <td key={j} className="px-5 py-4">
              <div className="h-4 bg-gray-100 rounded-md animate-pulse"
                style={{ width: j === 0 ? '2rem' : j === 1 ? '60%' : '40%' }} />
            </td>
          ))}
        </tr>
      ))}
    </>
  )
}

function EmptyState({ icon, title, description }: { icon?: React.ReactNode; title: string; description?: string }) {
  return (
    <tr>
      <td colSpan={100}>
        <div className="flex flex-col items-center justify-center py-20 gap-3">
          {icon && (
            <div className="w-12 h-12 rounded-xl bg-gray-100 flex items-center justify-center text-gray-300">
              {icon}
            </div>
          )}
          <div className="text-center">
            <p className="text-sm font-medium text-gray-500">{title}</p>
            {description && <p className="text-xs text-gray-400 mt-1">{description}</p>}
          </div>
        </div>
      </td>
    </tr>
  )
}

function useOutsideClick(ref: React.RefObject<HTMLElement | null>, cb: () => void) {
  useEffect(() => {
    const handler = (e: MouseEvent) => {
      if (ref.current && !ref.current.contains(e.target as Node)) cb()
    }
    document.addEventListener('mousedown', handler)
    return () => document.removeEventListener('mousedown', handler)
  }, [cb])
}

// ─────────────────────────────────────────────
// Main ListPage component
// ─────────────────────────────────────────────

export function ListPage<T extends Record<string, any>>({
  title,
  description,
  data,
  columns,
  keyExtractor,
  isLoading,
  primaryAction,
  filters = [],
  searchPlaceholder = 'Search...',
  searchKeys = [],
  emptyIcon,
  emptyTitle = 'No results',
  emptyDescription,
  onRowClick,
  rowsPerPageOptions = [10, 25, 50],
}: ListPageProps<T>) {

  const [search, setSearch] = useState('')
  const [filterValues, setFilterValues] = useState<Record<string, string>>({})
  const [sortKey, setSortKey] = useState<string | null>(null)
  const [sortDir, setSortDir] = useState<'asc' | 'desc'>('asc')
  const [page, setPage] = useState(1)
  const [rowsPerPage, setRowsPerPage] = useState(rowsPerPageOptions[0])
  const [showFilters, setShowFilters] = useState(false)
  const [hiddenColumns, setHiddenColumns] = useState<Set<string>>(new Set())
  const [showColumnPanel, setShowColumnPanel] = useState(false)

  const filterRef = useRef<HTMLDivElement>(null)
  const columnRef = useRef<HTMLDivElement>(null)
  useOutsideClick(filterRef, () => setShowFilters(false))
  useOutsideClick(columnRef, () => setShowColumnPanel(false))

  // Reset page on filter/search change
  useEffect(() => { setPage(1) }, [search, filterValues, rowsPerPage])

  // Active filter count
  const activeFilterCount = Object.values(filterValues).filter(v => v && v !== 'all').length

  // Filter + search + sort
  const processed = React.useMemo(() => {
    let result = [...data]

    // Search
    if (search.trim() && searchKeys.length > 0) {
      const q = search.toLowerCase()
      result = result.filter(row =>
        searchKeys.some(k => String(row[k] ?? '').toLowerCase().includes(q))
      )
    }

    // Filters
    filters.forEach(f => {
      const val = filterValues[f.key]
      if (val && val !== 'all') {
        result = result.filter(row => String(row[f.key]) === val)
      }
    })

    // Sort
    if (sortKey) {
      result.sort((a, b) => {
        const av = String(a[sortKey] ?? '')
        const bv = String(b[sortKey] ?? '')
        return sortDir === 'asc' ? av.localeCompare(bv) : bv.localeCompare(av)
      })
    }

    return result
  }, [data, search, filterValues, sortKey, sortDir])

  const totalPages = Math.max(1, Math.ceil(processed.length / rowsPerPage))
  const paginated = processed.slice((page - 1) * rowsPerPage, page * rowsPerPage)
  const visibleColumns = columns.filter(c => !hiddenColumns.has(c.key))

  const handleSort = (key: string) => {
    if (sortKey === key) setSortDir(d => d === 'asc' ? 'desc' : 'asc')
    else { setSortKey(key); setSortDir('asc') }
  }

  const SortIcon = ({ colKey }: { colKey: string }) => {
    if (sortKey !== colKey) return <ArrowUpDown size={12} className="text-gray-300 group-hover:text-gray-400 transition-colors" />
    return sortDir === 'asc'
      ? <ArrowUp size={12} className="text-blue-500" />
      : <ArrowDown size={12} className="text-blue-500" />
  }

  // Pagination page numbers with ellipsis
  const pageNumbers = (() => {
    const pages: (number | 'ellipsis')[] = []
    for (let i = 1; i <= totalPages; i++) {
      if (i === 1 || i === totalPages || Math.abs(i - page) <= 1) pages.push(i)
      else if (pages[pages.length - 1] !== 'ellipsis') pages.push('ellipsis')
    }
    return pages
  })()

  return (
    <div className="flex flex-col h-full">
      <div className="flex-1 overflow-y-auto">
        <div className="max-w-screen-xl mx-auto px-6 py-8 space-y-6">

          {/* ── Header ── */}
          <div>
            <h1 className="text-[19px] font-semibold tracking-tight text-gray-900">{title}</h1>
            {description && <p className="text-sm text-gray-400 mt-1">{description}</p>}
          </div>

          {/* ── Main card ── */}
          <div className="bg-white border border-gray-200/80 rounded-xl shadow-sm overflow-hidden">

            {/* ── Toolbar ── */}
            <div className="flex items-center gap-3 px-4 py-3 border-b border-gray-100">

              {/* Search — 38% width */}
              <div className="w-[38%] flex items-center gap-2 bg-gray-50 border border-transparent rounded-lg px-3 py-2
                focus-within:bg-white focus-within:border-gray-300 focus-within:shadow-sm transition-all">
                <Search size={13} className="text-gray-400 flex-shrink-0" />
                <input
                  type="text"
                  placeholder={searchPlaceholder}
                  value={search}
                  onChange={e => setSearch(e.target.value)}
                  className="text-[13px] bg-transparent outline-none w-full text-gray-700 placeholder-gray-400"
                />
                {search && (
                  <button onClick={() => setSearch('')} className="text-gray-400 hover:text-gray-600 transition-colors">
                    <X size={12} />
                  </button>
                )}
              </div>

              {/* Right side tools */}
              <div className="flex items-center gap-1.5 ml-auto">

                {/* Filter */}
                {filters.length > 0 && (
                  <div className="relative" ref={filterRef}>
                    <button
                      onClick={() => setShowFilters(!showFilters)}
                      className={`flex items-center gap-1.5 px-3 py-2 rounded-lg text-[13px] font-medium transition-all
                        ${showFilters || activeFilterCount > 0
                          ? 'bg-blue-50 text-blue-600 border border-blue-200'
                          : 'text-gray-500 hover:bg-gray-100 border border-transparent'}`}
                    >
                      <SlidersHorizontal size={13} />
                      Filters
                      {activeFilterCount > 0 && (
                        <span className="w-4 h-4 bg-blue-600 text-white text-[10px] font-bold rounded-full flex items-center justify-center">
                          {activeFilterCount}
                        </span>
                      )}
                    </button>

                    {showFilters && (
                      <div className="absolute top-full right-0 mt-1.5 bg-white border border-gray-200 rounded-xl shadow-lg z-20 p-4 min-w-64">
                        <div className="flex items-center justify-between mb-3">
                          <span className="text-xs font-semibold text-gray-500 uppercase tracking-wide">Filters</span>
                          {activeFilterCount > 0 && (
                            <button onClick={() => setFilterValues({})}
                              className="text-xs text-red-500 hover:text-red-600 font-medium">Clear all</button>
                          )}
                        </div>
                        <div className="space-y-3">
                          {filters.map(f => (
                            <div key={f.key}>
                              <label className="block text-xs font-medium text-gray-500 mb-1.5">{f.label}</label>
                              <div className="flex flex-wrap gap-1">
                                {f.options.map(opt => (
                                  <button
                                    key={opt.value}
                                    onClick={() => setFilterValues(prev => ({
                                      ...prev,
                                      [f.key]: prev[f.key] === opt.value ? 'all' : opt.value
                                    }))}
                                    className={`px-2.5 py-1 text-xs rounded-lg border transition-all font-medium
                                      ${filterValues[f.key] === opt.value
                                        ? 'bg-blue-600 text-white border-blue-600'
                                        : 'bg-gray-50 text-gray-600 border-gray-200 hover:border-gray-300'}`}
                                  >
                                    {opt.label}
                                  </button>
                                ))}
                              </div>
                            </div>
                          ))}
                        </div>
                      </div>
                    )}
                  </div>
                )}

                {/* Sort (visual indicator) */}
                {sortKey && (
                  <button
                    onClick={() => { setSortKey(null); setSortDir('asc') }}
                    className="flex items-center gap-1.5 px-3 py-2 rounded-lg text-[13px] font-medium text-blue-600 bg-blue-50 border border-blue-200 transition-all"
                  >
                    <ArrowUpDown size={13} />
                    Sorted
                    <X size={11} />
                  </button>
                )}

                {/* Column visibility */}
                <div className="relative" ref={columnRef}>
                  <button
                    onClick={() => setShowColumnPanel(!showColumnPanel)}
                    className="flex items-center gap-1.5 px-3 py-2 rounded-lg text-[13px] font-medium text-gray-500 hover:bg-gray-100 border border-transparent transition-all"
                  >
                    {hiddenColumns.size > 0 ? <EyeOff size={13} /> : <Eye size={13} />}
                    Columns
                  </button>
                  {showColumnPanel && (
                    <div className="absolute top-full right-0 mt-1.5 bg-white border border-gray-200 rounded-xl shadow-lg z-20 p-3 min-w-44">
                      <p className="text-xs font-semibold text-gray-500 uppercase tracking-wide mb-2 px-1">Columns</p>
                      {columns.map(col => (
                        <label key={col.key} className="flex items-center gap-2.5 px-2 py-1.5 rounded-lg hover:bg-gray-50 cursor-pointer">
                          <input
                            type="checkbox"
                            checked={!hiddenColumns.has(col.key)}
                            onChange={() => setHiddenColumns(prev => {
                              const next = new Set(prev)
                              next.has(col.key) ? next.delete(col.key) : next.add(col.key)
                              return next
                            })}
                            className="accent-blue-600 w-3.5 h-3.5"
                          />
                          <span className="text-xs text-gray-600">{col.label}</span>
                        </label>
                      ))}
                    </div>
                  )}
                </div>

                {/* Export */}
                <button className="flex items-center gap-1.5 px-3 py-2 rounded-lg text-[13px] font-medium text-gray-500 hover:bg-gray-100 border border-transparent transition-all">
                  <Download size={13} />
                  Export
                </button>

                {/* Divider */}
                {primaryAction && <div className="w-px h-5 bg-gray-200" />}

                {/* Primary action */}
                {primaryAction && (
                  <button
                    onClick={primaryAction.onClick}
                    className="flex items-center gap-1.5 px-4 py-2 bg-gray-900 hover:bg-gray-800 active:bg-gray-950 text-white text-[13px] font-medium rounded-lg transition-all shadow-sm"
                  >
                    {primaryAction.icon}
                    {primaryAction.label}
                  </button>
                )}

                {/* Count */}
                {!isLoading && (
                  <span className="text-xs text-gray-400 whitespace-nowrap pl-1">
                    {processed.length.toLocaleString()}
                  </span>
                )}
              </div>
            </div>

            {/* ── Table ── */}
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead>
                  <tr className="border-b border-gray-100">
                    <th className="w-12 px-5 py-3 text-left">
                      <span className="text-[11px] font-semibold text-gray-400 uppercase tracking-wider">#</span>
                    </th>
                    {visibleColumns.map(col => (
                      <th key={col.key} className={`px-5 py-3 text-left ${col.width ?? ''}`}>
                        {col.sortable ? (
                          <button
                            onClick={() => handleSort(col.key)}
                            className="group flex items-center gap-1.5 text-[11px] font-semibold text-gray-400 uppercase tracking-wider hover:text-gray-600 transition-colors"
                          >
                            {col.label}
                            <SortIcon colKey={col.key} />
                          </button>
                        ) : (
                          <span className="text-[11px] font-semibold text-gray-400 uppercase tracking-wider">{col.label}</span>
                        )}
                      </th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {isLoading ? (
                    <SkeletonRows columns={visibleColumns.length + 1} rows={rowsPerPage} />
                  ) : paginated.length === 0 ? (
                    <EmptyState icon={emptyIcon} title={emptyTitle} description={emptyDescription} />
                  ) : (
                    paginated.map((row, index) => (
                      <tr
                        key={keyExtractor(row)}
                        onClick={() => onRowClick?.(row)}
                        className={`group border-b border-gray-50 last:border-0 transition-colors duration-150
                          ${onRowClick ? 'cursor-pointer' : ''}
                          hover:bg-gray-50/70`}
                      >
                        <td className="px-5 py-4">
                          <span className="text-[12px] text-gray-300 font-medium tabular-nums">
                            {String((page - 1) * rowsPerPage + index + 1).padStart(2, '0')}
                          </span>
                        </td>
                        {visibleColumns.map(col => (
                          <td key={col.key} className="px-5 py-4">
                            {col.render(row, index)}
                          </td>
                        ))}
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>

            {/* ── Pagination ── */}
            {!isLoading && processed.length > 0 && (
              <div className="flex items-center justify-between px-5 py-3.5 border-t border-gray-100">
                <div className="flex items-center gap-3">
                  <span className="text-xs text-gray-400">Rows per page</span>
                  <select
                    value={rowsPerPage}
                    onChange={e => { setRowsPerPage(Number(e.target.value)); setPage(1) }}
                    className="text-xs border border-gray-200 rounded-lg px-2 py-1.5 bg-white outline-none focus:border-gray-300 text-gray-600"
                  >
                    {rowsPerPageOptions.map(n => <option key={n} value={n}>{n}</option>)}
                  </select>
                  <span className="text-xs text-gray-400">
                    {(page - 1) * rowsPerPage + 1}–{Math.min(page * rowsPerPage, processed.length)} of {processed.length}
                  </span>
                </div>

                {totalPages > 1 && (
                  <div className="flex items-center gap-1">
                    <button
                      onClick={() => setPage(p => Math.max(1, p - 1))}
                      disabled={page === 1}
                      className="w-7 h-7 flex items-center justify-center rounded-md text-gray-500 hover:bg-gray-100 disabled:opacity-30 disabled:cursor-not-allowed transition-colors"
                    >
                      <ChevronLeft size={13} />
                    </button>
                    {pageNumbers.map((p, i) =>
                      p === 'ellipsis' ? (
                        <span key={`e${i}`} className="w-7 h-7 flex items-center justify-center text-xs text-gray-400">…</span>
                      ) : (
                        <button
                          key={p}
                          onClick={() => setPage(p)}
                          className={`w-7 h-7 flex items-center justify-center rounded-md text-[12px] font-medium transition-colors
                            ${page === p
                              ? 'bg-gray-900 text-white'
                              : 'text-gray-500 hover:bg-gray-100'}`}
                        >
                          {p}
                        </button>
                      )
                    )}
                    <button
                      onClick={() => setPage(p => Math.min(totalPages, p + 1))}
                      disabled={page === totalPages}
                      className="w-7 h-7 flex items-center justify-center rounded-md text-gray-500 hover:bg-gray-100 disabled:opacity-30 disabled:cursor-not-allowed transition-colors"
                    >
                      <ChevronRight size={13} />
                    </button>
                  </div>
                )}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}