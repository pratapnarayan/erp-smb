-- Demo data patch: ensure Orders KPI is non-zero in the current month.
-- Forward-only migration (do not edit previously applied migrations).

-- Insert additional current-month orders across multiple statuses.
-- These codes are unique and safe to re-run.
INSERT INTO orders.sales_orders (code, customer, status, total, order_date) VALUES
  ('SO-DEMO-CUR-0101','BlueSky Traders','OPEN',       6400.00, (date_trunc('month', current_date) + INTERVAL '1 day')::date),
  ('SO-DEMO-CUR-0102','BlueSky Traders','CONFIRMED',  8900.00, (date_trunc('month', current_date) + INTERVAL '3 day')::date),
  ('SO-DEMO-CUR-0103','Metro Supplies', 'SHIPPED',    7200.00, (date_trunc('month', current_date) + INTERVAL '6 day')::date),
  ('SO-DEMO-CUR-0104','Metro Supplies', 'DELIVERED',  5100.00, (date_trunc('month', current_date) + INTERVAL '9 day')::date),
  ('SO-DEMO-CUR-0105','Sunrise Retail', 'CANCELLED',  4300.00, (date_trunc('month', current_date) + INTERVAL '12 day')::date),
  ('SO-DEMO-CUR-0106','Sunrise Retail', 'DELIVERED', 11200.00, (date_trunc('month', current_date) + INTERVAL '15 day')::date),
  ('SO-DEMO-CUR-0107','Acme Corp',      'SHIPPED',    9800.00, (date_trunc('month', current_date) + INTERVAL '18 day')::date),
  ('SO-DEMO-CUR-0108','Globex Inc',     'CONFIRMED',  7600.00, (date_trunc('month', current_date) + INTERVAL '20 day')::date)
ON CONFLICT (code) DO NOTHING;
