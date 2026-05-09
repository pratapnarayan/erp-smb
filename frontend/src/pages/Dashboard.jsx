import React, { Suspense } from 'react';
import KPIWidget from '../components/KPIWidget.jsx';
import FrostedCard from '../components/FrostedCard.jsx';
import DataTable from '../components/DataTable.jsx';
import Badge from '../components/Badge.jsx';

import http from '../api/clients/http.js';

const OrdersStatusDonut = React.lazy(() => import('../components/charts/OrdersStatusDonut.jsx'));
const MrrTrendLine = React.lazy(() => import('../components/charts/MrrTrendLine.jsx'));
const Sparkline = React.lazy(() => import('../components/charts/Sparkline.jsx'));

import { bankMicroTrend, mrrLast6Months, sparkFromDelta } from '../utils/demoTrends.js';
import { formatINR } from '../utils/formatCurrency.js';

const PERIODS = [
  { label: 'This Month', value: 'month' },
  { label: 'Last 7 Days', value: '7d' },
  { label: 'Last 30 Days', value: '30d' },
  { label: 'Last 90 Days', value: '90d' },
  { label: 'Custom', value: 'custom' },
];

function getPeriodDates(period, customFrom, customTo) {
  const now = new Date();
  if (period === 'month') {
    const start = new Date(now.getFullYear(), now.getMonth(), 1);
    return { from: toISO(start), to: toISO(now) };
  }
  if (period === '7d') return { from: toISO(daysAgo(7)), to: toISO(now) };
  if (period === '30d') return { from: toISO(daysAgo(30)), to: toISO(now) };
  if (period === '90d') return { from: toISO(daysAgo(90)), to: toISO(now) };
  if (period === 'custom') return { from: customFrom, to: customTo };
  return {};
}

function daysAgo(n) { const d = new Date(); d.setDate(d.getDate() - n); return d; }
function toISO(d) { return d instanceof Date ? d.toISOString().slice(0, 10) : d; }

const orderStatusColor = (v) => {
  const s = String(v || '').toLowerCase();
  if (s === 'delivered' || s === 'paid') return 'green';
  if (s === 'pending' || s === 'open' || s === 'confirmed') return 'amber';
  if (s === 'cancelled' || s === 'overdue') return 'red';
  if (s === 'shipped') return 'blue';
  return 'gray';
};

const cols = [
  { key: 'code', label: 'Order #' },
  { key: 'customer', label: 'Customer' },
  { key: 'status', label: 'Status', render: (v) => <Badge color={orderStatusColor(v)}>{v}</Badge> },
  { key: 'total', label: 'Total', render: (v) => formatINR(v) },
  { key: 'orderDate', label: 'Date' },
];

