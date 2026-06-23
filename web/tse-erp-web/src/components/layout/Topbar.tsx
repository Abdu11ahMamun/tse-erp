// src/components/layout/Topbar.tsx

import { Menu, Bell, Search, Moon, User, LogOut } from 'lucide-react'
import { useAuth } from '../../hooks/useAuth'

interface TopbarProps {
  onToggleSidebar: () => void
}

export default function Topbar({ onToggleSidebar }: TopbarProps) {
  const { logout, user } = useAuth()

  return (
    <header className="h-14 bg-white border-b border-gray-200 flex items-center justify-between px-4 flex-shrink-0">
      {/* Left */}
      <div className="flex items-center gap-3">
        <button
          onClick={onToggleSidebar}
          className="p-1.5 rounded-lg text-gray-500 hover:bg-gray-100 transition-colors"
        >
          <Menu size={20} />
        </button>

        {/* Search */}
        <div className="flex items-center gap-2 bg-gray-100 rounded-lg px-3 py-1.5 w-72">
          <Search size={15} className="text-gray-400" />
          <input
            type="text"
            placeholder="Search or type command..."
            className="bg-transparent text-sm text-gray-600 placeholder-gray-400 outline-none w-full"
          />
          <span className="text-xs text-gray-400 bg-gray-200 px-1.5 py-0.5 rounded font-mono">⌘K</span>
        </div>
      </div>

      {/* Right */}
      <div className="flex items-center gap-2">
        <button className="p-1.5 rounded-lg text-gray-500 hover:bg-gray-100 transition-colors">
          <Moon size={18} />
        </button>

        <button className="relative p-1.5 rounded-lg text-gray-500 hover:bg-gray-100 transition-colors">
          <Bell size={18} />
          <span className="absolute top-1 right-1 w-2 h-2 bg-orange-400 rounded-full"></span>
        </button>

        {/* User + Logout */}
        <div className="flex items-center gap-1">
          <div className="flex items-center gap-2 pl-2 pr-3 py-1.5 rounded-lg">
            <div className="w-7 h-7 bg-blue-600 rounded-full flex items-center justify-center">
              <User size={14} className="text-white" />
            </div>
            <div className="text-left">
              <p className="text-sm font-medium text-gray-700 leading-none">{user?.username ?? 'Admin'}</p>
              {user?.role && (
                <p className="text-xs text-gray-400 mt-0.5 leading-none">{user.role}</p>
              )}
            </div>
          </div>

          <button
            onClick={logout}
            className="p-1.5 rounded-lg text-gray-400 hover:text-red-500 hover:bg-red-50 transition-colors"
            title="Logout"
          >
            <LogOut size={16} />
          </button>
        </div>
      </div>
    </header>
  )
}