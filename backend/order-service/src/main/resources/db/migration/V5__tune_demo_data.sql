-- Tune demo data for dashboard KPI visuals (forward-only migration).
-- Exists because V2/V4 seed migrations may already be applied and must not be edited (Flyway checksum).

-- Remove the tiny initial seed orders (if present)
DELETE FROM orders.sales_orders
WHERE code IN ('SO-0001','SO-0002','SO-0003');

-- Insert a small curated set across previous/current months with mixed statuses.
-- This helps KPI delta and donut segmentation even if bulk seed is reduced/changed later.
INSERT INTO orders.sales_orders (code, customer, status, total, order_date) VALUES
  -- Previous month
  ('SO-DEMO-PREV-0001','Acme Corp','DELIVERED',12000.00, (date_trunc('month', current_date) - INTERVAL '1 month' + INTERVAL '3 days')::date),
  ('SO-DEMO-PREV-0002','Globex Inc','SHIPPED',  8400.50, (date_trunc('month', current_date) - INTERVAL '1 month' + INTERVAL '10 days')::date),
  ('SO-DEMO-PREV-0003','Initech',   'CANCELLED',3250.00, (date_trunc('month', current_date) - INTERVAL '1 month' + INTERVAL '18 days')::date),

  -- Current month (different count)
  ('SO-DEMO-CUR-0001','Acme Corp','OPEN',      9800.00, (date_trunc('month', current_date) + INTERVAL '2 days')::date),
  ('SO-DEMO-CUR-0002','Globex Inc','CONFIRMED',7600.00, (date_trunc('month', current_date) + INTERVAL '6 days')::date),
  ('SO-DEMO-CUR-0003','Initech',   'SHIPPED',  4310.00, (date_trunc('month', current_date) + INTERVAL '11 days')::date),
  ('SO-DEMO-CUR-0004','Umbrella',  'DELIVERED',5450.00, (date_trunc('month', current_date) + INTERVAL '15 days')::date)
ON CONFLICT (code) DO NOTHING;
