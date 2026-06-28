// src/components/ui/ListPage.tsx
// Universal list page system — used by Modules, Roles, Permissions, Menus, Users

import React, { useState, useRef, useEffect } from 'react'
import {
  Search,
  SlidersHorizontal,
  X,
  ChevronLeft,
  ChevronRight,
  ArrowUpDown,
  ArrowUp,
  ArrowDown,
  Download,
  Eye,
  EyeOff,
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
  headerIcon?: React.ReactNode
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
    <span
      className={`inline-flex items-center gap-1.5 rounded-full px-2.5 py-1 text-xs font-medium ${
        active
          ? 'bg-emerald-50 text-emerald-700 ring-1 ring-emerald-100'
          : 'bg-gray-100 text-gray-500 ring-1 ring-gray-200'
      }`}
    >
      <span
        className={`h-1.5 w-1.5 flex-shrink-0 rounded-full ${
          active ? 'bg-emerald-500' : 'bg-gray-400'
        }`}
      />
      {active ? 'Active' : 'Inactive'}
    </span>
  )
}

function SkeletonRows({ columns, rows = 8 }: { columns: number; rows?: number }) {
  return (
    <>
      {Array.from({ length: rows }).map((_, i) => (
        <tr key={i} style={{ opacity: 1 - i * 0.08 }}>
          {Array.from({ length: columns }).map((_, j) => (
            <td key={j} className="px-5 py-5">
              <div
                className="h-4 animate-pulse rounded-md bg-gray-100"
                style={{ width: j === 0 ? '2rem' : j === 1 ? '60%' : '40%' }}
              />
            </td>
          ))}
        </tr>
      ))}
    </>
  )
}

function EmptyState({
  icon,
  title,
  description,
}: {
  icon?: React.ReactNode
  title: string
  description?: string
}) {
  return (
    <tr>
      <td colSpan={100}>
        <div className="flex flex-col items-center justify-center gap-3 py-20">
          {icon && (
            <div className="flex h-12 w-12 items-center justify-center rounded-2xl border border-gray-200 bg-gray-50 text-gray-400">
              {icon}
            </div>
          )}

          <div className="text-center">
            <p className="text-sm font-semibold text-gray-700">{title}</p>
            {description && <p className="mt-1 text-xs text-gray-400">{description}</p>}
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
  }, [cb, ref])
}

// ─────────────────────────────────────────────
// Main ListPage component
// ─────────────────────────────────────────────

