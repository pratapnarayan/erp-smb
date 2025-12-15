-- Seed demo data for orders.sales_orders with current-date-relative values
INSERT INTO orders.sales_orders (code, customer, status, total, order_date) VALUES
  ('SO-0001','Acme Corp','OPEN',1200.00, current_date - INTERVAL '2 days'),
  ('SO-0002','Globex Inc','CLOSED',540.50, current_date - INTERVAL '1 days'),
  ('SO-0003','Initech','OPEN',250.00, current_date)
ON CONFLICT (code) DO NOTHING;
