-- Seed ~300 products/items with deterministic realistic data
SET search_path TO products;

WITH nums AS (
  SELECT i FROM generate_series(1,300) AS s(i)
), base AS (
  SELECT 
    1000 + i AS id,
    format('SKU-%04s', 1000 + i) AS sku,
    (ARRAY[
      'Steel Bolt','Aluminium Sheet','Copper Wire','PVC Pipe','Rubber Gasket',
      'Gear Assembly','Hydraulic Pump','Bearing Set','Control Valve','Safety Helmet',
      'LED Panel','Circuit Board','Power Supply','Fiber Cable','Network Switch'
    ])[((i - 1) % 15) + 1] || ' ' || ((i - 1) % 50 + 1) AS name,
    ((i * 17) % 250) + 10 AS stock,
    ((i * 5) % 30) + 5 AS reorder,
    (ARRAY['ACTIVE','ACTIVE','ACTIVE','INACTIVE'])[((i - 1) % 4) + 1] AS status
  FROM nums
)
INSERT INTO items (id, sku, name, stock, reorder, status)
SELECT id, sku, name, stock, reorder, status FROM base
ON CONFLICT (sku) DO NOTHING;
