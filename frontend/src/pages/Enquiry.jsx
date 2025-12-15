import React, { useCallback, useEffect, useMemo, useState } from 'react';
import FrostedCard from '../components/FrostedCard.jsx';
import DataTable from '../components/DataTable.jsx';
import Badge from '../components/Badge.jsx';

const toUiStatus = (status) => {
  const s = String(status || '').trim().toUpperCase();
  if (s === 'OPEN') return 'Open';
  if (s === 'PENDING' || s === 'IN_PROGRESS') return 'Pending';
  if (s === 'CLOSED' || s === 'RESOLVED') return 'Closed';
  return status || '';
};

const toApiStatus = (uiStatus) => {
  const s = String(uiStatus || '').trim().toLowerCase();
  if (s === 'open') return 'OPEN';
  if (s === 'pending') return 'IN_PROGRESS';
  if (s === 'closed') return 'CLOSED';
  return uiStatus;
};

const statusColor = (status) => {
  const s = String(status || '').trim().toLowerCase();
  if (s === 'open' || s === 'opened' || s === 'open'.toLowerCase()) return 'amber';
  if (s === 'closed' || s === 'resolved') return 'green';
  if (s === 'pending' || s === 'in_progress') return 'gray';
  return 'gray';
};

const canDelete = (status) => {
  const s = String(status || '').trim().toLowerCase();
  return s === 'closed' || s === 'resolved';
};

const initialData = [];

import http from '../api/clients/http.js';

export default function Enquiry() {
  const [rows, setRows] = useState(initialData);
  const [form, setForm] = useState({ customer: '', channel: 'Email', subject: '', status: 'Open' });

  const load = useCallback(async () => {
    const { data } = await http.get('/enquiry', { params: { page: 0, size: 50 } });
    setRows(data.content || []);
  }, []);

  const updateStatus = useCallback(async (id, uiStatus) => {
    const { data } = await http.put(`/enquiry/${id}/status`, { status: toApiStatus(uiStatus) });
    setRows((prev) => prev.map((r) => (r.id === id ? data : r)));
  }, []);

  const deleteEnquiry = useCallback(async (id) => {
    await http.delete(`/enquiry/${id}`);
    setRows((prev) => prev.filter((r) => r.id !== id));
  }, []);

  const columns = useMemo(() => [
    { key: 'id', label: 'Enquiry #' },
    { key: 'customer', label: 'Customer' },
    { key: 'channel', label: 'Channel' },
    { key: 'subject', label: 'Subject' },
    { key: 'status', label: 'Status', render: (v) => <Badge color={statusColor(v)}>{toUiStatus(v)}</Badge> },
    {
      key: 'actions',
      label: 'Actions',
      render: (_, row) => (
        <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
          <select
            value={toUiStatus(row.status) || 'Open'}
            onChange={(e) => updateStatus(row.id, e.target.value)}
            style={{ padding: '6px 8px', borderRadius: 8 }}
          >
            <option>Open</option>
            <option>Pending</option>
            <option>Closed</option>
          </select>
          <button
            className="btn"
            type="button"
            disabled={!canDelete(row.status)}
            onClick={() => deleteEnquiry(row.id)}
            title={canDelete(row.status) ? 'Delete enquiry' : 'Only Closed enquiries can be deleted'}
          >
            Delete
          </button>
        </div>
      ),
    },
  ], [deleteEnquiry, updateStatus]);

  useEffect(() => {
    (async () => {
      await load();
    })();
  }, [load]);

  const addRow = async (e) => {
    e.preventDefault();
    if (!form.customer || !form.subject) return;
    const payload = { code: `ENQ-${Math.floor(1000 + Math.random() * 9000)}`, ...form };
    const { data } = await http.post('/enquiry', payload);
    setRows([data, ...rows]);
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
