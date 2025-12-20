import React, { useEffect, useState } from 'react';
import { reportingApi } from '../api/clients/index.js';
import DataTable from '../components/DataTable.jsx';
import KPIWidget from '../components/KPIWidget.jsx';

export default function Reporting() {
  const [defs, setDefs] = useState([]);
  const [runs, setRuns] = useState({ content: [], totalElements: 0 });
  const [loading, setLoading] = useState(false);
  const [period, setPeriod] = useState('month');
  const [metrics, setMetrics] = useState([]);

  async function load() {
    setLoading(true);
    try {
      const [d, r, m] = await Promise.all([
        reportingApi.definitions(),
        reportingApi.runs(0, 20),
        reportingApi.metrics(period),
      ]);
      setDefs(d);
      setRuns(r);
      setMetrics(m || []);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { load(); }, []);
  useEffect(() => { (async () => { try { setMetrics(await reportingApi.metrics(period)); } catch {} })(); }, [period]);

  async function runReport(code) {
    setLoading(true);
    try {
      const resp = await reportingApi.run(code, '{}', 'CSV');
      // reload runs list
      const r = await reportingApi.runs(0, 20);
      setRuns(r);
      alert(`Queued run ${resp.runId} for ${code}`);
    } catch (e) {
      console.error(e);
      alert('Failed to queue report');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div>
      <div style={{ display: 'flex', gap: '12px', marginBottom: 16 }}>
        <KPIWidget label={`Metrics (${period})`} value={metrics?.length || 0} delta={metrics?.length || 0} trend="up" />
        <div style={{ marginLeft: 'auto' }}>
          <label>
            Period:&nbsp;
            <select value={period} onChange={(e) => setPeriod(e.target.value)}>
              <option value="day">day</option>
              <option value="week">week</option>
              <option value="month">month</option>
            </select>
          </label>
        </div>
      </div>

      <h3>Report Definitions</h3>
      <DataTable
        columns={[
          { key: 'code', label: 'Code' },
          { key: 'name', label: 'Name' },
          { key: 'category', label: 'Category' },
          { key: 'actions', label: 'Actions', render: (v, row) => (
              <button onClick={() => runReport(row.code)} aria-label={`Run ${row.code}`}>Run CSV</button>
            ) },
        ]}
        rows={defs}
      />

      <h3 style={{ marginTop: 24 }}>Recent Runs</h3>
      <DataTable
        columns={[
          { key: 'id', label: 'ID' },
          { key: 'status', label: 'Status' },
          { key: 'requestedAt', label: 'Requested At' },
        ]}
        rows={runs?.content || []}
      />

      {loading && <div className="badge">Loading…</div>}
    </div>
  );
}
