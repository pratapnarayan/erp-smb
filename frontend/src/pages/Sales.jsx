import React from 'react';
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

import http from '../api/clients/http.js';

const data = [];

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
