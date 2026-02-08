import React from 'react';
import { ResponsiveContainer, PieChart, Pie, Cell } from 'recharts';

const STATUS_ORDER = ['OPEN', 'CONFIRMED', 'SHIPPED', 'DELIVERED', 'CANCELLED'];

// Keep colors simple and consistent.
// TODO: if/when the UI has standardized badge colors for these statuses, reuse them.
const STATUS_COLORS = {
  OPEN: '#f59e0b',
  CONFIRMED: '#38bdf8',
  SHIPPED: '#a78bfa',
  DELIVERED: '#22c55e',
  CANCELLED: '#ef4444',
  UNKNOWN: '#94a3b8',
};

export default function OrdersStatusDonut({ counts }) {
  const entries = React.useMemo(() => {
    const c = counts || {};
    const normalized = STATUS_ORDER.map((k) => ({ status: k, value: Number(c[k] || 0) }));
    const unknown = Object.keys(c)
      .filter((k) => !STATUS_ORDER.includes(k))
      .reduce((sum, k) => sum + Number(c[k] || 0), 0);

    if (unknown > 0) normalized.push({ status: 'UNKNOWN', value: unknown });
    return normalized;
  }, [counts]);

  const total = entries.reduce((s, e) => s + e.value, 0);

  if (!total) {
    return <div style={{ color: 'hsl(var(--muted))', fontSize: 13 }}>No orders yet</div>;
  }

  return (
    <div className="grid cols-2" style={{ gap: 12, alignItems: 'center' }}>
      <div style={{ height: 160 }}>
        <ResponsiveContainer width="100%" height="100%">
          <PieChart>
            <Pie
              data={entries}
              dataKey="value"
              nameKey="status"
              innerRadius={48}
              outerRadius={68}
              paddingAngle={2}
              isAnimationActive={false}
            >
              {entries.map((e) => (
                <Cell key={e.status} fill={STATUS_COLORS[e.status] || STATUS_COLORS.UNKNOWN} />
              ))}
            </Pie>
          </PieChart>
        </ResponsiveContainer>
      </div>

      <div>
        <div style={{ fontSize: 12, color: 'hsl(var(--muted))', marginBottom: 8 }}>Order Status</div>
        <div style={{ display: 'grid', gap: 6 }}>
          {entries
            .filter((e) => e.value > 0)
            .map((e) => (
              <div key={e.status} style={{ display: 'flex', justifyContent: 'space-between', gap: 12 }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                  <span
                    style={{
                      width: 10,
                      height: 10,
                      borderRadius: 3,
                      background: STATUS_COLORS[e.status] || STATUS_COLORS.UNKNOWN,
                      display: 'inline-block',
                    }}
                  />
                  <span style={{ fontSize: 13 }}>{e.status}</span>
                </div>
                <span style={{ fontSize: 13, color: 'hsl(var(--muted))' }}>{e.value}</span>
              </div>
            ))}
        </div>
      </div>
    </div>
  );
}
