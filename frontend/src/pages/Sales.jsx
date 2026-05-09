import React from 'react';
import { formatINR } from '../utils/formatCurrency.js';
import FrostedCard from '../components/FrostedCard.jsx';
import DataTable from '../components/DataTable.jsx';
import Badge from '../components/Badge.jsx';
import http from '../api/clients/http.js';

// Backend may return the invoice number under different field names — try all common variants
const getInvoiceNumber = (row) =>
  row.invoiceNumber || row.invoiceNo || row.invoice || row.invoiceCode || row.code || '—';

const columns = [
  { key: 'invoice', label: 'Invoice #', render: (v, row) => getInvoiceNumber(row) },
  { key: 'customer', label: 'Customer' },
  { key: 'status', label: 'Status', render: (v) => (
    <Badge color={String(v || '').toLowerCase() === 'paid' ? 'green' : String(v || '').toLowerCase() === 'draft' ? 'amber' : 'red'}>
      {v}
    </Badge>
  )},
  { key: 'due', label: 'Due Date' },
  { key: 'amount', label: 'Amount', render: (v) => formatINR(v) },
];

export default function Sales() {
  const [rows, setRows] = React.useState([]);

  React.useEffect(() => {
    (async () => {
      const { data } = await http.get('/sales', { params: { page: 0, size: 50 } });
      setRows(data.content || []);
    })();
  }, []);

  return (
    <div className="grid cols-1">
      <FrostedCard
        title="Invoices"
        subtitle="Manage billing and collections"
        actions={
          <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
            <button className="btn btn-primary">+ New Invoice</button>
            <button className="btn">↓ Export</button>
          </div>
        }
      >
        <DataTable columns={columns} rows={rows} />
        {rows.length === 0 && (
          <div style={{ padding: '32px', textAlign: 'center', color: 'hsl(var(--muted))' }}>
            No invoices found.
          </div>
        )}
      </FrostedCard>
    </div>
  );
}
