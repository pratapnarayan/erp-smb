import React from 'react';
import FrostedCard from './FrostedCard.jsx';

export default function KPIWidget({ label, value, delta, trend = 'up', hint }) {
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
        {hint && <div className="kpi-hint">{hint}</div>}
      </div>
    </FrostedCard>
  );
}
