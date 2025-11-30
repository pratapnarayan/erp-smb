import React, { useEffect, useState } from 'react';
import { salesApi } from '../api/client.js';
import FrostedCard from '../components/FrostedCard.jsx';
import DataTable from '../components/DataTable.jsx';
import Badge from '../components/Badge.jsx';

const columns = [
  { key: 'invoice', label: 'Invoice #' },
  { key: 'customer', label: 'Customer' },
  { key: 'status', label: 'Status', render: (v) => <Badge color={v === 'Paid' ? 'green' : v === 'Draft' ? 'amber' : 'red'}>{v}</Badge> },
  { key: 'due', label: 'Due Date' },
  { key: 'amount', label: 'Amount' },
];

// replaced by API
const data = [
  { invoice: 'INV-2045', customer: 'Umbrella Corp', status: 'Paid', due: '2025-11-20', amount: '₹1,980.00' },
  { invoice: 'INV-2044', customer: 'Hooli', status: 'Draft', due: '2025-11-22', amount: '₹760.00' },
  { invoice: 'INV-2043', customer: 'Stark Industries', status: 'Overdue', due: '2025-10-15', amount: '₹12,600.00' },
];

export default function Sales() {
  const [rows, setRows] = useState(data);
  useEffect(() => {
    salesApi.list(0,20).then(p=>{
      const mapped = p.content.map(i=>({ invoice: i.invoiceNo, customer: i.customer, status: i.status, due: i.due, amount: `₹${Number(i.amount).toFixed(2)}`}));
      setRows(mapped);
    });
  }, []);
  return (
    <div className="grid cols-1">
      <FrostedCard
        title="Invoices"
        subtitle="Manage billing and collections"
        actions={<>
          <button className="topbar-btn">+ New Invoice</button>
          <button className="topbar-btn">Export</button>
        </>}
      >
        <DataTable columns={columns} rows={rows} />
      </FrostedCard>
    </div>
  );
}
