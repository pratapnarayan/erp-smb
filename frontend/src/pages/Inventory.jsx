import React, { useCallback, useEffect, useMemo, useState } from 'react';
import FrostedCard from '../components/FrostedCard.jsx';
import DataTable from '../components/DataTable.jsx';
import Badge from '../components/Badge.jsx';
import http from '../api/clients/http.js';

const statusColor = (status) => {
  const s = String(status || '').trim().toUpperCase();
  if (s === 'ACTIVE' || s === 'OK') return 'green';
  if (s === 'LOW') return 'amber';
  if (s === 'INACTIVE') return 'red';
  return 'gray';
};

const columns = [
  { key: 'sku', label: 'SKU' },
  { key: 'name', label: 'Item' },
  { key: 'stock', label: 'In Stock' },
  { key: 'reorder', label: 'Reorder Point' },
  { key: 'status', label: 'Status', render: (v) => <Badge color={statusColor(v)}>{v}</Badge> },
];

export default function Inventory() {
  const [rows, setRows] = useState([]);
  const [form, setForm] = useState({ sku: '', name: '', stock: '', reorder: '', status: 'ACTIVE' });
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(50);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);

  const load = useCallback(async (p, s) => {
    const { data } = await http.get('/products', { params: { page: p, size: s } });
    const nextTotalPages = data?.totalPages ?? 0;
    const normalizedTotalPages = nextTotalPages > 0 ? nextTotalPages : 1;
    if (nextTotalPages > 0 && p >= nextTotalPages) {
      setPage(nextTotalPages - 1);
      return;
    }
    setRows(data?.content || data?.items || []);
    setTotalPages(normalizedTotalPages);
    setTotalElements(data?.totalElements ?? 0);
  }, []);

  const canDelete = (row) => {
    const status = String(row?.status || '').trim().toUpperCase();
    return status === 'INACTIVE' && Number(row?.stock || 0) === 0;
  };

  const updateProduct = useCallback(async (id, patch) => {
    const { data } = await http.put(`/products/${id}`, patch);
    setRows((prev) => prev.map((r) => (r.id === id ? data : r)));
  }, []);

  const deleteProduct = useCallback(async (id) => {
    await http.delete(`/products/${id}`);
    await load(page, size);
  }, [load, page, size]);

  const tableColumns = useMemo(() => [
    ...columns,
    {
      key: 'actions',
      label: 'Actions',
      render: (_, row) => (
        <div style={{ display: 'flex', gap: 8, alignItems: 'center', flexWrap: 'wrap' }}>
          <select
            value={String(row.status || 'ACTIVE').toUpperCase()}
            onChange={(e) => updateProduct(row.id, { status: e.target.value })}
            style={{ padding: '6px 8px', borderRadius: 8 }}
          >
            <option value="ACTIVE">ACTIVE</option>
            <option value="INACTIVE">INACTIVE</option>
          </select>

          <input
            type="number"
            value={row.stock ?? 0}
            onChange={(e) => setRows((prev) => prev.map((r) => (r.id === row.id ? { ...r, stock: e.target.value } : r)))}
            onBlur={(e) => updateProduct(row.id, { stock: Number(e.target.value || 0) })}
            style={{ width: 90, padding: '6px 8px', borderRadius: 8 }}
            title="In Stock"
          />

          <input
            type="number"
            value={row.reorder ?? 0}
            onChange={(e) => setRows((prev) => prev.map((r) => (r.id === row.id ? { ...r, reorder: e.target.value } : r)))}
            onBlur={(e) => updateProduct(row.id, { reorder: Number(e.target.value || 0) })}
            style={{ width: 110, padding: '6px 8px', borderRadius: 8 }}
            title="Reorder Point"
          />

          <button
            className="btn"
            type="button"
            disabled={!canDelete(row)}
            onClick={() => deleteProduct(row.id)}
            title={canDelete(row) ? 'Delete product' : 'Only INACTIVE products with In Stock = 0 can be deleted'}
          >
            Delete
          </button>
        </div>
      ),
    },
  ], [deleteProduct, updateProduct]);

  useEffect(() => {
    (async () => {
      await load(page, size);
    })();
  }, [load, page, size]);

  const addProduct = async (e) => {
    e.preventDefault();
    if (!form.sku || !form.name) return;
    const payload = {
      sku: form.sku,
      name: form.name,
      stock: Number(form.stock || 0),
      reorder: Number(form.reorder || 0),
      status: form.status || 'ACTIVE',
    };
    await http.post('/products', payload);
    setForm({ sku: '', name: '', stock: '', reorder: '', status: 'ACTIVE' });
    setPage(0);
    await load(0, size);
  };

  return (
    <div className="grid cols-1">
      <FrostedCard title="New Product" subtitle="Add an item to inventory">
        <form className="form" onSubmit={addProduct}>
          <div className="form-grid">
            <label>
              <span>SKU</span>
              <input value={form.sku} onChange={(e) => setForm({ ...form, sku: e.target.value })} placeholder="SKU-1001" />
            </label>
            <label>
              <span>Name</span>
              <input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} placeholder="Widget A" />
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
                <option value="ACTIVE">ACTIVE</option>
                <option value="INACTIVE">INACTIVE</option>
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
        subtitle={`Stock levels and reorder alerts (${totalElements} records)`}
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
        <DataTable columns={tableColumns} rows={rows} />
      </FrostedCard>
    </div>
  );
}
