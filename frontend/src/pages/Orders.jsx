import React, { useEffect, useState } from 'react';
import { formatINR } from '../utils/formatCurrency.js';
import FrostedCard from '../components/FrostedCard.jsx';
import DataTable from '../components/DataTable.jsx';
import Badge from '../components/Badge.jsx';
import http from '../api/clients/http.js';

const ORDER_STATUSES = ['Pending', 'Open', 'Confirmed', 'Shipped', 'Delivered', 'Cancelled'];

const statusColor = (v) => {
  const s = String(v || '').toLowerCase();
  if (s === 'delivered' || s === 'paid') return 'green';
  if (s === 'pending' || s === 'open' || s === 'confirmed') return 'amber';
  if (s === 'cancelled' || s === 'overdue') return 'red';
  if (s === 'shipped') return 'blue';
  return 'gray';
};

const columns = [
  { key: 'code', label: 'Order #' },
  { key: 'customer', label: 'Customer' },
  { key: 'status', label: 'Status', render: (v) => <Badge color={statusColor(v)}>{v}</Badge> },
  { key: 'total', label: 'Total', render: (v) => formatINR(v) },
  { key: 'orderDate', label: 'Date' },
];

export default function Orders() {
  const [rows, setRows] = useState([]);
  const [form, setForm] = useState({ customer: '', status: 'Pending', total: '', date: '' });
  const [formErrors, setFormErrors] = useState({});
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [orderItems, setOrderItems] = useState([]);
  const [itemsLoading, setItemsLoading] = useState(false);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('');

  useEffect(() => {
    (async () => {
      const { data } = await http.get('/orders', { params: { page: 0, size: 50 } });
      setRows(data.content || []);
    })();
  }, []);

  const validate = () => {
    const errors = {};
    if (!form.customer.trim()) errors.customer = 'Customer name is required';
    if (!form.total || isNaN(Number(form.total)) || Number(form.total) <= 0) errors.total = 'Valid amount is required';
    if (!form.date) errors.date = 'Date is required';
    return errors;
  };

  const addRow = async (e) => {
    e.preventDefault();
    const errors = validate();
    if (Object.keys(errors).length > 0) { setFormErrors(errors); return; }
    setFormErrors({});
    const payload = {
      code: `SO-${Math.floor(1000 + Math.random() * 9000)}`,
      customer: form.customer.trim(),
      status: form.status,
      total: Number(form.total),
      orderDate: form.date,
    };
    const { data } = await http.post('/orders', payload);
    setRows([data, ...rows]);
    setForm({ customer: '', status: 'Pending', total: '', date: '' });
  };

  const filteredRows = rows.filter((r) => {
    const matchSearch = !search ||
      String(r.code || '').toLowerCase().includes(search.toLowerCase()) ||
      String(r.customer || '').toLowerCase().includes(search.toLowerCase());
    const matchStatus = !statusFilter || String(r.status || '').toLowerCase() === statusFilter.toLowerCase();
    return matchSearch && matchStatus;
  });

  return (
    <div className="grid cols-1">
      {/* Order detail modal */}
      {selectedOrder && (
        <div className="dialog-overlay" role="dialog" aria-modal="true" onClick={(e) => { if (e.target === e.currentTarget) setSelectedOrder(null); }}>
          <div className="dialog dialog--wide">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
              <h3 className="dialog-title" style={{ margin: 0 }}>Order {selectedOrder.code}</h3>
              <button className="btn" onClick={() => setSelectedOrder(null)}>✕ Close</button>
            </div>
            <div className="grid cols-2" style={{ gap: 12, marginBottom: 16 }}>
              <div><span style={{ color: 'hsl(var(--muted))', fontSize: 12 }}>Customer</span><div style={{ fontWeight: 600 }}>{selectedOrder.customer}</div></div>
              <div><span style={{ color: 'hsl(var(--muted))', fontSize: 12 }}>Status</span><div><Badge color={statusColor(selectedOrder.status)}>{selectedOrder.status}</Badge></div></div>
              <div><span style={{ color: 'hsl(var(--muted))', fontSize: 12 }}>Order Date</span><div>{selectedOrder.orderDate}</div></div>
              <div><span style={{ color: 'hsl(var(--muted))', fontSize: 12 }}>Total</span><div style={{ fontWeight: 700, fontSize: 18 }}>{formatINR(selectedOrder.total)}</div></div>
            </div>
            <div>
              <div style={{ fontWeight: 600, marginBottom: 8 }}>Line Items</div>
              {itemsLoading ? (
                <div style={{ color: 'hsl(var(--muted))', fontSize: 13 }}>Loading…</div>
              ) : orderItems.length === 0 ? (
                <div style={{ color: 'hsl(var(--muted))', fontSize: 13, padding: '12px 0' }}>No line items available for this order.</div>
              ) : (
                <table className="table" style={{ fontSize: 13 }}>
                  <thead>
                    <tr>
                      <th style={{ textAlign: 'left' }}>Product</th>
                      <th style={{ textAlign: 'left' }}>SKU</th>
                      <th style={{ textAlign: 'right' }}>Qty</th>
                      <th style={{ textAlign: 'right' }}>Unit Price</th>
                      <th style={{ textAlign: 'right' }}>Total</th>
                    </tr>
                  </thead>
                  <tbody>
                    {orderItems.map((item) => (
                      <tr key={item.id}>
                        <td>{item.product}</td>
                        <td style={{ color: 'hsl(var(--muted))' }}>{item.sku || '—'}</td>
                        <td style={{ textAlign: 'right' }}>{item.qty}</td>
                        <td style={{ textAlign: 'right' }}>{formatINR(item.unitPrice)}</td>
                        <td style={{ textAlign: 'right', fontWeight: 600 }}>{formatINR(item.total ?? item.qty * item.unitPrice)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          </div>
        </div>
      )}

      <FrostedCard title="New Order" subtitle="Create a sales order">
        <form className="form" onSubmit={addRow}>
          <div className="form-grid">
            <label>
              <span>Customer <span className="required">*</span></span>
              <input value={form.customer} onChange={(e) => setForm({ ...form, customer: e.target.value })} placeholder="Customer name" className={formErrors.customer ? 'input-error' : ''} />
              {formErrors.customer && <span className="field-error">{formErrors.customer}</span>}
            </label>
            <label>
              <span>Status</span>
              <select value={form.status} onChange={(e) => setForm({ ...form, status: e.target.value })}>
                {ORDER_STATUSES.map((s) => <option key={s}>{s}</option>)}
              </select>
            </label>
            <label>
              <span>Total <span className="required">*</span></span>
              <input type="number" step="0.01" value={form.total} onChange={(e) => setForm({ ...form, total: e.target.value })} placeholder="0.00" className={formErrors.total ? 'input-error' : ''} />
              {formErrors.total && <span className="field-error">{formErrors.total}</span>}
            </label>
            <label>
              <span>Date <span className="required">*</span></span>
              <input type="date" value={form.date} onChange={(e) => setForm({ ...form, date: e.target.value })} className={formErrors.date ? 'input-error' : ''} />
              {formErrors.date && <span className="field-error">{formErrors.date}</span>}
            </label>
          </div>
          <div className="form-actions">
            <button className="btn btn-primary" type="submit">Add Order</button>
          </div>
        </form>
      </FrostedCard>

      <FrostedCard
        title="Orders"
        subtitle="Recent sales orders — click a row to view details"
        actions={
          <div style={{ display: 'flex', gap: 8, alignItems: 'center', flexWrap: 'wrap' }}>
            <input
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Search by order # or customer…"
              style={{ padding: '6px 10px', borderRadius: 8, border: '1px solid hsl(var(--border))', background: 'hsl(var(--bg-elev))', color: 'inherit', width: 220 }}
            />
            <select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)} style={{ padding: '6px 8px', borderRadius: 8, border: '1px solid hsl(var(--border))', background: 'hsl(var(--bg-elev))', color: 'inherit' }}>
              <option value="">All statuses</option>
              {ORDER_STATUSES.map((s) => <option key={s} value={s}>{s}</option>)}
            </select>
          </div>
        }
      >
        <DataTable
          columns={columns}
          rows={filteredRows}
          onRowClick={async (row) => {
            setSelectedOrder(row);
            setOrderItems([]);
            setItemsLoading(true);
            try {
              const { data } = await http.get(`/orders/${row.id}/items`);
              setOrderItems(Array.isArray(data) ? data : []);
            } catch {
              setOrderItems([]);
            } finally {
              setItemsLoading(false);
            }
          }}
        />
      </FrostedCard>
    </div>
  );
}
