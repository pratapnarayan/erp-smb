import React, { useEffect, useState } from 'react';
import { productsApi } from '../api/client.js';
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

// replaced by API
const data = [
  { sku: 'SKU-001', item: 'Widget A', stock: 120, reorder: 40, status: 'OK' },
  { sku: 'SKU-002', item: 'Widget B', stock: 18, reorder: 25, status: 'Low' },
  { sku: 'SKU-003', item: 'Widget C', stock: 0, reorder: 15, status: 'Out' },
];

export default function Inventory() {
  const [rows, setRows] = useState(data);
  useEffect(() => {
    productsApi.list(0,20).then(p=>{
      const mapped = p.content.map(it=>({ sku: it.sku, item: it.name, stock: it.stock, reorder: it.reorder, status: it.status }));
      setRows(mapped);
    });
  }, []);
  return (
    <FrostedCard title="Inventory" subtitle="Stock levels and reorder alerts">
      <DataTable columns={columns} rows={rows} />
    </FrostedCard>
  );
}
