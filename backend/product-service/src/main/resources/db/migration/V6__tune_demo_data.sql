-- Tune demo data for inventory KPI visuals (forward-only migration).
-- Exists because earlier seed migrations (V2/V4) may already be applied.

-- Add a small curated active catalog with non-zero stock.
-- (We don't delete bulk items here; we just ensure there's always a believable baseline set.)
INSERT INTO products.items (sku, name, stock, reorder, status) VALUES
  ('SKU-1001','Widget A',120,20,'ACTIVE'),
  ('SKU-1002','Widget B',80,10,'ACTIVE'),
  ('SKU-1003','Widget C',60,10,'ACTIVE'),
  ('SKU-2001','Gadget X',50,5,'ACTIVE'),
  ('SKU-2002','Gadget Y',35,5,'ACTIVE'),
  ('SKU-3001','Spare Part P',25,5,'ACTIVE'),
  ('SKU-3002','Spare Part Q',18,3,'ACTIVE'),
  ('SKU-4001','Service Plan S',0,0,'INACTIVE')
ON CONFLICT (sku) DO NOTHING;
