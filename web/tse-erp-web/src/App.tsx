import { Routes, Route, Navigate } from 'react-router-dom'
import AppLayout from './components/layout/AppLayout'
import ChartOfAccountsPage from './pages/accounting/ChartOfAccountsPage'
import ModulePage from './pages/admin/ModulePage'
import RolePage from './pages/admin/RolePage'
import PermissionPage from './pages/admin/PermissionPage'
import RoleDetailPage from './pages/admin/RoleDetailPage'
import MenuPage from './pages/admin/MenuPage'


function App() {
  return (
    <Routes>
      <Route element={<AppLayout />}>
        <Route path="/" element={<Navigate to="/accounting/coa" replace />} />
        <Route path="/accounting/coa" element={<ChartOfAccountsPage />} />
        <Route path="/admin/modules" element={<ModulePage />} />
        <Route path="/admin/roles" element={<RolePage />} />
        <Route path="/admin/permissions" element={<PermissionPage />} />
        <Route path="/admin/role-details" element={<RoleDetailPage />} />
        <Route path="/admin/menus" element={<MenuPage />} />
      </Route>
    </Routes>
  )
}

export default App