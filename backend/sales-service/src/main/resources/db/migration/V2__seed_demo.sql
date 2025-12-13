-- Seed demo data for sales.invoices
INSERT INTO sales.invoices (invoice_no, customer, status, due, amount) VALUES
  ('INV-1001','Acme Corp','OPEN','2026-01-01',1200.00),
  ('INV-1002','Globex Inc','PAID','2025-12-15',540.50),
  ('INV-1003','Initech','OVERDUE','2025-12-05',250.00)
ON CONFLICT (invoice_no) DO NOTHING;
