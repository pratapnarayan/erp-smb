-- Demo data patch: tune inventory stock magnitude so "inventory turnover" (v1) is not rounded to 0.00.
--
-- Inventory turnover endpoint computes:
--   active_items / (total_stock + 1) * 100
-- With the bulk seed (~300 items) total_stock can be large enough that the result is < 0.01 and rounds to 0.00.
-- This migration reduces stock levels to a moderate SMB profile while keeping them non-zero.

-- Scale down stock for bulk-seeded items while preserving a minimum of 1.
-- (Curated SKUs in V2/V6 already have reasonable stock; this mainly affects the bulk SKUs.)
UPDATE products.items
SET stock = GREATEST(1, LEAST(stock, 60) / 3)
WHERE sku LIKE 'SKU-%';
