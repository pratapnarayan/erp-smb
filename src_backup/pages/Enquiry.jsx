import React, { useMemo, useState } from 'react';
import FrostedCard from '../components/FrostedCard.jsx';
import DataTable from '../components/DataTable.jsx';
import Badge from '../components/Badge.jsx';

const columns = [
  { key: 'id', label: 'Enquiry #' },
  { key: 'customer', label: 'Customer' },
  { key: 'channel', label: 'Channel' },
  { key: 'subject', label: 'Subject' },
  { key: 'status', label: 'Status', render: (v) => <Badge color={v === 'Open' ? 'amber' : v === 'Closed' ? 'green' : 'gray'}>{v}</Badge> },
];

const initialData = [
  { id: 'ENQ-1001', customer: 'Acme Co', channel: 'Email', subject: 'Pricing question', status: 'Open' },
  { id: 'ENQ-1002', customer: 'Globex', channel: 'Phone', subject: 'Bulk order', status: 'Pending' },
  { id: 'ENQ-1003', customer: 'Initech', channel: 'Web', subject: 'Demo request', status: 'Closed' },
];

export default function Enquiry() {
  const [rows, setRows] = useState(initialData);
  const [form, setForm] = useState({ customer: '', channel: 'Email', subject: '', status: 'Open' });

  const addRow = (e) => {
    e.preventDefault();
    if (!form.customer || !form.subject) return;
    const id = `ENQ-${Math.floor(1000 + Math.random() * 9000)}`;
    setRows([{ id, ...form }, ...rows]);
    setForm({ customer: '', channel: 'Email', subject: '', status: 'Open' });
  };

  return (
    <div className="grid cols-1">
      <FrostedCard title="New Enquiry" subtitle="Capture incoming requests">
        <form className="form" onSubmit={addRow}>
          <div className="form-grid">
            <label>
              <span>Customer</span>
              <input value={form.customer} onChange={(e) => setForm({ ...form, customer: e.target.value })} placeholder="Customer name" />
            </label>
            <label>
              <span>Channel</span>
              <select value={form.channel} onChange={(e) => setForm({ ...form, channel: e.target.value })}>
                <option>Email</option>
                <option>Phone</option>
                <option>Web</option>
              </select>
            </label>
            <label className="span-2">
              <span>Subject</span>
              <input value={form.subject} onChange={(e) => setForm({ ...form, subject: e.target.value })} placeholder="Short summary" />
            </label>
            <label>
              <span>Status</span>
              <select value={form.status} onChange={(e) => setForm({ ...form, status: e.target.value })}>
                <option>Open</option>
                <option>Pending</option>
                <option>Closed</option>
              </select>
            </label>
          </div>
          <div className="form-actions">
            <button className="btn btn-primary" type="submit">Add Enquiry</button>
          </div>
        </form>
      </FrostedCard>

      <FrostedCard title="Enquiries" subtitle="Latest enquiries">
        <DataTable columns={columns} rows={rows} />
      </FrostedCard>
    </div>
  );
}