export function ListPage<T extends Record<string, any>>({
  title,
  description,
  headerIcon,
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

  useEffect(() => {
    setPage(1)
  }, [search, filterValues, rowsPerPage])

  const activeFilterCount = Object.values(filterValues).filter((v) => v && v !== 'all').length

  const processed = React.useMemo(() => {
    let result = [...data]

    if (search.trim() && searchKeys.length > 0) {
      const q = search.toLowerCase()
      result = result.filter((row) =>
        searchKeys.some((k) => String(row[k] ?? '').toLowerCase().includes(q)),
      )
    }

    filters.forEach((f) => {
      const val = filterValues[f.key]

      if (val && val !== 'all') {
        result = result.filter((row) => String(row[f.key]) === val)
      }
    })

    if (sortKey) {
      result.sort((a, b) => {
        const av = String(a[sortKey] ?? '')
        const bv = String(b[sortKey] ?? '')

        return sortDir === 'asc' ? av.localeCompare(bv) : bv.localeCompare(av)
      })
    }

    return result
  }, [data, search, searchKeys, filters, filterValues, sortKey, sortDir])

  const totalPages = Math.max(1, Math.ceil(processed.length / rowsPerPage))
  const paginated = processed.slice((page - 1) * rowsPerPage, page * rowsPerPage)
  const visibleColumns = columns.filter((c) => !hiddenColumns.has(c.key))

  const handleSort = (key: string) => {
    if (sortKey === key) {
      setSortDir((d) => (d === 'asc' ? 'desc' : 'asc'))
    } else {
      setSortKey(key)
      setSortDir('asc')
    }
  }

  const SortIcon = ({ colKey }: { colKey: string }) => {
    if (sortKey !== colKey) {
      return (
        <ArrowUpDown
          size={12}
          className="text-gray-300 transition-colors group-hover:text-gray-500"
        />
      )
    }

    return sortDir === 'asc' ? (
      <ArrowUp size={12} className="text-blue-500" />
    ) : (
      <ArrowDown size={12} className="text-blue-500" />
    )
  }

  const pageNumbers = (() => {
    const pages: (number | 'ellipsis')[] = []

    for (let i = 1; i <= totalPages; i++) {
      if (i === 1 || i === totalPages || Math.abs(i - page) <= 1) {
        pages.push(i)
      } else if (pages[pages.length - 1] !== 'ellipsis') {
        pages.push('ellipsis')
      }
    }

    return pages
  })()

  return (
    <div className="flex h-full flex-col bg-[#fafbfc]">
      <div className="flex-1 overflow-y-auto">
        <div className="mx-auto max-w-screen-xl px-6 py-8">

          {/* Header */}
          <div className="mb-7 flex items-start justify-between gap-6 border-b border-gray-200/70 pb-7">
            <div className="flex items-start gap-4">
              <div className="flex h-12 w-12 flex-shrink-0 items-center justify-center rounded-2xl border border-gray-200 bg-white text-gray-600 shadow-[0_1px_2px_rgba(16,24,40,0.05)]">
                {headerIcon ?? <div className="h-2.5 w-2.5 rounded-full bg-blue-600" />}
              </div>

              <div>
                <h1 className="text-[25px] font-semibold leading-tight tracking-[-0.035em] text-gray-950">
                  {title}
                </h1>

                {description && (
                  <p className="mt-1.5 max-w-2xl text-[14px] leading-6 text-gray-500">
                    {description}
                  </p>
                )}

                {!isLoading && (
                  <div className="mt-3 flex items-center gap-2 text-xs text-gray-400">
                    <span>{processed.length.toLocaleString()} records</span>
                    <span className="text-gray-300">•</span>
                    <span>{visibleColumns.length} visible columns</span>
                    {activeFilterCount > 0 && (
                      <>
                        <span className="text-gray-300">•</span>
                        <span>{activeFilterCount} active filter</span>
                      </>
                    )}
                  </div>
                )}
              </div>
            </div>
          </div>

          {/* Main card */}
          <div className="overflow-hidden rounded-2xl border border-gray-200/80 bg-white shadow-[0_1px_3px_rgba(16,24,40,0.06)]">

            {/* Toolbar */}
            <div className="flex items-center gap-3 border-b border-gray-100 px-4 py-3.5">
              <div className="flex w-full max-w-md items-center gap-2 rounded-xl border border-transparent bg-gray-50 px-3 py-2.5 transition-all focus-within:border-gray-300 focus-within:bg-white focus-within:shadow-sm">
                <Search size={14} className="flex-shrink-0 text-gray-400" />

                <input
                  type="text"
                  placeholder={searchPlaceholder}
                  value={search}
                  onChange={(e) => setSearch(e.target.value)}
                  className="w-full bg-transparent text-[13px] text-gray-700 outline-none placeholder:text-gray-400"
                />

                {search && (
                  <button
                    onClick={() => setSearch('')}
                    className="text-gray-400 transition-colors hover:text-gray-600"
                  >
                    <X size={13} />
                  </button>
                )}
              </div>

              <div className="ml-auto flex items-center gap-1.5">

                {/* Filter */}
                {filters.length > 0 && (
                  <div className="relative" ref={filterRef}>
                    <button
                      onClick={() => setShowFilters(!showFilters)}
                      className={`flex items-center gap-1.5 rounded-xl border px-3 py-2 text-[13px] font-medium transition-all ${
                        showFilters || activeFilterCount > 0
                          ? 'border-blue-200 bg-blue-50 text-blue-600'
                          : 'border-transparent text-gray-500 hover:bg-gray-100'
                      }`}
                    >
                      <SlidersHorizontal size={14} />
                      Filters

                      {activeFilterCount > 0 && (
                        <span className="flex h-4 w-4 items-center justify-center rounded-full bg-blue-600 text-[10px] font-bold text-white">
                          {activeFilterCount}
                        </span>
                      )}
                    </button>

                    {showFilters && (
                      <div className="absolute right-0 top-full z-20 mt-2 min-w-64 rounded-2xl border border-gray-200 bg-white p-4 shadow-xl">
                        <div className="mb-3 flex items-center justify-between">
                          <span className="text-xs font-semibold uppercase tracking-wide text-gray-500">
                            Filters
                          </span>

                          {activeFilterCount > 0 && (
                            <button
                              onClick={() => setFilterValues({})}
                              className="text-xs font-medium text-red-500 hover:text-red-600"
                            >
                              Clear all
                            </button>
                          )}
                        </div>

                        <div className="space-y-3">
                          {filters.map((f) => (
                            <div key={f.key}>
                              <label className="mb-1.5 block text-xs font-medium text-gray-500">
                                {f.label}
                              </label>

                              <div className="flex flex-wrap gap-1">
                                {f.options.map((opt) => (
                                  <button
                                    key={opt.value}
                                    onClick={() =>
                                      setFilterValues((prev) => ({
                                        ...prev,
                                        [f.key]: prev[f.key] === opt.value ? 'all' : opt.value,
                                      }))
                                    }
                                    className={`rounded-lg border px-2.5 py-1 text-xs font-medium transition-all ${
                                      filterValues[f.key] === opt.value
                                        ? 'border-blue-600 bg-blue-600 text-white'
                                        : 'border-gray-200 bg-gray-50 text-gray-600 hover:border-gray-300'
                                    }`}
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

                {/* Sort clear */}
                {sortKey && (
                  <button
                    onClick={() => {
                      setSortKey(null)
                      setSortDir('asc')
                    }}
                    className="flex items-center gap-1.5 rounded-xl border border-blue-200 bg-blue-50 px-3 py-2 text-[13px] font-medium text-blue-600 transition-all"
                  >
                    <ArrowUpDown size={14} />
                    Sorted
                    <X size={12} />
                  </button>
                )}

                {/* Column visibility */}
                <div className="relative" ref={columnRef}>
                  <button
                    onClick={() => setShowColumnPanel(!showColumnPanel)}
                    className="flex items-center gap-1.5 rounded-xl border border-transparent px-3 py-2 text-[13px] font-medium text-gray-500 transition-all hover:bg-gray-100"
                  >
                    {hiddenColumns.size > 0 ? <EyeOff size={14} /> : <Eye size={14} />}
                    Columns
                  </button>

                  {showColumnPanel && (
                    <div className="absolute right-0 top-full z-20 mt-2 min-w-48 rounded-2xl border border-gray-200 bg-white p-3 shadow-xl">
                      <p className="mb-2 px-1 text-xs font-semibold uppercase tracking-wide text-gray-500">
                        Columns
                      </p>

                      {columns.map((col) => (
                        <label
                          key={col.key}
                          className="flex cursor-pointer items-center gap-2.5 rounded-lg px-2 py-1.5 hover:bg-gray-50"
                        >
                          <input
                            type="checkbox"
                            checked={!hiddenColumns.has(col.key)}
                            onChange={() =>
                              setHiddenColumns((prev) => {
                                const next = new Set(prev)

                                next.has(col.key) ? next.delete(col.key) : next.add(col.key)

                                return next
                              })
                            }
                            className="h-3.5 w-3.5 accent-blue-600"
                          />

                          <span className="text-xs text-gray-600">{col.label || 'Actions'}</span>
                        </label>
                      ))}
                    </div>
                  )}
                </div>

                {/* Export */}
                <button className="flex items-center gap-1.5 rounded-xl border border-transparent px-3 py-2 text-[13px] font-medium text-gray-500 transition-all hover:bg-gray-100">
                  <Download size={14} />
                  Export
                </button>

                {primaryAction && <div className="h-5 w-px bg-gray-200" />}

                {/* Primary action */}
                {primaryAction && (
                  <button
                    onClick={primaryAction.onClick}
                    className="flex items-center gap-1.5 rounded-xl bg-blue-600 px-4 py-2.5 text-[13px] font-semibold text-white shadow-sm transition-all hover:bg-blue-700 active:bg-blue-800"
                  >
                    {primaryAction.icon}
                    {primaryAction.label}
                  </button>
                )}
              </div>
            </div>

            {/* Table */}
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead className="bg-white">
                  <tr className="border-b border-gray-100">
                    <th className="w-12 px-5 py-3.5 text-left">
                      <span className="text-[11px] font-semibold uppercase tracking-wider text-gray-400">
                        #
                      </span>
                    </th>

                    {visibleColumns.map((col) => (
                      <th key={col.key} className={`px-5 py-3.5 text-left ${col.width ?? ''}`}>
                        {col.sortable ? (
                          <button
                            onClick={() => handleSort(col.key)}
                            className="group flex items-center gap-1.5 text-[11px] font-semibold uppercase tracking-wider text-gray-400 transition-colors hover:text-gray-600"
                          >
                            {col.label}
                            <SortIcon colKey={col.key} />
                          </button>
                        ) : (
                          <span className="text-[11px] font-semibold uppercase tracking-wider text-gray-400">
                            {col.label}
                          </span>
                        )}
                      </th>
                    ))}
                  </tr>
                </thead>

                <tbody>
                  {isLoading ? (
                    <SkeletonRows columns={visibleColumns.length + 1} rows={rowsPerPage} />
                  ) : paginated.length === 0 ? (
                    <EmptyState
                      icon={emptyIcon}
                      title={emptyTitle}
                      description={emptyDescription}
                    />
                  ) : (
                    paginated.map((row, index) => (
                      <tr
                        key={keyExtractor(row)}
                        onClick={() => onRowClick?.(row)}
                        className={`group border-b border-gray-50 transition-colors duration-150 last:border-0 hover:bg-gray-50/80 ${
                          onRowClick ? 'cursor-pointer' : ''
                        }`}
                      >
                        <td className="px-5 py-5">
                          <span className="tabular-nums text-[12px] font-medium text-gray-300">
                            {String((page - 1) * rowsPerPage + index + 1).padStart(2, '0')}
                          </span>
                        </td>

                        {visibleColumns.map((col) => (
                          <td key={col.key} className="px-5 py-5">
                            {col.render(row, index)}
                          </td>
                        ))}
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>

            {/* Pagination */}
            {!isLoading && processed.length > 0 && (
              <div className="flex items-center justify-between border-t border-gray-100 px-5 py-4">
                <div className="flex items-center gap-3">
                  <span className="text-xs text-gray-400">Rows per page</span>

                  <select
                    value={rowsPerPage}
                    onChange={(e) => {
                      setRowsPerPage(Number(e.target.value))
                      setPage(1)
                    }}
                    className="rounded-lg border border-gray-200 bg-white px-2 py-1.5 text-xs text-gray-600 outline-none focus:border-gray-300"
                  >
                    {rowsPerPageOptions.map((n) => (
                      <option key={n} value={n}>
                        {n}
                      </option>
                    ))}
                  </select>

                  <span className="text-xs text-gray-400">
                    {(page - 1) * rowsPerPage + 1}–
                    {Math.min(page * rowsPerPage, processed.length)} of {processed.length}
                  </span>
                </div>

                {totalPages > 1 && (
                  <div className="flex items-center gap-1">
                    <button
                      onClick={() => setPage((p) => Math.max(1, p - 1))}
                      disabled={page === 1}
                      className="flex h-7 w-7 items-center justify-center rounded-lg text-gray-500 transition-colors hover:bg-gray-100 disabled:cursor-not-allowed disabled:opacity-30"
                    >
                      <ChevronLeft size={14} />
                    </button>

                    {pageNumbers.map((p, i) =>
                      p === 'ellipsis' ? (
                        <span
                          key={`e${i}`}
                          className="flex h-7 w-7 items-center justify-center text-xs text-gray-400"
                        >
                          …
                        </span>
                      ) : (
                        <button
                          key={p}
                          onClick={() => setPage(p)}
                                    className={`flex h-8 w-8 items-center justify-center rounded-lg text-[12px] font-semibold transition-all ${page === p
                                            ? 'bg-blue-600 text-white shadow-sm'
                                            : 'text-blue-600 hover:bg-blue-50 hover:text-blue-700'
                                        }`}
                        >
                          {p}
                        </button>
                      ),
                    )}

                    <button
                      onClick={() => setPage((p) => Math.min(totalPages, p + 1))}
                      disabled={page === totalPages}
                      className="flex h-8 w-8 items-center justify-center rounded-lg text-blue-600 transition-colors hover:bg-blue-50 hover:text-blue-700 disabled:cursor-not-allowed disabled:opacity-30">
                      <ChevronRight size={14} />
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