export default function Dashboard() {
  const [kpis, setKpis] = React.useState([]);
  const [rows, setRows] = React.useState([]);
  const [bankBalances, setBankBalances] = React.useState({ operating: null, savings: null });
  const [period, setPeriod] = React.useState('month');
  const [customFrom, setCustomFrom] = React.useState('');
  const [customTo, setCustomTo] = React.useState('');
  const [kpiLoading, setKpiLoading] = React.useState(false);

  const kpisWithTrends = React.useMemo(() => {
    return (kpis || []).map((k) => ({
      ...k,
      trendData: sparkFromDelta(k?.value, k?.delta, 10),
    }));
  }, [kpis]);

  const ordersStatusCounts = React.useMemo(() => {
    const counts = {};
    for (const r of rows || []) {
      const status = String(r?.status || 'UNKNOWN').toUpperCase();
      counts[status] = (counts[status] || 0) + 1;
    }
    return counts;
  }, [rows]);

  const mrrKpi = React.useMemo(() => (kpis || []).find((k) => k?.label === 'MRR'), [kpis]);
  const mrrSeries = React.useMemo(() => mrrLast6Months(mrrKpi?.value), [mrrKpi?.value]);
  const operatingTrend = React.useMemo(() => bankMicroTrend(bankBalances.operating), [bankBalances.operating]);
  const savingsTrend = React.useMemo(() => bankMicroTrend(bankBalances.savings), [bankBalances.savings]);

  const { from, to } = React.useMemo(() => getPeriodDates(period, customFrom, customTo), [period, customFrom, customTo]);

  const loadOrders = React.useCallback(async () => {
    try {
      const params = { page: 0, size: 20 };
      if (from) params.from = from;
      if (to) params.to = to;
      const { data } = await http.get('/orders', { params });
      setRows(data?.content || data?.items || []);
    } catch (e) {
      console.error('Failed to load orders', e);
      setRows([]);
    }
  }, [from, to]);

  React.useEffect(() => {
    setKpiLoading(true);
    (async () => {
      try {
        const { data } = await http.get('/finance/kpis');
        const items = Array.isArray(data) ? data : Object.values(data || {});
        setKpis(items);
      } catch (e) {
        console.error('Failed to load KPIs', e);
        setKpis([]);
      } finally {
        setKpiLoading(false);
      }
    })();

    (async () => {
      try {
        const { data } = await http.get('/finance/bank-balances');
        setBankBalances({ operating: data?.operating ?? null, savings: data?.savings ?? null });
      } catch (e) {
        console.error('Failed to load bank balances', e);
      }
    })();
  }, []);

  React.useEffect(() => {
    if (period === 'custom' && (!customFrom || !customTo)) return;
    loadOrders();
  }, [loadOrders, period, customFrom, customTo]);

  const periodLabel = PERIODS.find((p) => p.value === period)?.label || 'This Month';

  return (
    <div className="grid cols-4">
      {/* Period selector */}
      <div style={{ gridColumn: '1 / -1', display: 'flex', gap: 8, alignItems: 'center', flexWrap: 'wrap' }}>
        <span style={{ fontSize: 13, color: 'hsl(var(--muted))', marginRight: 4 }}>Period:</span>
        {PERIODS.map((p) => (
          <button
            key={p.value}
            onClick={() => setPeriod(p.value)}
            style={{
              padding: '5px 12px', borderRadius: 8, fontSize: 13, cursor: 'pointer',
              border: '1px solid hsl(var(--border))',
              background: period === p.value ? 'hsl(var(--primary-500) / 0.15)' : 'hsl(var(--bg-elev))',
              color: period === p.value ? 'hsl(var(--primary-500))' : 'inherit',
              fontWeight: period === p.value ? 600 : 400,
            }}
          >
            {p.label}
          </button>
        ))}
        {period === 'custom' && (
          <>
            <input type="date" value={customFrom} onChange={(e) => setCustomFrom(e.target.value)} style={{ padding: '5px 8px', borderRadius: 8, border: '1px solid hsl(var(--border))', background: 'hsl(var(--bg-elev))', color: 'inherit', fontSize: 13 }} />
            <span style={{ color: 'hsl(var(--muted))' }}>→</span>
            <input type="date" value={customTo} onChange={(e) => setCustomTo(e.target.value)} style={{ padding: '5px 8px', borderRadius: 8, border: '1px solid hsl(var(--border))', background: 'hsl(var(--bg-elev))', color: 'inherit', fontSize: 13 }} />
          </>
        )}
        {kpiLoading && <span style={{ fontSize: 12, color: 'hsl(var(--muted))' }}>Loading KPIs…</span>}
      </div>

      {kpisWithTrends.map((k) => (
        <KPIWidget
          key={k.label}
          {...k}
          value={k.label === 'MRR' || k.label === 'AR Overdue' ? formatINR(k.value) : k.value}
        />
      ))}

      <div className="grid cols-2" style={{ gridColumn: '1 / -1' }}>
        <FrostedCard title="Recent Orders" subtitle={`Orders — ${periodLabel}`}>
          <DataTable columns={cols} rows={rows} />
        </FrostedCard>

        <FrostedCard title="Bank Accounts" subtitle="Balances">
          <div className="grid cols-2">
            <div>
              <div style={{ color: 'hsl(var(--muted))', fontSize: 12 }}>Operating</div>
              <div style={{ fontSize: 24, fontWeight: 700 }}>{formatINR(bankBalances.operating)}</div>
              {operatingTrend.length > 0 && (
                <div style={{ opacity: 0.75 }}>
                  <Suspense fallback={null}>
                    <Sparkline data={operatingTrend} stroke="rgba(148,163,184,0.9)" />
                  </Suspense>
                </div>
              )}
            </div>
            <div>
              <div style={{ color: 'hsl(var(--muted))', fontSize: 12 }}>Savings</div>
              <div style={{ fontSize: 24, fontWeight: 700 }}>{formatINR(bankBalances.savings)}</div>
              {savingsTrend.length > 0 && (
                <div style={{ opacity: 0.75 }}>
                  <Suspense fallback={null}>
                    <Sparkline data={savingsTrend} stroke="rgba(148,163,184,0.9)" />
                  </Suspense>
                </div>
              )}
            </div>
          </div>

          <div style={{ marginTop: 14 }}>
            <Suspense fallback={<div style={{ color: 'hsl(var(--muted))', fontSize: 13 }}>Loading chart…</div>}>
              <OrdersStatusDonut counts={ordersStatusCounts} />
            </Suspense>
          </div>

          <div style={{ marginTop: 14 }}>
            <div style={{ fontSize: 12, color: 'hsl(var(--muted))', marginBottom: 8 }}>MRR (last 6 months)</div>
            <Suspense fallback={<div style={{ color: 'hsl(var(--muted))', fontSize: 13 }}>Loading chart…</div>}>
              <MrrTrendLine series={mrrSeries} />
            </Suspense>
          </div>
        </FrostedCard>
      </div>
    </div>
  );
}
