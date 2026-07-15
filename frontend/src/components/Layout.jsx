import { Outlet, NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../AuthContext'

export default function Layout() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/auth')
  }

  const navLinks = [
    { to: '/', label: 'Dashboard' },
    { to: '/expenses', label: 'Expenses' },
    ...(user?.role === 'ADMIN' || user?.role === 'ACCOUNTANT' ? [
      { to: '/invoices', label: 'Invoices' },
      { to: '/clients', label: 'Clients' },
    ] : []),
    ...(user?.role === 'ADMIN' ? [
      { to: '/team', label: 'Team' },
    ] : []),
  ]

  return (
    <div className="min-h-screen bg-gray-50">
      <nav className="bg-white border-b border-gray-200 px-6 py-4 flex items-center justify-between">
        <div className="flex items-center gap-8">
          <span className="text-xl font-bold text-indigo-600">Invoxa</span>
          <div className="flex gap-6">
            {navLinks.map(({ to, label }) => (
              <NavLink
                key={to}
                to={to}
                end={to === '/'}
                className={({ isActive }) =>
                  `text-sm font-medium transition-colors ${
                    isActive
                      ? 'text-indigo-600 border-b-2 border-indigo-600 pb-1'
                      : 'text-gray-500 hover:text-gray-900'
                  }`
                }
              >
                {label}
              </NavLink>
            ))}
          </div>
        </div>
        <div className="flex items-center gap-4">
          <span className="text-sm text-gray-500">
            {user?.sub} · <span className="font-medium text-indigo-600">{user?.role}</span>
          </span>
          <button
            onClick={handleLogout}
            className="text-sm text-gray-500 hover:text-red-600 transition-colors"
          >
            Logout
          </button>
        </div>
      </nav>
      <main className="max-w-6xl mx-auto px-6 py-8">
        <Outlet />
      </main>
    </div>
  )
}