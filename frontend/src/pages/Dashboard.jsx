import React from 'react';
import KPIWidget from '../components/KPIWidget.jsx';
import FrostedCard from '../components/FrostedCard.jsx';
import DataTable from '../components/DataTable.jsx';
import Badge from '../components/Badge.jsx';

import http from '../api/clients/http.js';

const cols = [
  { key: 'code', label: 'Order #' },
  { key: 'customer', label: 'Customer' },
  { key: 'status', label: 'Status', render: (v) => <Badge color={v === 'Paid' ? 'green' : v === 'Pending' ? 'amber' : 'red'}>{v}</Badge> },
  { key: 'total', label: 'Total' },
  { key: 'orderDate', label: 'Date' },
];

export default function Dashboard() {
  const [kpis, setKpis] = React.useState([]);
  const [rows, setRows] = React.useState([]);

  React.useEffect(() => {
    // Load KPIs
    (async () => {
      try {
        const { data } = await http.get('/finance/kpis');
        // Backend returns a map of KPI objects; convert to array
        const items = Array.isArray(data) ? data : Object.values(data || {});
        setKpis(items);
      } catch (e) {
        console.error('Failed to load KPIs', e);
        setKpis([]);
      }
    })();

    // Load recent orders
    (async () => {
      try {
        const { data } = await http.get('/orders', { params: { page: 0, size: 10 } });
        setRows(data?.content || data?.items || []);
      } catch (e) {
        console.error('Failed to load orders', e);
        setRows([]);
      }
    })();
  }, []);

  return (
    <div className="grid cols-4">
      {kpis.map((k) => (
        <KPIWidget key={k.label} {...k} />
      ))}
      <div className="grid cols-2" style={{ gridColumn: '1 / -1' }}>
        <FrostedCard title="Recent Orders" subtitle="Last 7 days">
          <DataTable columns={cols} rows={rows} />
        </FrostedCard>
        <FrostedCard title="Bank Accounts" subtitle="Balances">
          <div className="grid cols-2">
            <div>
              <div style={{ color: 'hsl(var(--muted))', fontSize: 12 }}>Operating</div>
              <div style={{ fontSize: 24, fontWeight: 700 }}>₹84,230</div>
            </div>
            <div>
              <div style={{ color: 'hsl(var(--muted))', fontSize: 12 }}>Savings</div>
              <div style={{ fontSize: 24, fontWeight: 700 }}>₹152,400</div>
            </div>
          </div>
        </FrostedCard>
      </div>
    </div>
  );
}
