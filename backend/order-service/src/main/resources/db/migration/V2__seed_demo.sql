-- Seed demo data for orders.sales_orders
INSERT INTO orders.sales_orders (code, customer, status, total, order_date) VALUES
  ('SO-0001','Acme Corp','OPEN',1200.00,'2025-12-01'),
  ('SO-0002','Globex Inc','CLOSED',540.50,'2025-12-02'),
  ('SO-0003','Initech','OPEN',250.00,'2025-12-03')
ON CONFLICT (code) DO NOTHING;
