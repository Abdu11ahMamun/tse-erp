// src/components/layout/AppLayout.tsx

import { useState } from 'react'
import { Outlet } from 'react-router-dom'
import Sidebar from './Sidebar'
import Topbar from './Topbar'

export default function AppLayout() {
  const [collapsed, setCollapsed] = useState(false)

  return (
    <div className="flex h-screen bg-gray-50 overflow-hidden">
      <Sidebar collapsed={collapsed} />

      <div className="flex flex-col flex-1 overflow-hidden">
        <Topbar onToggleSidebar={() => setCollapsed(!collapsed)} />

        <main className="flex-1 overflow-auto p-0">
          <Outlet />
        </main>
      </div>
    </div>
  )
}