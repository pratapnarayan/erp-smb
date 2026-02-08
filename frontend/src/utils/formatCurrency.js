// UI-only currency formatter (demo + future-safe)

export function formatINR(value) {
  const n = typeof value === 'string' ? Number(value) : Number(value);
  const safe = Number.isFinite(n) ? n : 0;

  // Always show 2 decimals, Indian grouping.
  const formatted = new Intl.NumberFormat('en-IN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(safe);

  return `₹${formatted}`;
}
