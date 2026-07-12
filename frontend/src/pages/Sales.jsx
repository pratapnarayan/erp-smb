import React, { useCallback, useEffect, useState } from 'react';
import { formatINR } from '../utils/formatCurrency.js';
import FrostedCard from '../components/FrostedCard.jsx';
import DataTable from '../components/DataTable.jsx';
import Badge from '../components/Badge.jsx';
import http from '../api/clients/http.js';

const STATUSES = ['OPEN', 'PAID', 'OVERDUE', 'DRAFT'];

const statusColor = (v) => {
  const s = String(v || '').toLowerCase();
  if (s === 'paid') return 'green';
  if (s === 'open' || s === 'draft') return 'amber';
  return 'red';
};

// Backend may return invoice number under different field names
const getInvoiceNo = (row) =>
  row.invoiceNo || row.invoiceNumber || row.invoice || row.code || '-';

const columns = [
  { key: 'invoiceNo', label: 'Invoice #', render: (v, row) => getInvoiceNo(row) },
  { key: 'customer',  label: 'Customer' },
  { key: 'status',    label: 'Status', render: (v) => <Badge color={statusColor(v)}>{v}</Badge> },
  { key: 'due',       label: 'Due Date' },
  { key: 'amount',    label: 'Amount', render: (v) => formatINR(v) },
];

const EMPTY_FORM = { customer: '', status: 'OPEN', due: '', amount: '' };

