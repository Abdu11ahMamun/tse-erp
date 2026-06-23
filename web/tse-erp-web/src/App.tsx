import { Routes, Route, Navigate } from 'react-router-dom'
import AppLayout from './components/layout/AppLayout'
import ProtectedRoute from './components/auth/ProtectedRoute'
import LoginPage from './components/auth/LoginPage'
import ChartOfAccountsPage from './pages/accounting/ChartOfAccountsPage'
import ModulePage from './pages/admin/ModulePage'
import RolePage from './pages/admin/RolePage'
import PermissionPage from './pages/admin/PermissionPage'
import RoleDetailPage from './pages/admin/RoleDetailPage'
import MenuPage from './pages/admin/MenuPage'
import UserPage from './pages/admin/UserPage'
import DashboardPage from './pages/DashboardPage'



function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />

      <Route element={<ProtectedRoute />}>
        <Route element={<AppLayout />}>
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/accounting/coa" element={<ChartOfAccountsPage />} />
          <Route path="/admin/modules" element={<ModulePage />} />
          <Route path="/admin/roles" element={<RolePage />} />
          <Route path="/admin/permissions" element={<PermissionPage />} />
          <Route path="/admin/role-details" element={<RoleDetailPage />} />
          <Route path="/admin/menus" element={<MenuPage />} />
          <Route path="/admin/users" element={<UserPage />} />
        </Route>
      </Route>

      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  )
}

export default App