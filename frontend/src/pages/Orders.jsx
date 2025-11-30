import React, { useEffect, useState } from 'react';
import { ordersApi } from '../api/client.js';
import FrostedCard from '../components/FrostedCard.jsx';
import DataTable from '../components/DataTable.jsx';
import Badge from '../components/Badge.jsx';

const columns = [
  { key: 'id', label: 'Order #' },
  { key: 'customer', label: 'Customer' },
  { key: 'status', label: 'Status', render: (v) => <Badge color={v === 'Paid' ? 'green' : v === 'Pending' ? 'amber' : 'red'}>{v}</Badge> },
  { key: 'total', label: 'Total' },
  { key: 'date', label: 'Date' },
];

// replaced by API data
const initialData = [
  { id: 'SO-1051', customer: 'Acme Co', status: 'Paid', total: '₹1,240.00', date: '2025-11-03' },
  { id: 'SO-1050', customer: 'Globex', status: 'Pending', total: '₹520.00', date: '2025-11-02' },
  { id: 'SO-1049', customer: 'Initech', status: 'Overdue', total: '₹2,100.00', date: '2025-11-01' },
];

export default function Orders() {
  const [rows, setRows] = useState(initialData);
  useEffect(() => {
    ordersApi.list(0, 20).then((p) => {
      const mapped = p.content.map(o => ({ id: o.code, customer: o.customer, status: o.status, total: `₹${Number(o.total).toFixed(2)}`, date: o.orderDate }));
      setRows(mapped);
    }).catch(()=>{});
  }, []);
  const [form, setForm] = useState({ customer: '', status: 'Pending', total: '', date: '' });

  const addRow = async (e) => {
    e.preventDefault();
    if (!form.customer || !form.total || !form.date) return;
    const id = `SO-${Math.floor(1000 + Math.random() * 9000)}`; // kept for UI generation but backend stores code
    const payload = { code: id, customer: form.customer, status: form.status, total: Number(form.total), orderDate: form.date };
    try {
      await ordersApi.create(payload);
      const refreshed = await ordersApi.list(0, 20);
      const mapped = refreshed.content.map(o => ({ id: o.code, customer: o.customer, status: o.status, total: `₹${Number(o.total).toFixed(2)}`, date: o.orderDate }));
      setRows(mapped);
      setForm({ customer: '', status: 'Pending', total: '', date: '' });
    } catch(e) { /* noop */ }
  };

  return (
    <div className="grid cols-1">
      <FrostedCard title="New Order" subtitle="Create a sales order">
        <form className="form" onSubmit={addRow}>
          <div className="form-grid">
            <label>
              <span>Customer</span>
              <input value={form.customer} onChange={(e) => setForm({ ...form, customer: e.target.value })} placeholder="Customer name" />
            </label>
            <label>
              <span>Status</span>
              <select value={form.status} onChange={(e) => setForm({ ...form, status: e.target.value })}>
                <option>Pending</option>
                <option>Paid</option>
                <option>Overdue</option>
              </select>
            </label>
            <label>
              <span>Total</span>
              <input type="number" step="0.01" value={form.total} onChange={(e) => setForm({ ...form, total: e.target.value })} placeholder="0.00" />
            </label>
            <label>
              <span>Date</span>
              <input type="date" value={form.date} onChange={(e) => setForm({ ...form, date: e.target.value })} />
            </label>
          </div>
          <div className="form-actions">
            <button className="btn btn-primary" type="submit">Add Order</button>
          </div>
        </form>
      </FrostedCard>

      <FrostedCard title="Orders" subtitle="Recent sales orders">
        <DataTable columns={columns} rows={rows} />
      </FrostedCard>
    </div>
  );
}
