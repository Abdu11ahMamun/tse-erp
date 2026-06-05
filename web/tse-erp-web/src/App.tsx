import { Routes, Route, Navigate } from 'react-router-dom'
import AppLayout from './components/layout/AppLayout'
import ChartOfAccountsPage from './pages/accounting/ChartOfAccountsPage'

function App() {
  return (
    <Routes>
      <Route element={<AppLayout />}>
        <Route path="/" element={<Navigate to="/accounting/coa" replace />} />
        <Route path="/accounting/coa" element={<ChartOfAccountsPage />} />
      </Route>
    </Routes>
  )
}

export default App