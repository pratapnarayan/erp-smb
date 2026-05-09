import React, { useEffect, useState } from 'react';
import { reportingApi } from '../api/clients/index.js';
import DataTable from '../components/DataTable.jsx';
import Badge from '../components/Badge.jsx';
import FrostedCard from '../components/FrostedCard.jsx';

const formatTs = (iso) => {
  if (!iso) return '—';
  try {
    return new Intl.DateTimeFormat('en-IN', {
      day: '2-digit', month: 'short', year: 'numeric',
      hour: '2-digit', minute: '2-digit', hour12: true,
    }).format(new Date(iso));
  } catch {
    return iso;
  }
};

const runStatusColor = (s) => {
  const v = String(s || '').toLowerCase();
  if (v === 'completed') return 'green';
  if (v === 'failed') return 'red';
  if (v === 'running' || v === 'queued') return 'amber';
  return 'gray';
};

export default function Reporting() {
  const [defs, setDefs] = useState([]);
  const [runs, setRuns] = useState({ content: [], totalElements: 0 });
  const [loading, setLoading] = useState(false);
  const [period, setPeriod] = useState('month');

  async function load() {
    setLoading(true);
    try {
      const [d, r] = await Promise.all([
        reportingApi.definitions(),
        reportingApi.runs(0, 20),
      ]);
      setDefs(d);
      setRuns(r);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { load(); }, []);

  async function runReport(code) {
    setLoading(true);
    try {
      const resp = await reportingApi.run(code, '{}', 'CSV');
      const r = await reportingApi.runs(0, 20);
      setRuns(r);
      alert(`Queued run ${resp.runId} for ${code}`);
    } catch (e) {
      console.error(e);
      alert('Failed to queue report. Please try again.');
    } finally {
      setLoading(false);
    }
  }

  async function downloadLatest(runId) {
    try {
      const list = await reportingApi.exportsByRun(runId);
      if (!list || list.length === 0) { alert('No export available yet for this run.'); return; }
      const exportId = list[0].id;
      const blob = await reportingApi.download(exportId);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `report-${runId}.csv`;
      document.body.appendChild(a);
      a.click();
      a.remove();
      window.URL.revokeObjectURL(url);
    } catch (e) {
      console.error(e);
      alert('Failed to download export. Please try again.');
    }
  }

  const defsColumns = [
    { key: 'code', label: 'Code' },
    { key: 'name', label: 'Name' },
    { key: 'category', label: 'Category' },
    { key: 'actions', label: 'Actions', render: (v, row) => (
      <button className="btn btn-primary" onClick={() => runReport(row.code)} disabled={loading}>
        ▶ Run CSV
      </button>
    )},
  ];

  const runsColumns = [
    { key: 'id', label: 'Run ID' },
    { key: 'definitionCode', label: 'Report' },
    { key: 'status', label: 'Status', render: (v) => <Badge color={runStatusColor(v)}>{v}</Badge> },
    { key: 'requestedAt', label: 'Requested At', render: (v) => formatTs(v) },
    { key: 'errorMessage', label: 'Error', render: (v) => v ? (
      <span style={{ color: 'var(--red-500)', fontSize: 12 }} title={v}>
        {String(v).length > 60 ? String(v).slice(0, 60) + '…' : v}
      </span>
    ) : null },
    { key: 'actions', label: 'Actions', render: (v, row) => {
      const isDone = String(row.status || '').toLowerCase() === 'completed';
      const isFailed = String(row.status || '').toLowerCase() === 'failed';
      return (
        <div style={{ display: 'flex', gap: 6 }}>
          {isDone && (
            <button className="btn btn-primary" onClick={() => downloadLatest(row.id)}>
              ↓ Download CSV
            </button>
          )}
          {isFailed && (
            <button className="btn" onClick={() => runReport(row.definitionCode)} disabled={loading}>
              ↺ Retry
            </button>
          )}
        </div>
      );
    }},
  ];

  return (
    <div className="grid cols-1">
      <div style={{ display: 'flex', gap: 12, alignItems: 'center', flexWrap: 'wrap' }}>
        <label style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          Period:
          <select value={period} onChange={(e) => setPeriod(e.target.value)} style={{ padding: '6px 8px', borderRadius: 8, border: '1px solid hsl(var(--border))', background: 'hsl(var(--bg-elev))', color: 'inherit' }}>
            <option value="day">Day</option>
            <option value="week">Week</option>
            <option value="month">Month</option>
          </select>
        </label>
        {loading && <span className="badge">Loading…</span>}
      </div>

      <FrostedCard title="Report Definitions" subtitle="Available report templates">
        <DataTable columns={defsColumns} rows={defs} />
        {defs.length === 0 && !loading && (
          <div style={{ padding: '24px', textAlign: 'center', color: 'hsl(var(--muted))' }}>No report definitions available.</div>
        )}
      </FrostedCard>

      <FrostedCard title="Recent Runs" subtitle="Report execution history">
        <DataTable columns={runsColumns} rows={runs?.content || []} />
        {(runs?.content || []).length === 0 && !loading && (
          <div style={{ padding: '24px', textAlign: 'center', color: 'hsl(var(--muted))' }}>No report runs yet. Run a report above to get started.</div>
        )}
      </FrostedCard>
    </div>
  );
}
