-- Seed demo data for finance.transactions
INSERT INTO finance.transactions (tx_date, account, tx_type, amount, memo) VALUES
  ('2025-12-01','AR','CREDIT',1200.00,'Invoice INV-1001'),
  ('2025-12-02','AR','CREDIT',540.50,'Invoice INV-1002'),
  ('2025-12-03','Cash','DEBIT',300.00,'Office Supplies');
