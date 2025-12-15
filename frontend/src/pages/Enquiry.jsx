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
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(50);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);

  const load = useCallback(async (p, s) => {
    const { data } = await http.get('/enquiry', { params: { page: p, size: s } });
    const nextTotalPages = data?.totalPages ?? 0;
    const normalizedTotalPages = nextTotalPages > 0 ? nextTotalPages : 1;
    if (nextTotalPages > 0 && p >= nextTotalPages) {
      setPage(nextTotalPages - 1);
      return;
    }
    setRows(data?.content || []);
    setTotalPages(normalizedTotalPages);
    setTotalElements(data?.totalElements ?? 0);
  }, []);

  const updateStatus = useCallback(async (id, uiStatus) => {
    const { data } = await http.put(`/enquiry/${id}/status`, { status: toApiStatus(uiStatus) });
    setRows((prev) => prev.map((r) => (r.id === id ? data : r)));
  }, []);

  const deleteEnquiry = useCallback(async (id) => {
    await http.delete(`/enquiry/${id}`);
    await load(page, size);
  }, [load, page, size]);

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
      await load(page, size);
    })();
  }, [load, page, size]);

  const addRow = async (e) => {
    e.preventDefault();
    if (!form.customer || !form.subject) return;
    const payload = { code: `ENQ-${Math.floor(1000 + Math.random() * 9000)}`, ...form };
    await http.post('/enquiry', payload);
    setForm({ customer: '', channel: 'Email', subject: '', status: 'Open' });
    setPage(0);
    await load(0, size);
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

      <FrostedCard
        title="Enquiries"
        subtitle={`Latest enquiries (${totalElements} records)`}
        actions={
          <div style={{ display: 'flex', gap: 8, alignItems: 'center', flexWrap: 'wrap' }}>
            <button className="btn" type="button" disabled={page <= 0} onClick={() => setPage((p) => Math.max(0, p - 1))}>
              Prev
            </button>
            <div style={{ minWidth: 110, textAlign: 'center' }}>
              Page {page + 1} / {totalPages}
            </div>
            <button className="btn" type="button" disabled={page >= totalPages - 1} onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}>
              Next
            </button>
            <select
              value={size}
              onChange={(e) => {
                const nextSize = Number(e.target.value);
                setSize(nextSize);
                setPage(0);
              }}
              style={{ padding: '6px 8px', borderRadius: 8 }}
            >
              <option value={10}>10 / page</option>
              <option value={20}>20 / page</option>
              <option value={50}>50 / page</option>
              <option value={100}>100 / page</option>
            </select>
          </div>
        }
      >
        <DataTable columns={columns} rows={rows} />
      </FrostedCard>
    </div>
  );
}
