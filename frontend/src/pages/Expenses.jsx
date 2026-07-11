import { useState, useEffect } from 'react'
import API from '../api'
import { useAuth } from '../AuthContext'

export default function Expenses() {
  const { user } = useAuth()
  const [expenses, setExpenses] = useState([])
  const [loading, setLoading] = useState(true)
  const [showForm, setShowForm] = useState(false)
  const [form, setForm] = useState({ amount: '', category: '', description: '' })
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')

  const isAdmin = user?.role === 'ADMIN' || user?.role === 'ACCOUNTANT'

  const fetchExpenses = async () => {
    try {
      const res = await API.get(isAdmin ? '/expenses' : '/expenses/me')
      setExpenses(res.data)
    } catch (e) {
      console.error(e)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { fetchExpenses() }, [])

  const handleSubmit = async (e) => {
    e.preventDefault()
    setSubmitting(true)
    setError('')
    try {
      await API.post('/expenses', {
        amount: parseFloat(form.amount),
        category: form.category,
        description: form.description,
      })
      setForm({ amount: '', category: '', description: '' })
      setShowForm(false)
      fetchExpenses()
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to submit expense')
    } finally {
      setSubmitting(false)
    }
  }

  const handleAction = async (id, action) => {
    try {
      await API.patch(`/expenses/${id}/${action}`)
      fetchExpenses()
    } catch (e) {
      console.error(e)
    }
  }

  const statusStyle = (status) => {
    if (status === 'APPROVED') return 'bg-green-100 text-green-700'
    if (status === 'REJECTED') return 'bg-red-100 text-red-700'
    return 'bg-yellow-100 text-yellow-700'
  }

  if (loading) return <div className="text-gray-400 text-center py-20">Loading...</div>

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Expenses</h1>
          <p className="text-gray-500 text-sm mt-1">{expenses.length} total</p>
        </div>
        <button
          onClick={() => setShowForm(!showForm)}
          className="bg-indigo-600 text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-indigo-700 transition-colors"
        >
          {showForm ? 'Cancel' : '+ New Expense'}
        </button>
      </div>

      {showForm && (
        <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6 mb-6">
          <h2 className="text-base font-semibold text-gray-900 mb-4">Submit Expense</h2>
          <form onSubmit={handleSubmit} className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Amount ($)</label>
              <input
                type="number"
                step="0.01"
                required
                value={form.amount}
                onChange={(e) => setForm({ ...form, amount: e.target.value })}
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
                placeholder="49.99"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Category</label>
              <select
                required
                value={form.category}
                onChange={(e) => setForm({ ...form, category: e.target.value })}
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
              >
                <option value="">Select category</option>
                {['Travel', 'Food', 'Software', 'Hardware', 'Office', 'Other'].map(c => (
                  <option key={c} value={c}>{c}</option>
                ))}
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Description</label>
              <input
                type="text"
                value={form.description}
                onChange={(e) => setForm({ ...form, description: e.target.value })}
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
                placeholder="Optional"
              />
            </div>
            {error && <p className="text-red-500 text-sm col-span-3">{error}</p>}
            <div className="col-span-3">
              <button
                type="submit"
                disabled={submitting}
                className="bg-indigo-600 text-white px-6 py-2 rounded-lg text-sm font-medium hover:bg-indigo-700 disabled:opacity-50"
              >
                {submitting ? 'Submitting...' : 'Submit Expense'}
              </button>
            </div>
          </form>
        </div>
      )}

      <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        {expenses.length === 0 ? (
          <div className="text-center py-16 text-gray-400">
            <p className="text-lg">No expenses yet</p>
            <p className="text-sm mt-1">Click "+ New Expense" to submit one</p>
          </div>
        ) : (
          <table className="w-full">
            <thead className="bg-gray-50 border-b border-gray-100">
              <tr>
                {['Category', 'Description', 'Amount', 'Submitted By', 'Status', isAdmin ? 'Actions' : ''].map(h => (
                  <th key={h} className="text-left text-xs font-medium text-gray-500 uppercase tracking-wider px-6 py-3">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50">
              {expenses.map(e => (
                <tr key={e.id} className="hover:bg-gray-50 transition-colors">
                  <td className="px-6 py-4 text-sm font-medium text-gray-900">{e.category}</td>
                  <td className="px-6 py-4 text-sm text-gray-500">{e.description || '—'}</td>
                  <td className="px-6 py-4 text-sm font-semibold text-gray-900">${e.amount}</td>
                  <td className="px-6 py-4 text-sm text-gray-500">{e.submittedByEmail}</td>
                  <td className="px-6 py-4">
                    <span className={`text-xs px-2 py-1 rounded-full font-medium ${statusStyle(e.status)}`}>
                      {e.status}
                    </span>
                  </td>
                  {isAdmin && (
                    <td className="px-6 py-4">
                      {e.status === 'PENDING' && (
                        <div className="flex gap-2">
                          <button
                            onClick={() => handleAction(e.id, 'approve')}
                            className="text-xs bg-green-100 text-green-700 px-3 py-1 rounded-full hover:bg-green-200 font-medium transition-colors"
                          >
                            Approve
                          </button>
                          <button
                            onClick={() => handleAction(e.id, 'reject')}
                            className="text-xs bg-red-100 text-red-700 px-3 py-1 rounded-full hover:bg-red-200 font-medium transition-colors"
                          >
                            Reject
                          </button>
                        </div>
                      )}
                    </td>
                  )}
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  )
}