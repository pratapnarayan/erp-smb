import React, { useEffect, useState } from 'react';
import FrostedCard from '../components/FrostedCard.jsx';
import DataTable from '../components/DataTable.jsx';
import Badge from '../components/Badge.jsx';

import http from '../api/clients/http.js';

const columns = [
  { key: 'code', label: 'Order #' },
  { key: 'customer', label: 'Customer' },
  { key: 'status', label: 'Status', render: (v) => <Badge color={v === 'Paid' ? 'green' : v === 'Pending' ? 'amber' : 'red'}>{v}</Badge> },
  { key: 'total', label: 'Total' },
  { key: 'orderDate', label: 'Date' },
];

const initialData = [];

export default function Orders() {
  const [rows, setRows] = useState(initialData);
  const [form, setForm] = useState({ customer: '', status: 'Pending', total: '', date: '' });

  useEffect(() => {
    (async () => {
      const { data } = await http.get('/orders', { params: { page: 0, size: 50 } });
      setRows(data.content || []);
    })();
  }, []);

  const addRow = async (e) => {
    e.preventDefault();
    if (!form.customer || !form.total || !form.date) return;
    const payload = {
      code: `SO-${Math.floor(1000 + Math.random() * 9000)}`,
      customer: form.customer,
      status: form.status,
      total: Number(form.total),
      orderDate: form.date,
    };
    const { data } = await http.post('/orders', payload);
    setRows([data, ...rows]);
    setForm({ customer: '', status: 'Pending', total: '', date: '' });
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
