-- Seed demo data for sales.invoices with current-date-relative due dates
INSERT INTO sales.invoices (invoice_no, customer, status, due, amount) VALUES
  ('INV-1001','Acme Corp','OPEN', current_date + INTERVAL '20 days', 1200.00),
  ('INV-1002','Globex Inc','PAID', current_date + INTERVAL '7 days', 540.50),
  ('INV-1003','Initech','OVERDUE', current_date - INTERVAL '5 days', 250.00)
ON CONFLICT (invoice_no) DO NOTHING;
