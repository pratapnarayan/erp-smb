import React, { useMemo, useState } from 'react';
import { formatINR } from '../utils/formatCurrency.js';
import FrostedCard from '../components/FrostedCard.jsx';
import DataTable from '../components/DataTable.jsx';
import http from '../api/clients/http.js';

const TABS = ['Transactions', 'AR Aging', 'P&L Statement'];

const columns = [
  { key: 'txDate', label: 'Date' },
  { key: 'account', label: 'Account' },
  { key: 'txType', label: 'Type', render: (v) => (
    <span style={{ color: String(v || '').toLowerCase() === 'credit' || String(v || '').toLowerCase() === 'income' ? 'var(--green-500)' : 'var(--red-500)', fontWeight: 600 }}>
      {v}
    </span>
  )},
  { key: 'amount', label: 'Amount', render: (v) => formatINR(v) },
  { key: 'memo', label: 'Memo' },
];

const ACCOUNTS = ['Operating', 'Savings', 'Payroll'];
const TYPES = ['Income', 'Expense'];

const AR_BUCKETS = [
  { label: '0–30 days', key: 'b0', color: 'var(--green-500)' },
  { label: '31–60 days', key: 'b31', color: 'var(--amber-500)' },
  { label: '61–90 days', key: 'b61', color: '#f97316' },
  { label: '90+ days', key: 'b90', color: 'var(--red-500)' },
];

function computeArAging(rows) {
  const today = new Date();
  const buckets = { b0: [], b31: [], b61: [], b90: [] };
  const arRows = rows.filter((r) => String(r.account || '').toUpperCase() === 'AR' && String(r.txType || '').toUpperCase() === 'CREDIT');
  for (const r of arRows) {
    if (!r.txDate) continue;
    const age = Math.floor((today - new Date(r.txDate)) / 86400000);
    if (age <= 30) buckets.b0.push({ ...r, ageDays: age });
    else if (age <= 60) buckets.b31.push({ ...r, ageDays: age });
    else if (age <= 90) buckets.b61.push({ ...r, ageDays: age });
    else buckets.b90.push({ ...r, ageDays: age });
  }
  return buckets;
}

function computePnl(rows) {
  const now = new Date();
  const months = [];
  for (let i = 5; i >= 0; i--) {
    const d = new Date(now.getFullYear(), now.getMonth() - i, 1);
    months.push({
      label: d.toLocaleString('en-IN', { month: 'short', year: '2-digit' }),
      year: d.getFullYear(),
      month: d.getMonth(),
      income: 0,
      expense: 0,
    });
  }
  for (const r of rows) {
    if (!r.txDate) continue;
    const d = new Date(r.txDate);
    const m = months.find((m) => m.year === d.getFullYear() && m.month === d.getMonth());
    if (!m) continue;
    const type = String(r.txType || '').toUpperCase();
    const amt = Number(r.amount || 0);
    if (type === 'CREDIT' || type === 'INCOME') m.income += amt;
    else m.expense += amt;
  }
  return months.map((m) => ({ ...m, net: m.income - m.expense }));
}

const ageCols = [
  { key: 'memo', label: 'Invoice' },
  { key: 'txDate', label: 'Date' },
  { key: 'ageDays', label: 'Age (days)' },
  { key: 'amount', label: 'Amount', render: (v) => formatINR(v) },
];

