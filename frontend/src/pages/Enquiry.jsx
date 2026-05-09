import React, { useCallback, useEffect, useMemo, useState } from 'react';
import FrostedCard from '../components/FrostedCard.jsx';
import DataTable from '../components/DataTable.jsx';
import Badge from '../components/Badge.jsx';
import http from '../api/clients/http.js';

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
  if (s === 'open') return 'amber';
  if (s === 'closed' || s === 'resolved') return 'green';
  if (s === 'pending' || s === 'in_progress') return 'gray';
  return 'gray';
};

const canDelete = (status) => {
  const s = String(status || '').trim().toLowerCase();
  return s === 'closed' || s === 'resolved';
};

export default function Enquiry() {
  const [rows, setRows] = useState([]);
  const [form, setForm] = useState({ customer: '', channel: 'Email', subject: '', description: '', status: 'Open' });
  const [formErrors, setFormErrors] = useState({});
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(50);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);
  const [confirmDelete, setConfirmDelete] = useState(null); // holds { id, subject }

  const load = useCallback(async (p, s) => {
    const { data } = await http.get('/enquiry', { params: { page: p, size: s } });
    const nextTotalPages = data?.totalPages ?? 0;
    const normalizedTotalPages = nextTotalPages > 0 ? nextTotalPages : 1;
    if (nextTotalPages > 0 && p >= nextTotalPages) { setPage(nextTotalPages - 1); return; }
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
    setConfirmDelete(null);
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
            className="btn btn-danger-outline"
            type="button"
            disabled={!canDelete(row.status)}
            onClick={() => setConfirmDelete({ id: row.id, subject: row.subject })}
            title={canDelete(row.status) ? 'Delete enquiry' : 'Only Closed enquiries can be deleted'}
          >
            Delete
          </button>
        </div>
      ),
    },
  ], [deleteEnquiry, updateStatus]);

  useEffect(() => { (async () => { await load(page, size); })(); }, [load, page, size]);

  const validate = () => {
    const errors = {};
    if (!form.customer.trim()) errors.customer = 'Customer name is required';
    if (!form.subject.trim()) errors.subject = 'Subject is required';
    return errors;
  };

  const addRow = async (e) => {
    e.preventDefault();
    const errors = validate();
    if (Object.keys(errors).length > 0) { setFormErrors(errors); return; }
    setFormErrors({});
    const payload = {
      code: `ENQ-${Math.floor(1000 + Math.random() * 9000)}`,
      customer: form.customer.trim(),
      channel: form.channel,
      subject: form.subject.trim(),
      description: form.description.trim(),
      status: form.status,
    };
    await http.post('/enquiry', payload);
    setForm({ customer: '', channel: 'Email', subject: '', description: '', status: 'Open' });
    setPage(0);
    await load(0, size);
  };

  return (
    <div className="grid cols-1">
      {/* Delete confirmation dialog */}
      {confirmDelete && (
        <div className="dialog-overlay" role="dialog" aria-modal="true">
          <div className="dialog">
            <h3 className="dialog-title">Delete Enquiry</h3>
            <p className="dialog-body">
              Are you sure you want to delete enquiry <strong>#{confirmDelete.id}</strong>
              {confirmDelete.subject ? ` — "${confirmDelete.subject}"` : ''}? This action cannot be undone.
            </p>
            <div className="dialog-actions">
              <button className="btn" onClick={() => setConfirmDelete(null)}>Cancel</button>
              <button className="btn btn-danger" onClick={() => deleteEnquiry(confirmDelete.id)}>Delete</button>
            </div>
          </div>
        </div>
      )}

      <FrostedCard title="New Enquiry" subtitle="Capture incoming requests">
        <form className="form" onSubmit={addRow}>
          <div className="form-grid">
            <label>
              <span>Customer <span className="required">*</span></span>
              <input
                value={form.customer}
                onChange={(e) => setForm({ ...form, customer: e.target.value })}
                placeholder="Customer name"
                className={formErrors.customer ? 'input-error' : ''}
              />
              {formErrors.customer && <span className="field-error">{formErrors.customer}</span>}
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
              <span>Subject <span className="required">*</span></span>
              <input
                value={form.subject}
                onChange={(e) => setForm({ ...form, subject: e.target.value })}
                placeholder="Short summary"
                className={formErrors.subject ? 'input-error' : ''}
              />
              {formErrors.subject && <span className="field-error">{formErrors.subject}</span>}
            </label>
            <label className="span-4">
              <span>Description / Notes</span>
              <textarea
                value={form.description}
                onChange={(e) => setForm({ ...form, description: e.target.value })}
                placeholder="Full enquiry context, requirements, or follow-up notes..."
                rows={3}
                style={{ resize: 'vertical' }}
              />
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
            <button className="btn" type="button" disabled={page <= 0} onClick={() => setPage((p) => Math.max(0, p - 1))}>Prev</button>
            <div style={{ minWidth: 110, textAlign: 'center' }}>Page {page + 1} / {totalPages}</div>
            <button className="btn" type="button" disabled={page >= totalPages - 1} onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}>Next</button>
            <select value={size} onChange={(e) => { setSize(Number(e.target.value)); setPage(0); }} style={{ padding: '6px 8px', borderRadius: 8 }}>
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
