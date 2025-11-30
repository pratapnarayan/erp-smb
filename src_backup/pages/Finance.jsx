import React, { useState } from 'react';
import FrostedCard from '../components/FrostedCard.jsx';
import DataTable from '../components/DataTable.jsx';

const columns = [
  { key: 'date', label: 'Date' },
  { key: 'account', label: 'Account' },
  { key: 'type', label: 'Type' },
  { key: 'amount', label: 'Amount' },
  { key: 'memo', label: 'Memo' },
];

const initialData = [
  { date: '2025-11-03', account: 'Operating', type: 'Income', amount: '₹3,500.00', memo: 'Subscription revenue' },
  { date: '2025-11-02', account: 'Operating', type: 'Expense', amount: '₹-640.00', memo: 'Shipping' },
  { date: '2025-11-01', account: 'Savings', type: 'Income', amount: '₹1,200.00', memo: 'Interest' },
];

export default function Finance() {
  const [rows, setRows] = useState(initialData);
  const [form, setForm] = useState({ date: '', account: 'Operating', type: 'Income', amount: '', memo: '' });

  const addRow = (e) => {
    e.preventDefault();
    if (!form.date || !form.amount) return;
    const amount = form.amount.startsWith('$') ? form.amount : `${form.type === 'Expense' ? '-' : ''}$${Math.abs(Number(form.amount)).toFixed(2)}`;
    setRows([{ ...form, amount }, ...rows]);
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
