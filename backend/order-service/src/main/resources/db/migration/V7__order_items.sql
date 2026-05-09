-- Add order line items table and seed demo data.
CREATE TABLE IF NOT EXISTS orders.order_items (
  id         BIGSERIAL PRIMARY KEY,
  order_id   BIGINT NOT NULL REFERENCES orders.sales_orders(id) ON DELETE CASCADE,
  product    VARCHAR(255) NOT NULL,
  sku        VARCHAR(64),
  qty        INT NOT NULL DEFAULT 1,
  unit_price NUMERIC(12,2) NOT NULL,
  total      NUMERIC(12,2) GENERATED ALWAYS AS (qty * unit_price) STORED
);

CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON orders.order_items(order_id);

-- Seed line items for current-month demo orders (inserted by V6).
-- Using subqueries so IDs are resolved from codes rather than hard-coded.
INSERT INTO orders.order_items (order_id, product, sku, qty, unit_price)
SELECT o.id, 'Wireless Keyboard', 'SKU-WKB-001', 2, 1800.00
FROM orders.sales_orders o WHERE o.code = 'SO-DEMO-CUR-0101'
ON CONFLICT DO NOTHING;

INSERT INTO orders.order_items (order_id, product, sku, qty, unit_price)
SELECT o.id, 'USB-C Hub 7-port', 'SKU-HUB-002', 1, 2800.00
FROM orders.sales_orders o WHERE o.code = 'SO-DEMO-CUR-0101'
ON CONFLICT DO NOTHING;

INSERT INTO orders.order_items (order_id, product, sku, qty, unit_price)
SELECT o.id, 'Monitor Stand', 'SKU-MNS-003', 1, 1300.00
FROM orders.sales_orders o WHERE o.code = 'SO-DEMO-CUR-0101'
ON CONFLICT DO NOTHING;

INSERT INTO orders.order_items (order_id, product, sku, qty, unit_price)
SELECT o.id, 'Office Chair Lumbar Support', 'SKU-CHR-010', 3, 1650.00
FROM orders.sales_orders o WHERE o.code = 'SO-DEMO-CUR-0102'
ON CONFLICT DO NOTHING;

INSERT INTO orders.order_items (order_id, product, sku, qty, unit_price)
SELECT o.id, 'Desk Organiser Set', 'SKU-ORG-011', 2, 1150.00
FROM orders.sales_orders o WHERE o.code = 'SO-DEMO-CUR-0102'
ON CONFLICT DO NOTHING;

INSERT INTO orders.order_items (order_id, product, sku, qty, unit_price)
SELECT o.id, 'Laser Printer A4', 'SKU-PRT-020', 1, 7200.00
FROM orders.sales_orders o WHERE o.code = 'SO-DEMO-CUR-0103'
ON CONFLICT DO NOTHING;

INSERT INTO orders.order_items (order_id, product, sku, qty, unit_price)
SELECT o.id, 'Toner Cartridge (Black)', 'SKU-TON-021', 1, 1800.00
FROM orders.sales_orders o WHERE o.code = 'SO-DEMO-CUR-0103'
ON CONFLICT DO NOTHING;

INSERT INTO orders.order_items (order_id, product, sku, qty, unit_price)
SELECT o.id, 'Smart IP Camera', 'SKU-CAM-030', 3, 1700.00
FROM orders.sales_orders o WHERE o.code = 'SO-DEMO-CUR-0104'
ON CONFLICT DO NOTHING;

INSERT INTO orders.order_items (order_id, product, sku, qty, unit_price)
SELECT o.id, 'CAT6 Ethernet Cable 10m', 'SKU-CAB-031', 2, 350.00
FROM orders.sales_orders o WHERE o.code = 'SO-DEMO-CUR-0104'
ON CONFLICT DO NOTHING;

INSERT INTO orders.order_items (order_id, product, sku, qty, unit_price)
SELECT o.id, 'Noise Cancelling Headset', 'SKU-HDT-040', 2, 2150.00
FROM orders.sales_orders o WHERE o.code = 'SO-DEMO-CUR-0106'
ON CONFLICT DO NOTHING;

INSERT INTO orders.order_items (order_id, product, sku, qty, unit_price)
SELECT o.id, 'Webcam HD 1080p', 'SKU-CAM-041', 3, 2300.00
FROM orders.sales_orders o WHERE o.code = 'SO-DEMO-CUR-0106'
ON CONFLICT DO NOTHING;

INSERT INTO orders.order_items (order_id, product, sku, qty, unit_price)
SELECT o.id, 'NAS Storage 4-bay', 'SKU-NAS-050', 1, 9800.00
FROM orders.sales_orders o WHERE o.code = 'SO-DEMO-CUR-0107'
ON CONFLICT DO NOTHING;

INSERT INTO orders.order_items (order_id, product, sku, qty, unit_price)
SELECT o.id, 'External SSD 2TB', 'SKU-SSD-051', 2, 3850.00
FROM orders.sales_orders o WHERE o.code = 'SO-DEMO-CUR-0108'
ON CONFLICT DO NOTHING;
