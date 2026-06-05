// src/components/layout/Topbar.tsx

import { Menu, Bell, Search, Moon, User } from 'lucide-react'

interface TopbarProps {
  onToggleSidebar: () => void
}

export default function Topbar({ onToggleSidebar }: TopbarProps) {
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

        {/* User */}
        <button className="flex items-center gap-2 pl-2 pr-3 py-1.5 rounded-lg hover:bg-gray-100 transition-colors">
          <div className="w-7 h-7 bg-blue-600 rounded-full flex items-center justify-center">
            <User size={14} className="text-white" />
          </div>
          <span className="text-sm font-medium text-gray-700">Admin</span>
        </button>
      </div>
    </header>
  )
}