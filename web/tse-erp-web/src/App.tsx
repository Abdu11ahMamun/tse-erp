import { Routes, Route, Navigate } from 'react-router-dom'
import ChartOfAccountsPage from './pages/accounting/ChartOfAccountsPage'

function App() {
  return (
    <div className="min-h-screen bg-gray-100">
      <Routes>
        <Route path="/" element={<Navigate to="/accounting/coa" replace />} />
        <Route path="/accounting/coa" element={<ChartOfAccountsPage />} />
      </Routes>
    </div>
  )
}

export default App