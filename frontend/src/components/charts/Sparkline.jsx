import React from 'react';
import { ResponsiveContainer, LineChart, Line } from 'recharts';

/**
 * Minimal sparkline used inside KPI cards.
 *
 * - No axes, no grid, no tooltip.
 * - Muted stroke so it never dominates the KPI value.
 */
export default function Sparkline({ data = [], stroke = 'rgba(148,163,184,0.9)' }) {
  if (!Array.isArray(data) || data.length === 0) return null;

  const points = data.map((v, i) => ({ i, v: Number(v) || 0 }));

  return (
    <div style={{ height: 26, marginTop: 6 }}>
      <ResponsiveContainer width="100%" height="100%">
        <LineChart data={points} margin={{ top: 2, bottom: 2, left: 2, right: 2 }}>
          <Line type="monotone" dataKey="v" stroke={stroke} strokeWidth={2} dot={false} isAnimationActive={false} />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
}