export default function Sales() {
  const [rows, setRows]             = useState([]);
  const [loading, setLoading]       = useState(true);
  const [loadError, setLoadError]   = useState('');
  const [showForm, setShowForm]     = useState(false);
  const [form, setForm]             = useState(EMPTY_FORM);
  const [formErrors, setFormErrors] = useState({});
  const [saving, setSaving]         = useState(false);
  const [saveError, setSaveError]   = useState('');
  const [saveSuccess, setSaveSuccess] = useState('');

  const load = useCallback(async () => {
    setLoading(true);
    setLoadError('');
    try {
      const { data } = await http.get('/sales', { params: { page: 0, size: 50 } });
      setRows(data.content || []);
    } catch (err) {
      console.error('Failed to load invoices', err);
      setLoadError('Failed to load invoices. Please refresh.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { load(); }, [load]);

  // ── Form validation ────────────────────────────────────────────────────────
  const validate = () => {
    const errors = {};
    if (!form.customer.trim()) errors.customer = 'Customer is required';
    if (!form.due) errors.due = 'Due date is required';
    const amt = parseFloat(form.amount);
    if (!form.amount) {
      errors.amount = 'Amount is required';
    } else if (isNaN(amt) || amt <= 0) {
      errors.amount = 'Amount must be a positive number';
    }
    if (!form.status) errors.status = 'Status is required';
    return errors;
  };

  // ── Submit: POST /api/sales ────────────────────────────────────────────────
  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaveError('');
    setSaveSuccess('');

    const errors = validate();
    if (Object.keys(errors).length > 0) { setFormErrors(errors); return; }
    setFormErrors({});
    setSaving(true);

    // Generate a lightweight invoice number client-side (timestamp suffix).
    // The backend enforces uniqueness and will reject duplicates with a clear error.
    const invoiceNo = `INV-${String(Date.now()).slice(-6)}`;

    try {
      const { data: created } = await http.post('/sales', {
        invoiceNo,
        customer: form.customer.trim(),
        status:   form.status,
        due:      form.due,
        amount:   parseFloat(form.amount),
      });
      setRows((prev) => [created, ...prev]);
      setForm(EMPTY_FORM);
      setShowForm(false);
      setSaveSuccess(`Invoice ${getInvoiceNo(created)} created successfully.`);
      setTimeout(() => setSaveSuccess(''), 5000);
    } catch (err) {
      const code = err?.response?.data?.error;
      if (code === 'invoice_no_already_exists') {
        setSaveError('Invoice number collision — please try again.');
      } else if (err?.response?.status === 403) {
        setSaveError('Permission denied. You do not have access to create invoices.');
      } else {
        setSaveError('Failed to create invoice. Please try again.');
      }
    } finally {
      setSaving(false);
    }
  };

  return (
    <div>
      <FrostedCard
        title="Sales Invoices"
        subtitle="All invoices across customers"
        actions={
          <div style={{ display: 'flex', gap: 8 }}>
            <button className="btn" onClick={load} disabled={loading}>
              {loading ? 'Loading...' : 'Refresh'}
            </button>
            <button
              className={showForm ? 'btn btn-secondary' : 'btn btn-primary'}
              onClick={() => { setShowForm((v) => !v); setSaveError(''); setFormErrors({}); }}
            >
              {showForm ? 'Cancel' : '+ New Invoice'}
            </button>
          </div>
        }
      >
        {saveSuccess && (
          <div style={{
            marginBottom: 12, padding: '8px 12px', borderRadius: 8,
            background: 'hsl(142 70% 45% / 0.1)', color: 'hsl(142 60% 30%)',
            fontSize: 13, border: '1px solid hsl(142 60% 60% / 0.3)',
          }}>
            {saveSuccess}
          </div>
        )}

        {showForm && (
          <div style={{ marginBottom: 24, paddingBottom: 24, borderBottom: '1px solid hsl(var(--border))' }}>
            {saveError && (
              <div className="form-error" role="alert" style={{ marginBottom: 12 }}>{saveError}</div>
            )}
            <form className="form" onSubmit={handleSubmit}>
              <div className="form-grid">
                <label>
                  <span>Customer <span className="required">*</span></span>
                  <input
                    value={form.customer}
                    onChange={(e) => setForm({ ...form, customer: e.target.value })}
                    placeholder="e.g. Tata Motors"
                    autoComplete="off"
                    className={formErrors.customer ? 'input-error' : ''}
                  />
                  {formErrors.customer && <span className="field-error">{formErrors.customer}</span>}
                </label>
                <label>
                  <span>Due Date <span className="required">*</span></span>
                  <input
                    type="date"
                    value={form.due}
                    onChange={(e) => setForm({ ...form, due: e.target.value })}
                    className={formErrors.due ? 'input-error' : ''}
                  />
                  {formErrors.due && <span className="field-error">{formErrors.due}</span>}
                </label>
                <label>
                  <span>Amount (INR) <span className="required">*</span></span>
                  <input
                    type="number"
                    min="0"
                    step="0.01"
                    value={form.amount}
                    onChange={(e) => setForm({ ...form, amount: e.target.value })}
                    placeholder="e.g. 50000"
                    className={formErrors.amount ? 'input-error' : ''}
                  />
                  {formErrors.amount && <span className="field-error">{formErrors.amount}</span>}
                </label>
                <label>
                  <span>Status <span className="required">*</span></span>
                  <select
                    value={form.status}
                    onChange={(e) => setForm({ ...form, status: e.target.value })}
                    className={formErrors.status ? 'input-error' : ''}
                  >
                    {STATUSES.map((s) => <option key={s} value={s}>{s}</option>)}
                  </select>
                  {formErrors.status && <span className="field-error">{formErrors.status}</span>}
                </label>
              </div>
              <div className="form-actions">
                <button className="btn btn-primary" type="submit" disabled={saving}>
                  {saving ? 'Saving...' : 'Create Invoice'}
                </button>
              </div>
            </form>
          </div>
        )}

        {loadError && (
          <div className="form-error" role="alert" style={{ margin: '8px 0' }}>{loadError}</div>
        )}
        {loading && (
          <div style={{ color: 'hsl(var(--muted))', padding: 16, fontSize: 13 }}>Loading...</div>
        )}
        {!loading && !loadError && rows.length === 0 && (
          <div style={{ padding: '24px', textAlign: 'center', color: 'hsl(var(--muted))' }}>
            No invoices found.
          </div>
        )}
        {rows.length > 0 && <DataTable columns={columns} rows={rows} />}
      </FrostedCard>
    </div>
  );
}
