import React, { useState } from 'react';
import FrostedCard from '../components/FrostedCard.jsx';
import DataTable from '../components/DataTable.jsx';
import { getEntries, clearEntries } from '../utils/auditLog.js';

function formatTs(iso) {
  if (!iso) return '—';
  try {
    return new Date(iso).toLocaleString('en-IN', {
      day: '2-digit', month: 'short', year: 'numeric',
      hour: '2-digit', minute: '2-digit', second: '2-digit',
    });
  } catch {
    return iso;
  }
}

const ACTION_COLOR = {
  CREATE: 'var(--green-500)',
  UPDATE: 'hsl(var(--primary-500))',
  DELETE: 'var(--red-500)',
  VIEW: 'hsl(var(--muted))',
  LOGIN: 'var(--green-500)',
  LOGOUT: 'var(--red-500)',
};

const columns = [
  { key: 'ts', label: 'Timestamp', render: (v) => <span style={{ fontSize: 12, whiteSpace: 'nowrap' }}>{formatTs(v)}</span> },
  { key: 'user', label: 'User' },
  { key: 'role', label: 'Role', render: (v) => <span style={{ textTransform: 'capitalize', color: 'hsl(var(--muted))', fontSize: 12 }}>{v}</span> },
  { key: 'action', label: 'Action', render: (v) => (
    <span style={{ fontWeight: 600, color: ACTION_COLOR[v] || 'inherit', fontSize: 12 }}>{v}</span>
  )},
  { key: 'entity', label: 'Module' },
  { key: 'detail', label: 'Details', render: (v) => <span style={{ fontSize: 12, color: 'hsl(var(--muted))' }}>{v}</span> },
];

export default function AuditLog() {
  const [entries, setEntries] = useState(() => getEntries());
  const [actionFilter, setActionFilter] = useState('');
  const [search, setSearch] = useState('');

  const refresh = () => setEntries(getEntries());

  const handleClear = () => {
    clearEntries();
    setEntries([]);
  };

  const filtered = entries.filter((e) => {
    if (actionFilter && e.action !== actionFilter) return false;
    if (search) {
      const q = search.toLowerCase();
      return (
        String(e.user || '').toLowerCase().includes(q) ||
        String(e.entity || '').toLowerCase().includes(q) ||
        String(e.detail || '').toLowerCase().includes(q)
      );
    }
    return true;
  });

  return (
    <div className="grid cols-1">
      <div className="grid cols-4">
        {[
          { label: 'Total Events', value: entries.length, color: 'hsl(var(--primary-500))' },
          { label: 'Creates', value: entries.filter((e) => e.action === 'CREATE').length, color: 'var(--green-500)' },
          { label: 'Updates', value: entries.filter((e) => e.action === 'UPDATE').length, color: 'hsl(var(--primary-500))' },
          { label: 'Deletes', value: entries.filter((e) => e.action === 'DELETE').length, color: 'var(--red-500)' },
        ].map((s) => (
          <FrostedCard key={s.label}>
            <div className="card-body">
              <div style={{ fontSize: 12, color: 'hsl(var(--muted))' }}>{s.label}</div>
              <div style={{ fontSize: 28, fontWeight: 700, color: s.color }}>{s.value}</div>
            </div>
          </FrostedCard>
        ))}
      </div>

      <FrostedCard
        title="Activity Log"
        subtitle={`${filtered.length} event${filtered.length !== 1 ? 's' : ''} — session only`}
        actions={
          <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
            <input
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Search user, module, detail…"
              style={{ padding: '6px 10px', borderRadius: 8, border: '1px solid hsl(var(--border))', background: 'hsl(var(--bg-elev))', color: 'inherit', width: 200 }}
            />
            <select
              value={actionFilter}
              onChange={(e) => setActionFilter(e.target.value)}
              style={{ padding: '6px 8px', borderRadius: 8, border: '1px solid hsl(var(--border))', background: 'hsl(var(--bg-elev))', color: 'inherit' }}
            >
              <option value="">All actions</option>
              {['CREATE', 'UPDATE', 'DELETE', 'VIEW', 'LOGIN', 'LOGOUT'].map((a) => <option key={a}>{a}</option>)}
            </select>
            <button className="btn" onClick={refresh}>↻ Refresh</button>
            <button className="btn btn-danger-outline" onClick={handleClear}>Clear</button>
          </div>
        }
      >
        {filtered.length === 0 ? (
          <div style={{ padding: '32px', textAlign: 'center', color: 'hsl(var(--muted))' }}>
            <div style={{ fontSize: 32, marginBottom: 8 }}>📋</div>
            <div style={{ fontWeight: 600, marginBottom: 4 }}>No activity recorded yet</div>
            <div style={{ fontSize: 13 }}>Actions like creates, updates, and deletes will appear here during your session.</div>
          </div>
        ) : (
          <DataTable columns={columns} rows={filtered} initialSort={{ key: 'ts', dir: 'desc' }} />
        )}
      </FrostedCard>
    </div>
  );
}
