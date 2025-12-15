-- Seed demo data for products.items
INSERT INTO products.items (sku, name, stock, reorder, status) VALUES
  ('SKU-1001','Widget A',120,20,'ACTIVE'),
  ('SKU-1002','Widget B',80,10,'ACTIVE'),
  ('SKU-2001','Gadget X',50,5,'ACTIVE'),
  ('SKU-3001','Service Plan S',0,0,'INACTIVE')
ON CONFLICT (sku) DO NOTHING;
