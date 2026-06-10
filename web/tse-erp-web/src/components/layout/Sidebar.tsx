// src/components/layout/Sidebar.tsx

import { useState } from 'react'
import { NavLink } from 'react-router-dom'
import {
  LayoutDashboard,
  BookOpen,
  ShoppingCart,
  Package,
  Users,
  Building2,
  BarChart3,
  Settings,
  ChevronDown,
  ChevronRight,
  Landmark,
  Shield,
} from 'lucide-react'

interface NavItem {
  label: string
  icon: React.ReactNode
  path?: string
  children?: { label: string; path: string }[]
}

const NAV_ITEMS: NavItem[] = [
  {
    label: 'Dashboard',
    icon: <LayoutDashboard size={18} />,
    path: '/dashboard',
  },
  {
  label: 'Admin',
    icon: <Shield size={18} />,
    children: [
      { label: 'Modules', path: '/admin/modules' },
      { label: 'Roles', path: '/admin/roles' },
      { label: 'Permissions', path: '/admin/permissions' },
      { label: 'Role Details', path: '/admin/role-details' },
    ],
  },
  {
    label: 'Accounting',
    icon: <BookOpen size={18} />,
    children: [
      { label: 'Chart of Accounts', path: '/accounting/coa' },
      { label: 'Journal Entry', path: '/accounting/journal' },
      { label: 'Ledger', path: '/accounting/ledger' },
    ],
  },
  {
    label: 'Sales',
    icon: <ShoppingCart size={18} />,
    children: [
      { label: 'Orders', path: '/sales/orders' },
      { label: 'Invoices', path: '/sales/invoices' },
    ],
  },
  {
    label: 'Inventory',
    icon: <Package size={18} />,
    children: [
      { label: 'Products', path: '/inventory/products' },
      { label: 'Stock', path: '/inventory/stock' },
    ],
  },
  {
    label: 'Purchasing',
    icon: <Landmark size={18} />,
    children: [
      { label: 'Purchase Orders', path: '/purchasing/orders' },
      { label: 'Vendors', path: '/purchasing/vendors' },
    ],
  },
  {
    label: 'HR & Payroll',
    icon: <Users size={18} />,
    children: [
      { label: 'Employees', path: '/hr/employees' },
      { label: 'Payroll', path: '/hr/payroll' },
    ],
  },
  {
    label: 'Assets',
    icon: <Building2 size={18} />,
    children: [
      { label: 'Asset List', path: '/assets/list' },
    ],
  },
  {
    label: 'Reports',
    icon: <BarChart3 size={18} />,
    children: [
      { label: 'Financial Reports', path: '/reports/financial' },
    ],
  },
  {
    label: 'Settings',
    icon: <Settings size={18} />,
    path: '/settings',
  },
]

interface SidebarProps {
  collapsed: boolean
}

function NavGroup({ item, collapsed }: { item: NavItem; collapsed: boolean }) {
  const [open, setOpen] = useState(item.label === 'Accounting')

  if (!item.children) {
    return (
      <NavLink
        to={item.path!}
        className={({ isActive }) =>
          `flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm transition-colors
          ${isActive
            ? 'bg-blue-600 text-white font-medium'
            : 'text-gray-600 hover:bg-gray-100 hover:text-gray-900'
          }`
        }
      >
        <span className="flex-shrink-0">{item.icon}</span>
        {!collapsed && <span>{item.label}</span>}
      </NavLink>
    )
  }

  return (
    <div>
      <button
        onClick={() => setOpen(!open)}
        className="w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900 transition-colors"
      >
        <span className="flex-shrink-0">{item.icon}</span>
        {!collapsed && (
          <>
            <span className="flex-1 text-left">{item.label}</span>
            {open ? <ChevronDown size={14} /> : <ChevronRight size={14} />}
          </>
        )}
      </button>

      {open && !collapsed && (
        <div className="ml-4 mt-0.5 border-l border-gray-200 pl-3 space-y-0.5">
          {item.children.map((child) => (
            <NavLink
              key={child.path}
              to={child.path}
              className={({ isActive }) =>
                `block px-2 py-2 rounded-md text-sm transition-colors
                ${isActive
                  ? 'text-blue-600 font-medium bg-blue-50'
                  : 'text-gray-500 hover:text-gray-900 hover:bg-gray-50'
                }`
              }
            >
              {child.label}
            </NavLink>
          ))}
        </div>
      )}
    </div>
  )
}

export default function Sidebar({ collapsed }: SidebarProps) {
  return (
    <aside
      className={`flex flex-col h-screen bg-white border-r border-gray-200 transition-all duration-300 flex-shrink-0
        ${collapsed ? 'w-16' : 'w-64'}`}
    >
      {/* Logo */}
      <div className="flex items-center gap-3 px-4 py-4 border-b border-gray-200">
        <div className="w-8 h-8 bg-blue-600 rounded-lg flex items-center justify-center flex-shrink-0">
          <BarChart3 size={16} className="text-white" />
        </div>
        {!collapsed && (
          <div>
            <p className="text-sm font-bold text-gray-900 leading-none">TSE-ERP</p>
            <p className="text-xs text-gray-400 mt-0.5">Enterprise Suite</p>
          </div>
        )}
      </div>

      {/* Nav */}
      <nav className="flex-1 overflow-y-auto px-3 py-4 space-y-1">
        {!collapsed && (
          <p className="text-xs font-semibold text-gray-400 uppercase tracking-wider px-3 mb-2">
            Menu
          </p>
        )}
        {NAV_ITEMS.map((item) => (
          <NavGroup key={item.label} item={item} collapsed={collapsed} />
        ))}
      </nav>

      {/* Bottom */}
      {!collapsed && (
        <div className="px-4 py-4 border-t border-gray-200">
          <div className="bg-blue-50 rounded-lg p-3 text-center">
            <p className="text-xs font-semibold text-blue-700">TSE-ERP v1.0</p>
            <p className="text-xs text-blue-500 mt-0.5">Test Company Limited</p>
          </div>
        </div>
      )}
    </aside>
  )
}