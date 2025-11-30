import React from 'react';
import KPIWidget from '../components/KPIWidget.jsx';
import FrostedCard from '../components/FrostedCard.jsx';
import DataTable from '../components/DataTable.jsx';
import Badge from '../components/Badge.jsx';

const kpis = [
  { label: 'MRR', value: '₹42,980', delta: '+6.2%', trend: 'up', hint: 'vs last month' },
  { label: 'Orders', value: '1,284', delta: '+3.1%', trend: 'up', hint: 'last 30 days' },
  { label: 'AR Overdue', value: '₹12,450', delta: '-1.2%', trend: 'down', hint: 'aged > 30d' },
  { label: 'Inventory Turnover', value: '5.2x', delta: '+0.4x', trend: 'up', hint: 'rolling 12m' },
];

const cols = [
  { key: 'id', label: 'Order #' },
  { key: 'customer', label: 'Customer' },
  { key: 'status', label: 'Status', render: (v) => <Badge color={v === 'Paid' ? 'green' : v === 'Pending' ? 'amber' : 'red'}>{v}</Badge> },
  { key: 'total', label: 'Total' },
  { key: 'date', label: 'Date' },
];

const rows = [
  { id: 'SO-1048', customer: 'Acme Co', status: 'Paid', total: '₹1,240.00', date: '2025-11-01' },
  { id: 'SO-1047', customer: 'Globex', status: 'Pending', total: '₹690.00', date: '2025-10-31' },
  { id: 'SO-1046', customer: 'Initech', status: 'Overdue', total: '₹2,140.00', date: '2025-10-30' },
  { id: 'SO-1045', customer: 'Soylent', status: 'Paid', total: '₹320.00', date: '2025-10-28' },
];

export default function Dashboard() {
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
