SET search_path TO reporting;

-- Minimal demo data for tenant 'demo'
INSERT INTO sales_daily_summary(date, tenant_id, region_id, product_id, revenue, qty, orders_count)
VALUES
  (current_date - 10, 'demo', 1, 101, 1200, 30, 6),
  (current_date - 9,  'demo', 1, 102,  800, 20, 4),
  (current_date - 8,  'demo', 2, 101, 1500, 40, 7),
  (current_date - 7,  'demo', 2, 103,  950, 22, 5),
  (current_date - 6,  'demo', 1, 101, 2000, 55, 9),
  (current_date - 5,  'demo', 1, 104,  650, 18, 3),
  (current_date - 4,  'demo', 2, 102, 1100, 28, 6),
  (current_date - 3,  'demo', 2, 103,  900, 25, 5),
  (current_date - 2,  'demo', 1, 101, 1750, 48, 8),
  (current_date - 1,  'demo', 1, 105,  720, 19, 4)
ON CONFLICT DO NOTHING;

INSERT INTO sales_order_facts(order_id, tenant_id, order_date, customer_id, product_id, qty, unit_price, revenue, region_id, salesperson_id, status)
VALUES
  (10001, 'demo', current_date - 10, 501, 101, 5, 40, 200, 1, 9001, 'PAID'),
  (10002, 'demo', current_date - 9,  502, 102, 3, 50, 150, 1, 9002, 'PAID'),
  (10003, 'demo', current_date - 8,  503, 101, 6, 40, 240, 2, 9001, 'PAID'),
  (10004, 'demo', current_date - 7,  504, 103, 2, 80, 160, 2, 9003, 'PAID'),
  (10005, 'demo', current_date - 6,  505, 101, 10, 40, 400, 1, 9001, 'PAID')
ON CONFLICT DO NOTHING;
