import React from 'react';
import FrostedCard from '../components/FrostedCard.jsx';
import DataTable from '../components/DataTable.jsx';
import Badge from '../components/Badge.jsx';

import { useEffect, useState } from 'react';
import http from '../api/clients/http.js';

const columns = [
  { key: 'sku', label: 'SKU' },
  { key: 'name', label: 'Item' },
  { key: 'stock', label: 'In Stock' },
  { key: 'reorder', label: 'Reorder Point' },
  { key: 'status', label: 'Status', render: (v) => <Badge color={v === 'OK' ? 'green' : v === 'Low' ? 'amber' : 'red'}>{v}</Badge> },
];

export default function Inventory() {
  const [rows, setRows] = useState([]);
  useEffect(() => {
    (async () => {
      const { data } = await http.get('/products', { params: { page: 0, size: 50 } });
      setRows(data.content || data.items || []);
    })();
  }, []);
  return (
    <FrostedCard title="Inventory" subtitle="Stock levels and reorder alerts">
      <DataTable columns={columns} rows={rows} />
    </FrostedCard>
  );
}
