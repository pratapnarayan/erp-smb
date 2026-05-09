import React, { Suspense } from 'react';
import FrostedCard from './FrostedCard.jsx';

const Sparkline = React.lazy(() => import('./charts/Sparkline.jsx'));

export default function KPIWidget({ label, value, delta, trend = 'up', hint, trendData }) {
  const numericDelta = Number(delta ?? 0);
  const isNeutral = numericDelta === 0 || delta == null;
  const up = trend === 'up' && !isNeutral;
  const color = isNeutral ? 'hsl(var(--muted))' : up ? 'var(--green-500)' : 'var(--red-500)';
  const arrow = isNeutral ? '—' : up ? '▲' : '▼';
  const deltaLabel = isNeutral ? 'No change' : `${arrow} ${numericDelta}`;

  return (
    <FrostedCard tight>
      <div className="kpi">
        <div className="kpi-label">{label}</div>
        <div className="kpi-value">{value ?? '—'}</div>
        <div className="kpi-delta" style={{ color }}>
          {deltaLabel}
        </div>
        {Array.isArray(trendData) && trendData.length > 0 && (
          <Suspense fallback={null}>
            <Sparkline data={trendData} stroke={up ? 'rgba(34,197,94,0.9)' : isNeutral ? 'rgba(148,163,184,0.6)' : 'rgba(239,68,68,0.9)'} />
          </Suspense>
        )}
        {hint && <div className="kpi-hint">{hint}</div>}
      </div>
    </FrostedCard>
  );
}
