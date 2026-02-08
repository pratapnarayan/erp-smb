import React, { Suspense } from 'react';
import KPIWidget from '../components/KPIWidget.jsx';
import FrostedCard from '../components/FrostedCard.jsx';
import DataTable from '../components/DataTable.jsx';
import Badge from '../components/Badge.jsx';

import http from '../api/clients/http.js';

// Lazy-load charts to keep initial bundle lightweight.
const OrdersStatusDonut = React.lazy(() => import('../components/charts/OrdersStatusDonut.jsx'));
const MrrTrendLine = React.lazy(() => import('../components/charts/MrrTrendLine.jsx'));
const Sparkline = React.lazy(() => import('../components/charts/Sparkline.jsx'));

import { bankMicroTrend, mrrLast6Months, sparkFromDelta } from '../utils/demoTrends.js';
import { formatINR } from '../utils/formatCurrency.js';

const cols = [
  { key: 'code', label: 'Order #' },
  { key: 'customer', label: 'Customer' },
  { key: 'status', label: 'Status', render: (v) => <Badge color={v === 'Paid' ? 'green' : v === 'Pending' ? 'amber' : 'red'}>{v}</Badge> },
  { key: 'total', label: 'Total', render: (v) => formatINR(v) },
  { key: 'orderDate', label: 'Date' },
];

export default function Dashboard() {
  const [kpis, setKpis] = React.useState([]);
  const [rows, setRows] = React.useState([]);
  const [bankBalances, setBankBalances] = React.useState({ operating: null, savings: null });

  // ---- Demo-only derived series for visuals ----
  // TODO: replace with real historical series when backend provides it.
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

  React.useEffect(() => {
    // Load KPIs
    (async () => {
      try {
        const { data } = await http.get('/finance/kpis');
        // Backend returns a map of KPI objects; convert to array
        const items = Array.isArray(data) ? data : Object.values(data || {});
        setKpis(items);
      } catch (e) {
        console.error('Failed to load KPIs', e);
        setKpis([]);
      }
    })();

    // Load recent orders
    (async () => {
      try {
        const { data } = await http.get('/orders', { params: { page: 0, size: 10 } });
        setRows(data?.content || data?.items || []);
      } catch (e) {
        console.error('Failed to load orders', e);
        setRows([]);
      }
    })();

    // Load bank balances
    (async () => {
      try {
        const { data } = await http.get('/finance/bank-balances');
        setBankBalances({
          operating: data?.operating ?? null,
          savings: data?.savings ?? null,
        });
      } catch (e) {
        console.error('Failed to load bank balances', e);
        setBankBalances({ operating: null, savings: null });
      }
    })();
  }, []);

  return (
    <div className="grid cols-4">
      {kpisWithTrends.map((k) => (
        <KPIWidget
          key={k.label}
          {...k}
          // Monetary KPIs are formatted at render time (UI-only).
          // KPIWidget itself does not guess currency.
          value={k.label === 'MRR' || k.label === 'AR Overdue' ? formatINR(k.value) : k.value}
        />
      ))}
      <div className="grid cols-2" style={{ gridColumn: '1 / -1' }}>
        <FrostedCard title="Recent Orders" subtitle="Last 7 days">
          <DataTable columns={cols} rows={rows} />
        </FrostedCard>

        <FrostedCard title="Bank Accounts" subtitle="Balances">
          <div className="grid cols-2">
            <div>
              <div style={{ color: 'hsl(var(--muted))', fontSize: 12 }}>Operating</div>
              <div style={{ fontSize: 24, fontWeight: 700 }}>{formatINR(bankBalances.operating)}</div>
              {/* Decorative micro-trend (demo) */}
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
              {/* Decorative micro-trend (demo) */}
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
            {/* Demo-safe approximation based on current MRR value */}
            <Suspense fallback={<div style={{ color: 'hsl(var(--muted))', fontSize: 13 }}>Loading chart…</div>}>
              <MrrTrendLine series={mrrSeries} />
            </Suspense>
          </div>
        </FrostedCard>
      </div>
    </div>
  );
}
