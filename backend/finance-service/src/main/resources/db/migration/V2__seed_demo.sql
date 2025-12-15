-- Seed demo data for finance.transactions with current-date-relative values
INSERT INTO finance.transactions (tx_date, account, tx_type, amount, memo) VALUES
  (current_date - INTERVAL '2 days','AR','CREDIT',1200.00,'Invoice INV-1001'),
  (current_date - INTERVAL '1 days','AR','CREDIT',540.50,'Invoice INV-1002'),
  (current_date,'Cash','DEBIT',300.00,'Office Supplies');
