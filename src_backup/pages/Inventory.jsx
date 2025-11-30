import React from 'react';
import FrostedCard from '../components/FrostedCard.jsx';
import DataTable from '../components/DataTable.jsx';
import Badge from '../components/Badge.jsx';

const columns = [
  { key: 'sku', label: 'SKU' },
  { key: 'item', label: 'Item' },
  { key: 'stock', label: 'In Stock' },
  { key: 'reorder', label: 'Reorder Point' },
  { key: 'status', label: 'Status', render: (v) => <Badge color={v === 'OK' ? 'green' : v === 'Low' ? 'amber' : 'red'}>{v}</Badge> },
];

const data = [
  { sku: 'SKU-001', item: 'Widget A', stock: 120, reorder: 40, status: 'OK' },
  { sku: 'SKU-002', item: 'Widget B', stock: 18, reorder: 25, status: 'Low' },
  { sku: 'SKU-003', item: 'Widget C', stock: 0, reorder: 15, status: 'Out' },
];

export default function Inventory() {
  return (
    <FrostedCard title="Inventory" subtitle="Stock levels and reorder alerts">
      <DataTable columns={columns} rows={data} />
    </FrostedCard>
  );
}
