import React, { useState } from 'react';
import FrostedCard from '../components/FrostedCard.jsx';
import DataTable from '../components/DataTable.jsx';

import http from '../api/clients/http.js';

const columns = [
  { key: 'txDate', label: 'Date' },
  { key: 'account', label: 'Account' },
  { key: 'txType', label: 'Type' },
  { key: 'amount', label: 'Amount' },
  { key: 'memo', label: 'Memo' },
];

const initialData = [];

export default function Finance() {
  const [rows, setRows] = useState(initialData);
  const [form, setForm] = useState({ date: '', account: 'Operating', type: 'Income', amount: '', memo: '' });

  React.useEffect(() => {
    (async () => {
      const { data } = await http.get('/finance', { params: { page: 0, size: 50 } });
      setRows(data.content || []);
    })();
  }, []);

  const addRow = async (e) => {
    e.preventDefault();
    if (!form.date || !form.amount) return;
    const payload = {
      txDate: form.date,
      account: form.account,
      txType: form.type,
      amount: Number(form.amount),
      memo: form.memo,
    };
    const { data } = await http.post('/finance', payload);
    setRows([data, ...rows]);
    setForm({ date: '', account: 'Operating', type: 'Income', amount: '', memo: '' });
  };

  return (
    <div className="grid cols-1">
      <FrostedCard title="New Transaction" subtitle="Record income or expense">
        <form className="form" onSubmit={addRow}>
          <div className="form-grid">
            <label>
              <span>Date</span>
              <input type="date" value={form.date} onChange={(e) => setForm({ ...form, date: e.target.value })} />
            </label>
            <label>
              <span>Account</span>
              <select value={form.account} onChange={(e) => setForm({ ...form, account: e.target.value })}>
                <option>Operating</option>
                <option>Savings</option>
                <option>Payroll</option>
              </select>
            </label>
            <label>
              <span>Type</span>
              <select value={form.type} onChange={(e) => setForm({ ...form, type: e.target.value })}>
                <option>Income</option>
                <option>Expense</option>
              </select>
            </label>
            <label>
              <span>Amount</span>
              <input type="number" step="0.01" value={form.amount} onChange={(e) => setForm({ ...form, amount: e.target.value })} placeholder="0.00" />
            </label>
            <label className="span-2">
              <span>Memo</span>
              <input value={form.memo} onChange={(e) => setForm({ ...form, memo: e.target.value })} placeholder="Description" />
            </label>
          </div>
          <div className="form-actions">
            <button className="btn btn-primary" type="submit">Add Transaction</button>
          </div>
        </form>
      </FrostedCard>

      <FrostedCard title="Transactions" subtitle="Recent ledger entries">
        <DataTable columns={columns} rows={rows} />
      </FrostedCard>
    </div>
  );
}
