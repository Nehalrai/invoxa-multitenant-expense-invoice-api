import { useState, useEffect } from 'react'
import API from '../api'

export default function Invoices() {
  const [invoices, setInvoices] = useState([])
  const [clients, setClients] = useState([])
  const [loading, setLoading] = useState(true)
  const [showForm, setShowForm] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')
  const [form, setForm] = useState({
    clientId: '',
    dueDate: '',
    notes: '',
    lineItems: [{ description: '', quantity: 1, unitPrice: '' }],
  })

  useEffect(() => {
    fetchData()
  }, [])

  async function fetchData() {
    try {
      const invRes = await API.get('/invoices')
      const clientRes = await API.get('/clients')
      setInvoices(invRes.data)
      setClients(clientRes.data)
    } catch (e) {
      console.error('Fetch error:', e)
    }
    setLoading(false)
  }

  function addLineItem() {
    setForm(prev => ({
      ...prev,
      lineItems: [...prev.lineItems, { description: '', quantity: 1, unitPrice: '' }],
    }))
  }

  function removeLineItem(index) {
    setForm(prev => ({
      ...prev,
      lineItems: prev.lineItems.filter((_, i) => i !== index),
    }))
  }

  function updateLineItem(index, field, value) {
    setForm(prev => ({
      ...prev,
      lineItems: prev.lineItems.map((item, i) =>
        i === index ? { ...item, [field]: value } : item
      ),
    }))
  }

  const total = form.lineItems.reduce((sum, item) => {
    return sum + (parseFloat(item.unitPrice) || 0) * (parseInt(item.quantity) || 0)
  }, 0)

  async function handleSubmit(e) {
    e.preventDefault()
    setSubmitting(true)
    setError('')
    try {
      await API.post('/invoices', {
        clientId: form.clientId,
        dueDate: form.dueDate,
        notes: form.notes,
        lineItems: form.lineItems.map(item => ({
          description: item.description,
          quantity: parseInt(item.quantity),
          unitPrice: parseFloat(item.unitPrice),
        })),
      })
      setForm({ clientId: '', dueDate: '', notes: '', lineItems: [{ description: '', quantity: 1, unitPrice: '' }] })
      setShowForm(false)
      fetchData()
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to create invoice')
    }
    setSubmitting(false)
  }

  async function handleSend(id) {
    try {
      const res = await API.patch(`/invoices/${id}/send`)
      if (res.data.stripePaymentLink) {
        window.open(res.data.stripePaymentLink, '_blank')
      }
      fetchData()
    } catch (e) {
      alert('Failed to send invoice')
    }
  }

  async function handleManualPay(id) {
    try {
      await API.patch(`/invoices/${id}/pay`)
      fetchData()
    } catch (e) {
      alert('Failed to mark as paid')
    }
  }

  if (loading) {
    return <div className="text-gray-400 text-center py-20">Loading...</div>
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Invoices</h1>
          <p className="text-gray-500 text-sm mt-1">{invoices.length} total</p>
        </div>
        <button
          onClick={() => setShowForm(!showForm)}
          className="bg-indigo-600 text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-indigo-700 transition-colors"
        >
          {showForm ? 'Cancel' : '+ New Invoice'}
        </button>
      </div>

      {showForm && (
        <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6 mb-6">
          <h2 className="text-base font-semibold text-gray-900 mb-4">Create Invoice</h2>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Client</label>
                <select
                  required
                  value={form.clientId}
                  onChange={(e) => setForm({ ...form, clientId: e.target.value })}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
                >
                  <option value="">Select client</option>
                  {clients.map(c => (
                    <option key={c.id} value={c.id}>{c.name}</option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Due Date</label>
                <input
                  type="date"
                  required
                  value={form.dueDate}
                  onChange={(e) => setForm({ ...form, dueDate: e.target.value })}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Notes</label>
                <input
                  type="text"
                  value={form.notes}
                  onChange={(e) => setForm({ ...form, notes: e.target.value })}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
                  placeholder="Optional"
                />
              </div>
            </div>

            <div>
              <div className="flex items-center justify-between mb-2">
                <label className="text-sm font-medium text-gray-700">Line Items</label>
                <button type="button" onClick={addLineItem} className="text-xs text-indigo-600 hover:text-indigo-800 font-medium">
                  + Add Item
                </button>
              </div>
              <div className="space-y-2">
                {form.lineItems.map((item, index) => (
                  <div key={index} className="grid grid-cols-12 gap-2 items-center">
                    <input
                      type="text"
                      required
                      placeholder="Description"
                      value={item.description}
                      onChange={(e) => updateLineItem(index, 'description', e.target.value)}
                      className="col-span-6 border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
                    />
                    <input
                      type="number"
                      required
                      min="1"
                      placeholder="Qty"
                      value={item.quantity}
                      onChange={(e) => updateLineItem(index, 'quantity', e.target.value)}
                      className="col-span-2 border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
                    />
                    <input
                      type="number"
                      required
                      step="0.01"
                      placeholder="Unit Price"
                      value={item.unitPrice}
                      onChange={(e) => updateLineItem(index, 'unitPrice', e.target.value)}
                      className="col-span-3 border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
                    />
                    {form.lineItems.length > 1 && (
                      <button
                        type="button"
                        onClick={() => removeLineItem(index)}
                        className="col-span-1 text-red-400 hover:text-red-600 text-lg font-bold"
                      >
                        x
                      </button>
                    )}
                  </div>
                ))}
              </div>
              <div className="mt-3 text-right">
                <span className="text-sm text-gray-500">Total: </span>
                <span className="text-lg font-bold text-indigo-600">${total.toFixed(2)}</span>
              </div>
            </div>

            {error && <p className="text-red-500 text-sm">{error}</p>}
            <button
              type="submit"
              disabled={submitting}
              className="bg-indigo-600 text-white px-6 py-2 rounded-lg text-sm font-medium hover:bg-indigo-700 disabled:opacity-50"
            >
              {submitting ? 'Creating...' : 'Create Invoice'}
            </button>
          </form>
        </div>
      )}

      <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        {invoices.length === 0 ? (
          <div className="text-center py-16 text-gray-400">
            <p className="text-lg">No invoices yet</p>
            <p className="text-sm mt-1">Click New Invoice to create one</p>
          </div>
        ) : (
          <table className="w-full">
            <thead className="bg-gray-50 border-b border-gray-100">
              <tr>
                {['Invoice', 'Client', 'Amount', 'Due Date', 'Status', 'Actions'].map(h => (
                  <th key={h} className="text-left text-xs font-medium text-gray-500 uppercase tracking-wider px-6 py-3">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50">
              {invoices.map(inv => (
                <tr key={inv.id} className="hover:bg-gray-50 transition-colors">
                  <td className="px-6 py-4 text-sm font-medium text-gray-900">{inv.invoiceNumber}</td>
                  <td className="px-6 py-4 text-sm text-gray-500">{inv.clientName}</td>
                  <td className="px-6 py-4 text-sm font-semibold text-gray-900">${inv.totalAmount}</td>
                  <td className="px-6 py-4 text-sm text-gray-500">{inv.dueDate}</td>
                  <td className="px-6 py-4">
                    <span className={`text-xs px-2 py-1 rounded-full font-medium ${
                      inv.status === 'PAID' ? 'bg-green-100 text-green-700' :
                      inv.status === 'SENT' ? 'bg-blue-100 text-blue-700' :
                      'bg-gray-100 text-gray-600'
                    }`}>
                      {inv.status}
                    </span>
                  </td>
                  <td className="px-6 py-4">
                    <div className="flex gap-2 flex-wrap">
                      {inv.status === 'DRAFT' && (
                        <button
                          onClick={() => handleSend(inv.id)}
                          className="text-xs bg-blue-100 text-blue-700 px-3 py-1 rounded-full hover:bg-blue-200 font-medium"
                        >
                          Send + Pay Link
                        </button>
                      )}
                      {inv.status === 'SENT' && inv.stripePaymentLink && (
                        <button
                          onClick={() => window.open(inv.stripePaymentLink, '_blank')}
                          className="text-xs bg-indigo-100 text-indigo-700 px-3 py-1 rounded-full hover:bg-indigo-200 font-medium"
                        >
                          View Pay Link
                        </button>
                      )}
                      {inv.status === 'SENT' && (
                        <button
                          onClick={() => handleManualPay(inv.id)}
                          className="text-xs bg-green-100 text-green-700 px-3 py-1 rounded-full hover:bg-green-200 font-medium"
                        >
                          Mark Paid
                        </button>
                      )}
                      {inv.status === 'PAID' && (
                        <span className="text-xs text-green-600 font-medium">Paid</span>
                      )}
                    </div>
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