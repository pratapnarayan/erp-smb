import React, { useCallback, useEffect, useMemo, useState } from 'react';
import FrostedCard from '../components/FrostedCard.jsx';
import DataTable from '../components/DataTable.jsx';
import Badge from '../components/Badge.jsx';
import http from '../api/clients/http.js';

// Normalize to title-case for display
const normalizeStatus = (s) => {
  const v = String(s || '').trim().toUpperCase();
  if (v === 'ACTIVE') return 'Active';
  if (v === 'INACTIVE') return 'Inactive';
  if (v === 'LOW') return 'Low Stock';
  return s || 'Active';
};

const statusColor = (s) => {
  const v = String(s || '').trim().toUpperCase();
  if (v === 'ACTIVE' || v === 'OK') return 'green';
  if (v === 'LOW') return 'amber';
  if (v === 'INACTIVE') return 'red';
  return 'gray';
};

const displayColumns = [
  { key: 'sku', label: 'SKU' },
  { key: 'name', label: 'Item' },
  { key: 'stock', label: 'In Stock' },
  { key: 'reorder', label: 'Reorder Point' },
  { key: 'status', label: 'Status', render: (v) => (
    <Badge color={statusColor(v)}>
      {normalizeStatus(v)}
      {String(v || '').toUpperCase() !== 'INACTIVE' && Number(v) !== 0 && null}
    </Badge>
  )},
];

