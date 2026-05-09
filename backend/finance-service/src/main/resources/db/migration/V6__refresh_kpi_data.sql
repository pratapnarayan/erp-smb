-- Refresh KPI data so MRR and bank-balance figures reflect the actual current month.
-- Root cause: V5 used date_trunc('month', current_date) which was evaluated at migration
-- run-time (when the schema was first applied).  Those dates are now stale.
-- This migration deletes and re-inserts only the demo rows we own (identifiable by memo).

-- ── MRR / AR CREDIT rows ─────────────────────────────────────────────────────
-- Remove stale current-month and previous-month AR CREDIT demo rows
DELETE FROM finance.transactions
WHERE memo IN (
  'Invoice INV-PREV-1001', 'Invoice INV-PREV-1002', 'Invoice INV-PREV-1003',
  'Invoice INV-CUR-2001',  'Invoice INV-CUR-2002',  'Invoice INV-CUR-2003',  'Invoice INV-CUR-2004'
);

-- Re-insert with dates that resolve to the current calendar month when this migration runs.
-- Previous month revenue ≈ ₹1.55 L; current month revenue ≈ ₹1.85 L.
INSERT INTO finance.transactions (tx_date, account, tx_type, amount, memo) VALUES
  -- Previous month
  ((date_trunc('month', current_date) - INTERVAL '20 days')::date, 'AR', 'CREDIT',  65000.00, 'Invoice INV-PREV-1001'),
  ((date_trunc('month', current_date) - INTERVAL '12 days')::date, 'AR', 'CREDIT',  48000.00, 'Invoice INV-PREV-1002'),
  ((date_trunc('month', current_date) - INTERVAL '6 days')::date,  'AR', 'CREDIT',  42000.00, 'Invoice INV-PREV-1003'),

  -- Current month
  ((date_trunc('month', current_date) + INTERVAL '2 days')::date,  'AR', 'CREDIT',  72000.00, 'Invoice INV-CUR-2001'),
  ((date_trunc('month', current_date) + INTERVAL '8 days')::date,  'AR', 'CREDIT',  56000.00, 'Invoice INV-CUR-2002'),
  ((date_trunc('month', current_date) + INTERVAL '15 days')::date, 'AR', 'CREDIT',  43000.00, 'Invoice INV-CUR-2003'),
  ((date_trunc('month', current_date) + INTERVAL '18 days')::date, 'AR', 'CREDIT',  14000.00, 'Invoice INV-CUR-2004');

-- ── AR Overdue rows ───────────────────────────────────────────────────────────
-- Remove stale overdue rows and re-insert so they stay > 30 days old relative to today.
DELETE FROM finance.transactions
WHERE memo IN ('Invoice INV-OVERDUE-3001', 'Invoice INV-OVERDUE-3002');

INSERT INTO finance.transactions (tx_date, account, tx_type, amount, memo) VALUES
  ((current_date - INTERVAL '45 days')::date, 'AR', 'CREDIT', 28000.00, 'Invoice INV-OVERDUE-3001'),
  ((current_date - INTERVAL '65 days')::date, 'AR', 'CREDIT', 14000.00, 'Invoice INV-OVERDUE-3002');

-- ── Bank balance rows ─────────────────────────────────────────────────────────
-- The bank balance query sums ALL transactions per account (no date window), so stale
-- dates do not break it — but multiple migration runs can accumulate duplicates.
-- Wipe known V5 bank rows and re-insert once with fresh dates.
DELETE FROM finance.transactions
WHERE memo IN (
  'Customer payments batch', 'Payroll', 'Rent / utilities', 'Supplies',
  'Transferred to savings',  'Emergency expense'
);

INSERT INTO finance.transactions (tx_date, account, tx_type, amount, memo) VALUES
  ((current_date - INTERVAL '8 days')::date,  'Cash',    'CREDIT', 220000.00, 'Customer payments batch'),
  ((current_date - INTERVAL '6 days')::date,  'Cash',    'DEBIT',   45000.00, 'Payroll'),
  ((current_date - INTERVAL '4 days')::date,  'Cash',    'DEBIT',   18000.00, 'Rent / utilities'),
  ((current_date - INTERVAL '2 days')::date,  'Cash',    'DEBIT',    6500.00, 'Supplies'),
  ((current_date - INTERVAL '12 days')::date, 'Savings', 'CREDIT', 180000.00, 'Transferred to savings'),
  ((current_date - INTERVAL '3 days')::date,  'Savings', 'DEBIT',   12000.00, 'Emergency expense');
