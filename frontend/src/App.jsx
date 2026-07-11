import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider, useAuth } from './AuthContext'
import AuthPage from './pages/AuthPage'
import Dashboard from './pages/Dashboard'
import Expenses from './pages/Expenses'
import Invoices from './pages/Invoices'
import Clients from './pages/Clients'
import Layout from './components/Layout'

function ProtectedRoute({ children }) {
  const { token } = useAuth()
  return token ? children : <Navigate to="/auth" />
}

function AppRoutes() {
  const { token } = useAuth()
  return (
    <Routes>
      <Route path="/auth" element={token ? <Navigate to="/" /> : <AuthPage />} />
      <Route path="/" element={<ProtectedRoute><Layout /></ProtectedRoute>}>
        <Route index element={<Dashboard />} />
        <Route path="expenses" element={<Expenses />} />
        <Route path="invoices" element={<Invoices />} />
        <Route path="clients" element={<Clients />} />
      </Route>
    </Routes>
  )
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <AppRoutes />
      </BrowserRouter>
    </AuthProvider>
  )
}