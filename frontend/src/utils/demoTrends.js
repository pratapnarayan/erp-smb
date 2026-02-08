/**
 * Demo-only trend generators for dashboard visuals.
 *
 * TODO: replace with real historical series when backend provides it.
 */

export function sparkFromDelta(value, delta, points = 10) {
  const v = Number(value) || 0;
  const d = Number(delta) || 0;

  // If values are missing, return empty so charts don't render.
  if (!Number.isFinite(v) || !Number.isFinite(d)) return [];

  // Create a simple progression that ends at current value.
  // Example: delta=10 means previous approx is value/1.10.
  const prev = d === 0 ? v : v / (1 + d / 100);
  const arr = [];
  for (let i = 0; i < points; i++) {
    const t = points === 1 ? 1 : i / (points - 1);
    // small wobble to look more natural
    const wobble = Math.sin(i * 0.9) * (Math.abs(v - prev) * 0.03);
    arr.push(prev + (v - prev) * t + wobble);
  }
  return arr;
}

export function mrrLast6Months(value) {
  const v = Number(value) || 0;
  if (!Number.isFinite(v)) return [];

  const now = new Date();
  const out = [];

  // Demo-safe approximation: gently ramp up/down around current MRR.
  // TODO: replace with real monthly totals.
  for (let i = 5; i >= 0; i--) {
    const d = new Date(now.getFullYear(), now.getMonth() - i, 1);
    const month = d.toLocaleString('en-US', { month: 'short' });

    const factor = 0.85 + (5 - i) * 0.03; // gradual ramp
    const noise = Math.sin(i * 1.3) * (v * 0.04);
    out.push({ month, value: Math.max(0, Math.round(v * factor + noise)) });
  }
  return out;
}

export function bankMicroTrend(balance) {
  const b = Number(balance);
  if (!Number.isFinite(b)) return [];

  // Small 8-point decorative trend around current balance.
  // TODO: replace with real daily/weekly balance history.
  return Array.from({ length: 8 }, (_, i) => {
    const wobble = Math.sin(i * 0.8) * (b * 0.02);
    const drift = (i - 7) * (b * 0.003);
    return Math.max(0, b + wobble + drift);
  });
}
