import { Routes, Route, Navigate } from 'react-router-dom'
import AppLayout from './components/layout/AppLayout'
import ChartOfAccountsPage from './pages/accounting/ChartOfAccountsPage'
import ModulePage from './pages/admin/ModulePage'
import RolePage from './pages/admin/RolePage'


function App() {
  return (
    <Routes>
      <Route element={<AppLayout />}>
        <Route path="/" element={<Navigate to="/accounting/coa" replace />} />
        <Route path="/accounting/coa" element={<ChartOfAccountsPage />} />
        <Route path="/admin/modules" element={<ModulePage />} />
        <Route path="/admin/roles" element={<RolePage />} />

      </Route>
    </Routes>
  )
}

export default App