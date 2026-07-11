import { useState, useEffect } from 'react'
import API from '../api'

export default function Clients() {
  const [clients, setClients] = useState([])
  const [loading, setLoading] = useState(true)
  const [showForm, setShowForm] = useState(false)
  const [form, setForm] = useState({ name: '', email: '', phone: '', address: '' })
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')

  const fetchClients = async () => {
    try {
      const res = await API.get('/clients')
      setClients(res.data)
    } catch (e) {
      console.error(e)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { fetchClients() }, [])

  const handleSubmit = async (e) => {
    e.preventDefault()
    setSubmitting(true)
    setError('')
    try {
      await API.post('/clients', form)
      setForm({ name: '', email: '', phone: '', address: '' })
      setShowForm(false)
      fetchClients()
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to create client')
    } finally {
      setSubmitting(false)
    }
  }

  if (loading) return <div className="text-gray-400 text-center py-20">Loading...</div>

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Clients</h1>
          <p className="text-gray-500 text-sm mt-1">{clients.length} total</p>
        </div>
        <button
          onClick={() => setShowForm(!showForm)}
          className="bg-indigo-600 text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-indigo-700 transition-colors"
        >
          {showForm ? 'Cancel' : '+ New Client'}
        </button>
      </div>

      {showForm && (
        <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6 mb-6">
          <h2 className="text-base font-semibold text-gray-900 mb-4">Add Client</h2>
          <form onSubmit={handleSubmit} className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Company Name *</label>
              <input
                type="text"
                required
                value={form.name}
                onChange={(e) => setForm({ ...form, name: e.target.value })}
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
                placeholder="TechCorp Ltd"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
              <input
                type="email"
                value={form.email}
                onChange={(e) => setForm({ ...form, email: e.target.value })}
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
                placeholder="billing@techcorp.com"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Phone</label>
              <input
                type="text"
                value={form.phone}
                onChange={(e) => setForm({ ...form, phone: e.target.value })}
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
                placeholder="+91 9876543210"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Address</label>
              <input
                type="text"
                value={form.address}
                onChange={(e) => setForm({ ...form, address: e.target.value })}
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
                placeholder="123 Main St, Mumbai"
              />
            </div>
            {error && <p className="text-red-500 text-sm col-span-2">{error}</p>}
            <div className="col-span-2">
              <button
                type="submit"
                disabled={submitting}
                className="bg-indigo-600 text-white px-6 py-2 rounded-lg text-sm font-medium hover:bg-indigo-700 disabled:opacity-50"
              >
                {submitting ? 'Creating...' : 'Create Client'}
              </button>
            </div>
          </form>
        </div>
      )}

      <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        {clients.length === 0 ? (
          <div className="text-center py-16 text-gray-400">
            <p className="text-lg">No clients yet</p>
            <p className="text-sm mt-1">Click "+ New Client" to add one</p>
          </div>
        ) : (
          <table className="w-full">
            <thead className="bg-gray-50 border-b border-gray-100">
              <tr>
                {['Name', 'Email', 'Phone', 'Address', 'Created'].map(h => (
                  <th key={h} className="text-left text-xs font-medium text-gray-500 uppercase tracking-wider px-6 py-3">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50">
              {clients.map(c => (
                <tr key={c.id} className="hover:bg-gray-50 transition-colors">
                  <td className="px-6 py-4 text-sm font-medium text-gray-900">{c.name}</td>
                  <td className="px-6 py-4 text-sm text-gray-500">{c.email || '—'}</td>
                  <td className="px-6 py-4 text-sm text-gray-500">{c.phone || '—'}</td>
                  <td className="px-6 py-4 text-sm text-gray-500">{c.address || '—'}</td>
                  <td className="px-6 py-4 text-sm text-gray-400">
                    {new Date(c.createdAt).toLocaleDateString()}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  )
}