-- Tune demo data for dashboard KPIs (forward-only migration).
-- This exists because previously-applied seed migrations (V2) must never be edited,
-- otherwise Flyway checksum validation fails.

-- Clear the tiny initial seed rows (by memo) and insert a moderate-SMB profile.
-- NOTE: This is demo/prototype data; accuracy is less important than trend visibility.

-- Remove earlier small demo rows if present
DELETE FROM finance.transactions
WHERE memo IN ('Invoice INV-1001','Invoice INV-1002','Office Supplies');

-- MRR proxy (v1): sum of CREDIT transactions for account='AR' in current calendar month.
-- Previous month revenue ~ ₹1.55L; current month revenue ~ ₹1.85L.
INSERT INTO finance.transactions (tx_date, account, tx_type, amount, memo) VALUES
  -- Previous month
  (date_trunc('month', current_date) - INTERVAL '20 days', 'AR', 'CREDIT', 65000.00, 'Invoice INV-PREV-1001'),
  (date_trunc('month', current_date) - INTERVAL '12 days', 'AR', 'CREDIT', 48000.00, 'Invoice INV-PREV-1002'),
  (date_trunc('month', current_date) - INTERVAL '6 days',  'AR', 'CREDIT', 42000.00, 'Invoice INV-PREV-1003'),

  -- Current month
  (date_trunc('month', current_date) + INTERVAL '2 days',  'AR', 'CREDIT', 72000.00, 'Invoice INV-CUR-2001'),
  (date_trunc('month', current_date) + INTERVAL '8 days',  'AR', 'CREDIT', 56000.00, 'Invoice INV-CUR-2002'),
  (date_trunc('month', current_date) + INTERVAL '15 days', 'AR', 'CREDIT', 43000.00, 'Invoice INV-CUR-2003'),
  (date_trunc('month', current_date) + INTERVAL '18 days', 'AR', 'CREDIT', 14000.00, 'Invoice INV-CUR-2004');

-- AR overdue (v1): AR CREDIT older than 30 days.
INSERT INTO finance.transactions (tx_date, account, tx_type, amount, memo) VALUES
  (current_date - INTERVAL '45 days', 'AR', 'CREDIT', 28000.00, 'Invoice INV-OVERDUE-3001'),
  (current_date - INTERVAL '65 days', 'AR', 'CREDIT', 14000.00, 'Invoice INV-OVERDUE-3002');

-- Bank balances (v1): group by account and sum amount.
-- Ensure Cash/Savings have both credits and debits recently.
INSERT INTO finance.transactions (tx_date, account, tx_type, amount, memo) VALUES
  (current_date - INTERVAL '8 days',  'Cash',    'CREDIT', 220000.00, 'Customer payments batch'),
  (current_date - INTERVAL '6 days',  'Cash',    'DEBIT',   45000.00, 'Payroll'),
  (current_date - INTERVAL '4 days',  'Cash',    'DEBIT',   18000.00, 'Rent / utilities'),
  (current_date - INTERVAL '2 days',  'Cash',    'DEBIT',    6500.00, 'Supplies'),
  (current_date - INTERVAL '12 days', 'Savings', 'CREDIT', 180000.00, 'Transferred to savings'),
  (current_date - INTERVAL '3 days',  'Savings', 'DEBIT',   12000.00, 'Emergency expense');
