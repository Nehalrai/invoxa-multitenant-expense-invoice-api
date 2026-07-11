import { useState, useEffect } from 'react'
import { useAuth } from '../AuthContext'
import API from '../api'

function StatCard({ title, value, color, subtitle }) {
  return (
    <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
      <p className="text-sm text-gray-500 font-medium">{title}</p>
      <p className={`text-3xl font-bold mt-2 ${color}`}>{value}</p>
      {subtitle && <p className="text-xs text-gray-400 mt-1">{subtitle}</p>}
    </div>
  )
}

export default function Dashboard() {
  const { user } = useAuth()
  const [expenses, setExpenses] = useState([])
  const [invoices, setInvoices] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    Promise.all([
      API.get('/expenses'),
      API.get('/invoices'),
    ]).then(([expRes, invRes]) => {
      setExpenses(expRes.data)
      setInvoices(invRes.data)
    }).catch(() => {}).finally(() => setLoading(false))
  }, [])

  const pending = expenses.filter(e => e.status === 'PENDING').length
  const approved = expenses.filter(e => e.status === 'APPROVED').length
  const unpaidInvoices = invoices.filter(i => i.status !== 'PAID').length
  const totalRevenue = invoices
    .filter(i => i.status === 'PAID')
    .reduce((sum, i) => sum + i.totalAmount, 0)

  if (loading) return <div className="text-gray-400 text-center py-20">Loading...</div>

  return (
    <div>
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-gray-900">
          Welcome back 👋
        </h1>
        <p className="text-gray-500 mt-1">{user?.sub} · {user?.role}</p>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-10">
        <StatCard title="Pending Expenses" value={pending} color="text-yellow-500" subtitle="Awaiting approval" />
        <StatCard title="Approved Expenses" value={approved} color="text-green-500" subtitle="Ready for reimbursement" />
        <StatCard title="Unpaid Invoices" value={unpaidInvoices} color="text-red-500" subtitle="Action required" />
        <StatCard title="Total Revenue" value={`$${totalRevenue.toFixed(2)}`} color="text-indigo-600" subtitle="From paid invoices" />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
          <h2 className="text-base font-semibold text-gray-900 mb-4">Recent Expenses</h2>
          {expenses.length === 0 ? (
            <p className="text-gray-400 text-sm">No expenses yet</p>
          ) : (
            <div className="space-y-3">
              {expenses.slice(0, 5).map(e => (
                <div key={e.id} className="flex items-center justify-between">
                  <div>
                    <p className="text-sm font-medium text-gray-800">{e.category}</p>
                    <p className="text-xs text-gray-400">{e.description || '—'}</p>
                  </div>
                  <div className="text-right">
                    <p className="text-sm font-semibold text-gray-900">${e.amount}</p>
                    <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${
                      e.status === 'APPROVED' ? 'bg-green-100 text-green-700' :
                      e.status === 'REJECTED' ? 'bg-red-100 text-red-700' :
                      'bg-yellow-100 text-yellow-700'
                    }`}>{e.status}</span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
          <h2 className="text-base font-semibold text-gray-900 mb-4">Recent Invoices</h2>
          {invoices.length === 0 ? (
            <p className="text-gray-400 text-sm">No invoices yet</p>
          ) : (
            <div className="space-y-3">
              {invoices.slice(0, 5).map(i => (
                <div key={i.id} className="flex items-center justify-between">
                  <div>
                    <p className="text-sm font-medium text-gray-800">{i.invoiceNumber}</p>
                    <p className="text-xs text-gray-400">{i.clientName}</p>
                  </div>
                  <div className="text-right">
                    <p className="text-sm font-semibold text-gray-900">${i.totalAmount}</p>
                    <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${
                      i.status === 'PAID' ? 'bg-green-100 text-green-700' :
                      i.status === 'SENT' ? 'bg-blue-100 text-blue-700' :
                      'bg-gray-100 text-gray-600'
                    }`}>{i.status}</span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}