export default function Inventory() {
  const [rows, setRows] = useState([]);
  const [form, setForm] = useState({ sku: '', name: '', stock: '', reorder: '', status: 'ACTIVE' });
  const [formErrors, setFormErrors] = useState({});
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(50);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);
  const [error, setError] = useState('');
  const [editingId, setEditingId] = useState(null); // only one row in edit mode at a time
  const [editDraft, setEditDraft] = useState({});   // draft values while editing
  const [confirmDelete, setConfirmDelete] = useState(null); // { id, name }

  const load = useCallback(async (p, s) => {
    setError('');
    try {
      const { data } = await http.get('/products', { params: { page: p, size: s } });
      const nextTotalPages = data?.totalPages ?? 0;
      const normalized = nextTotalPages > 0 ? nextTotalPages : 1;
      if (nextTotalPages > 0 && p >= nextTotalPages) { setPage(nextTotalPages - 1); return; }
      setRows(data?.content || data?.items || []);
      setTotalPages(normalized);
      setTotalElements(data?.totalElements ?? 0);
    } catch (err) {
      const status = err?.response?.status;
      setError(status === 401 || status === 403 ? 'Session expired. Please login again.' : 'Failed to load products.');
      setRows([]);
    }
  }, []);

  const startEdit = (row) => { setEditingId(row.id); setEditDraft({ stock: row.stock ?? 0, reorder: row.reorder ?? 0, status: String(row.status || 'ACTIVE').toUpperCase() }); };
  const cancelEdit = () => { setEditingId(null); setEditDraft({}); };

  const saveEdit = useCallback(async (id) => {
    const { data } = await http.put(`/products/${id}`, editDraft);
    setRows((prev) => prev.map((r) => (r.id === id ? data : r)));
    setEditingId(null);
    setEditDraft({});
  }, [editDraft]);

  const deleteProduct = useCallback(async (id) => {
    await http.delete(`/products/${id}`);
    setConfirmDelete(null);
    await load(page, size);
  }, [load, page, size]);

  const canDelete = (row) => String(row?.status || '').trim().toUpperCase() === 'INACTIVE' && Number(row?.stock || 0) === 0;

  const isLowStock = (row) => Number(row?.stock ?? 0) < Number(row?.reorder ?? 0) && Number(row?.reorder ?? 0) > 0;

  const tableColumns = useMemo(() => [
    ...displayColumns,
    {
      key: 'actions',
      label: 'Actions',
      render: (_, row) => {
        if (editingId === row.id) {
          return (
            <div style={{ display: 'flex', gap: 6, alignItems: 'center', flexWrap: 'wrap' }}>
              <select value={editDraft.status} onChange={(e) => setEditDraft((d) => ({ ...d, status: e.target.value }))} style={{ padding: '5px 8px', borderRadius: 8 }}>
                <option value="ACTIVE">Active</option>
                <option value="INACTIVE">Inactive</option>
              </select>
              <input type="number" value={editDraft.stock} onChange={(e) => setEditDraft((d) => ({ ...d, stock: e.target.value }))} style={{ width: 80, padding: '5px 8px', borderRadius: 8 }} title="In Stock" />
              <input type="number" value={editDraft.reorder} onChange={(e) => setEditDraft((d) => ({ ...d, reorder: e.target.value }))} style={{ width: 100, padding: '5px 8px', borderRadius: 8 }} title="Reorder Point" />
              <button className="btn btn-primary" onClick={() => saveEdit(row.id)}>Save</button>
              <button className="btn" onClick={cancelEdit}>Cancel</button>
            </div>
          );
        }
        return (
          <div style={{ display: 'flex', gap: 6, alignItems: 'center' }}>
            {isLowStock(row) && <span style={{ color: 'var(--amber-500)', fontSize: 12, fontWeight: 600 }}>⚠ Low Stock</span>}
            <button className="btn" onClick={() => startEdit(row)}>✏ Edit</button>
            <button
              className="btn btn-danger-outline"
              disabled={!canDelete(row)}
              onClick={() => setConfirmDelete({ id: row.id, name: row.name })}
              title={canDelete(row) ? 'Delete product' : 'Only Inactive products with 0 stock can be deleted'}
            >
              Delete
            </button>
          </div>
        );
      },
    },
  ], [editingId, editDraft, saveEdit]);

  useEffect(() => { (async () => { await load(page, size); })(); }, [load, page, size]);

  const validate = () => {
    const errors = {};
    if (!form.sku.trim()) errors.sku = 'SKU is required';
    if (!form.name.trim()) errors.name = 'Item name is required';
    return errors;
  };

  const addProduct = async (e) => {
    e.preventDefault();
    const errors = validate();
    if (Object.keys(errors).length > 0) { setFormErrors(errors); return; }
    setFormErrors({});
    await http.post('/products', { sku: form.sku.trim(), name: form.name.trim(), stock: Number(form.stock || 0), reorder: Number(form.reorder || 0), status: form.status });
    setForm({ sku: '', name: '', stock: '', reorder: '', status: 'ACTIVE' });
    setPage(0);
    await load(0, size);
  };

  return (
    <div className="grid cols-1">
      {/* Delete confirmation dialog */}
      {confirmDelete && (
        <div className="dialog-overlay" role="dialog" aria-modal="true">
          <div className="dialog">
            <h3 className="dialog-title">Delete Product</h3>
            <p className="dialog-body">
              Are you sure you want to delete <strong>{confirmDelete.name}</strong>? This action cannot be undone.
            </p>
            <div className="dialog-actions">
              <button className="btn" onClick={() => setConfirmDelete(null)}>Cancel</button>
              <button className="btn btn-danger" onClick={() => deleteProduct(confirmDelete.id)}>Delete</button>
            </div>
          </div>
        </div>
      )}

      <FrostedCard title="New Product" subtitle="Add an item to inventory">
        <form className="form" onSubmit={addProduct}>
          <div className="form-grid">
            <label>
              <span>SKU <span className="required">*</span></span>
              <input value={form.sku} onChange={(e) => setForm({ ...form, sku: e.target.value })} placeholder="SKU-1001" className={formErrors.sku ? 'input-error' : ''} />
              {formErrors.sku && <span className="field-error">{formErrors.sku}</span>}
            </label>
            <label>
              <span>Name <span className="required">*</span></span>
              <input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} placeholder="Widget A" className={formErrors.name ? 'input-error' : ''} />
              {formErrors.name && <span className="field-error">{formErrors.name}</span>}
            </label>
            <label>
              <span>In Stock</span>
              <input type="number" value={form.stock} onChange={(e) => setForm({ ...form, stock: e.target.value })} placeholder="0" />
            </label>
            <label>
              <span>Reorder Point</span>
              <input type="number" value={form.reorder} onChange={(e) => setForm({ ...form, reorder: e.target.value })} placeholder="0" />
            </label>
            <label>
              <span>Status</span>
              <select value={form.status} onChange={(e) => setForm({ ...form, status: e.target.value })}>
                <option value="ACTIVE">Active</option>
                <option value="INACTIVE">Inactive</option>
              </select>
            </label>
          </div>
          <div className="form-actions">
            <button className="btn btn-primary" type="submit">Add Product</button>
          </div>
        </form>
      </FrostedCard>

      <FrostedCard
        title="Inventory"
        subtitle={`Stock levels and reorder alerts (${totalElements} records) — click ✏ Edit to modify a row`}
        actions={
          <div style={{ display: 'flex', gap: 8, alignItems: 'center', flexWrap: 'wrap' }}>
            <button className="btn" disabled={page <= 0} onClick={() => setPage((p) => Math.max(0, p - 1))}>Prev</button>
            <div style={{ minWidth: 110, textAlign: 'center' }}>Page {page + 1} / {totalPages}</div>
            <button className="btn" disabled={page >= totalPages - 1} onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}>Next</button>
            <select value={size} onChange={(e) => { setSize(Number(e.target.value)); setPage(0); }} style={{ padding: '6px 8px', borderRadius: 8 }}>
              <option value={10}>10 / page</option>
              <option value={20}>20 / page</option>
              <option value={50}>50 / page</option>
              <option value={100}>100 / page</option>
            </select>
          </div>
        }
      >
        {error && <div className="form-error" role="alert">{error}</div>}
        <DataTable columns={tableColumns} rows={rows} />
      </FrostedCard>
    </div>
  );
}