export default function Finance() {
  const [tab, setTab] = useState('Transactions');
  const [rows, setRows] = useState([]);
  const [form, setForm] = useState({ date: '', account: 'Operating', type: 'Income', amount: '', memo: '' });
  const [formErrors, setFormErrors] = useState({});
  const [search, setSearch] = useState('');
  const [accountFilter, setAccountFilter] = useState('');
  const [typeFilter, setTypeFilter] = useState('');
  const [dateFrom, setDateFrom] = useState('');
  const [dateTo, setDateTo] = useState('');
  const [agingBucket, setAgingBucket] = useState(null);

  React.useEffect(() => {
    (async () => {
      try {
        const { data } = await http.get('/finance', { params: { page: 0, size: 200 } });
        setRows(data.content || []);
      } catch (e) {
        console.error('Failed to load finance data', e);
      }
    })();
  }, []);

  const validate = () => {
    const errors = {};
    if (!form.date) errors.date = 'Date is required';
    if (!form.amount || isNaN(Number(form.amount)) || Number(form.amount) <= 0) errors.amount = 'Valid amount is required';
    return errors;
  };

  const addRow = async (e) => {
    e.preventDefault();
    const errors = validate();
    if (Object.keys(errors).length > 0) { setFormErrors(errors); return; }
    setFormErrors({});
    const payload = { txDate: form.date, account: form.account, txType: form.type, amount: Number(form.amount), memo: form.memo.trim() };
    const { data } = await http.post('/finance', payload);
    setRows([data, ...rows]);
    setForm({ date: '', account: 'Operating', type: 'Income', amount: '', memo: '' });
  };

  const filteredRows = useMemo(() => rows.filter((r) => {
    if (search && !String(r.memo || '').toLowerCase().includes(search.toLowerCase()) && !String(r.account || '').toLowerCase().includes(search.toLowerCase())) return false;
    if (accountFilter && String(r.account || '').toLowerCase() !== accountFilter.toLowerCase()) return false;
    if (typeFilter && String(r.txType || '').toLowerCase() !== typeFilter.toLowerCase()) return false;
    if (dateFrom && r.txDate && r.txDate < dateFrom) return false;
    if (dateTo && r.txDate && r.txDate > dateTo) return false;
    return true;
  }), [rows, search, accountFilter, typeFilter, dateFrom, dateTo]);

  const clearFilters = () => { setSearch(''); setAccountFilter(''); setTypeFilter(''); setDateFrom(''); setDateTo(''); };
  const hasFilters = search || accountFilter || typeFilter || dateFrom || dateTo;

  const arBuckets = useMemo(() => computeArAging(rows), [rows]);
  const pnl = useMemo(() => computePnl(rows), [rows]);
  const totalIncome = pnl.reduce((s, m) => s + m.income, 0);
  const totalExpense = pnl.reduce((s, m) => s + m.expense, 0);

  return (
    <div className="grid cols-1">
      {/* Tab bar */}
      <div style={{ display: 'flex', gap: 6, borderBottom: '1px solid hsl(var(--border))' }}>
        {TABS.map((t) => (
          <button
            key={t}
            onClick={() => setTab(t)}
            style={{
              padding: '8px 16px', border: 'none', background: 'none', cursor: 'pointer',
              color: tab === t ? 'hsl(var(--primary-500))' : 'hsl(var(--muted))',
              borderBottom: tab === t ? '2px solid hsl(var(--primary-500))' : '2px solid transparent',
              fontWeight: tab === t ? 600 : 400, fontSize: 14,
            }}
          >
            {t}
          </button>
        ))}
      </div>

      {tab === 'Transactions' && (
        <>
          <FrostedCard title="New Transaction" subtitle="Record income or expense">
            <form className="form" onSubmit={addRow}>
              <div className="form-grid">
                <label>
                  <span>Date <span className="required">*</span></span>
                  <input type="date" value={form.date} onChange={(e) => setForm({ ...form, date: e.target.value })} className={formErrors.date ? 'input-error' : ''} />
                  {formErrors.date && <span className="field-error">{formErrors.date}</span>}
                </label>
                <label>
                  <span>Account</span>
                  <select value={form.account} onChange={(e) => setForm({ ...form, account: e.target.value })}>
                    {ACCOUNTS.map((a) => <option key={a}>{a}</option>)}
                  </select>
                </label>
                <label>
                  <span>Type</span>
                  <select value={form.type} onChange={(e) => setForm({ ...form, type: e.target.value })}>
                    {TYPES.map((t) => <option key={t}>{t}</option>)}
                  </select>
                </label>
                <label>
                  <span>Amount <span className="required">*</span></span>
                  <input type="number" step="0.01" value={form.amount} onChange={(e) => setForm({ ...form, amount: e.target.value })} placeholder="0.00" className={formErrors.amount ? 'input-error' : ''} />
                  {formErrors.amount && <span className="field-error">{formErrors.amount}</span>}
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

          <FrostedCard
            title="Transactions"
            subtitle={`Ledger entries${hasFilters ? ` — showing ${filteredRows.length} of ${rows.length}` : ` (${rows.length} total)`}`}
            actions={
              <div style={{ display: 'flex', gap: 8, alignItems: 'center', flexWrap: 'wrap' }}>
                <input value={search} onChange={(e) => setSearch(e.target.value)} placeholder="Search memo / account…" style={{ padding: '6px 10px', borderRadius: 8, border: '1px solid hsl(var(--border))', background: 'hsl(var(--bg-elev))', color: 'inherit', width: 180 }} />
                <select value={accountFilter} onChange={(e) => setAccountFilter(e.target.value)} style={{ padding: '6px 8px', borderRadius: 8, border: '1px solid hsl(var(--border))', background: 'hsl(var(--bg-elev))', color: 'inherit' }}>
                  <option value="">All accounts</option>
                  {ACCOUNTS.map((a) => <option key={a} value={a}>{a}</option>)}
                </select>
                <select value={typeFilter} onChange={(e) => setTypeFilter(e.target.value)} style={{ padding: '6px 8px', borderRadius: 8, border: '1px solid hsl(var(--border))', background: 'hsl(var(--bg-elev))', color: 'inherit' }}>
                  <option value="">All types</option>
                  {TYPES.map((t) => <option key={t} value={t}>{t}</option>)}
                </select>
                <input type="date" value={dateFrom} onChange={(e) => setDateFrom(e.target.value)} title="From date" style={{ padding: '6px 8px', borderRadius: 8, border: '1px solid hsl(var(--border))', background: 'hsl(var(--bg-elev))', color: 'inherit' }} />
                <input type="date" value={dateTo} onChange={(e) => setDateTo(e.target.value)} title="To date" style={{ padding: '6px 8px', borderRadius: 8, border: '1px solid hsl(var(--border))', background: 'hsl(var(--bg-elev))', color: 'inherit' }} />
                {hasFilters && <button className="btn" onClick={clearFilters}>Clear</button>}
              </div>
            }
          >
            <DataTable columns={columns} rows={filteredRows} />
          </FrostedCard>
        </>
      )}

      {tab === 'AR Aging' && (
        <>
          <div className="grid cols-4">
            {AR_BUCKETS.map((b) => {
              const total = arBuckets[b.key].reduce((s, r) => s + Number(r.amount || 0), 0);
              const count = arBuckets[b.key].length;
              return (
                <FrostedCard key={b.key}>
                  <div className="card-body" style={{ cursor: 'pointer' }} onClick={() => setAgingBucket(agingBucket === b.key ? null : b.key)}>
                    <div style={{ fontSize: 12, color: 'hsl(var(--muted))' }}>{b.label}</div>
                    <div style={{ fontSize: 22, fontWeight: 700, color: b.color, margin: '4px 0' }}>{formatINR(total)}</div>
                    <div style={{ fontSize: 12, color: 'hsl(var(--muted))' }}>{count} invoice{count !== 1 ? 's' : ''}</div>
                  </div>
                </FrostedCard>
              );
            })}
          </div>
          {agingBucket && (
            <FrostedCard title={`${AR_BUCKETS.find((b) => b.key === agingBucket)?.label} — Details`}>
              {arBuckets[agingBucket].length === 0 ? (
                <div style={{ padding: '24px', textAlign: 'center', color: 'hsl(var(--muted))', fontSize: 13 }}>No overdue invoices in this bucket.</div>
              ) : (
                <DataTable columns={ageCols} rows={arBuckets[agingBucket]} />
              )}
            </FrostedCard>
          )}
          {!agingBucket && (
            <div style={{ color: 'hsl(var(--muted))', fontSize: 13, textAlign: 'center', padding: 16 }}>
              Click a bucket above to see the individual invoices.
            </div>
          )}
        </>
      )}

      {tab === 'P&L Statement' && (
        <FrostedCard title="Profit & Loss Statement" subtitle="Last 6 months">
          <div className="grid cols-3" style={{ marginBottom: 16 }}>
            <div style={{ background: 'hsl(142 70% 45% / 0.08)', borderRadius: 10, padding: '12px 16px' }}>
              <div style={{ fontSize: 12, color: 'hsl(var(--muted))' }}>Total Income</div>
              <div style={{ fontSize: 20, fontWeight: 700, color: 'var(--green-500)' }}>{formatINR(totalIncome)}</div>
            </div>
            <div style={{ background: 'hsl(0 80% 55% / 0.08)', borderRadius: 10, padding: '12px 16px' }}>
              <div style={{ fontSize: 12, color: 'hsl(var(--muted))' }}>Total Expenses</div>
              <div style={{ fontSize: 20, fontWeight: 700, color: 'var(--red-500)' }}>{formatINR(totalExpense)}</div>
            </div>
            <div style={{ background: 'hsl(var(--primary-500) / 0.08)', borderRadius: 10, padding: '12px 16px' }}>
              <div style={{ fontSize: 12, color: 'hsl(var(--muted))' }}>Net Profit</div>
              <div style={{ fontSize: 20, fontWeight: 700, color: totalIncome - totalExpense >= 0 ? 'var(--green-500)' : 'var(--red-500)' }}>{formatINR(totalIncome - totalExpense)}</div>
            </div>
          </div>
          <div className="table-wrapper">
            <table className="table">
              <thead>
                <tr>
                  <th style={{ textAlign: 'left' }}>Month</th>
                  <th style={{ textAlign: 'right' }}>Income</th>
                  <th style={{ textAlign: 'right' }}>Expenses</th>
                  <th style={{ textAlign: 'right' }}>Net</th>
                  <th style={{ textAlign: 'right' }}>Margin</th>
                </tr>
              </thead>
              <tbody>
                {pnl.map((m) => {
                  const margin = m.income > 0 ? ((m.net / m.income) * 100).toFixed(1) : '—';
                  return (
                    <tr key={m.label}>
                      <td style={{ fontWeight: 600 }}>{m.label}</td>
                      <td style={{ textAlign: 'right', color: 'var(--green-500)' }}>{formatINR(m.income)}</td>
                      <td style={{ textAlign: 'right', color: 'var(--red-500)' }}>{formatINR(m.expense)}</td>
                      <td style={{ textAlign: 'right', fontWeight: 600, color: m.net >= 0 ? 'var(--green-500)' : 'var(--red-500)' }}>{formatINR(m.net)}</td>
                      <td style={{ textAlign: 'right', color: 'hsl(var(--muted))' }}>{typeof margin === 'number' ? `${margin}%` : margin}</td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </FrostedCard>
      )}
    </div>
  );
}
