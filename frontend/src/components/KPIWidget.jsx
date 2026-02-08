import React, { Suspense } from 'react';
import FrostedCard from './FrostedCard.jsx';

// Lazy-load charts to keep initial bundle lightweight.
const Sparkline = React.lazy(() => import('./charts/Sparkline.jsx'));


export default function KPIWidget({ label, value, delta, trend = 'up', hint, trendData }) {
  const up = trend === 'up';
  const color = up ? 'var(--green-500)' : 'var(--red-500)';
  const arrow = up ? '▲' : '▼';
  return (
    <FrostedCard tight>
      <div className="kpi">
        <div className="kpi-label">{label}</div>
        <div className="kpi-value">{value}</div>
        <div className="kpi-delta" style={{ color }}>
          {arrow} {delta}
        </div>

        {/* Optional demo/prototype sparkline */}
        {Array.isArray(trendData) && trendData.length > 0 && (
          <Suspense fallback={null}>
            <Sparkline data={trendData} stroke={up ? 'rgba(34,197,94,0.9)' : 'rgba(239,68,68,0.9)'} />
          </Suspense>
        )}

        {hint && <div className="kpi-hint">{hint}</div>}
      </div>
    </FrostedCard>
  );
}
