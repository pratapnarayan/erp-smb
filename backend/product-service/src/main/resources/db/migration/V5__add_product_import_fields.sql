-- Add new fields to items table for data import feature
ALTER TABLE products.items
  ADD COLUMN category VARCHAR(100),
  ADD COLUMN unit VARCHAR(50),
  ADD COLUMN cost_price DECIMAL(15,2),
  ADD COLUMN selling_price DECIMAL(15,2),
  ADD COLUMN gst_rate DECIMAL(5,2);

-- Add check constraint for GST rate (0-28%)
ALTER TABLE products.items
  ADD CONSTRAINT check_gst_rate CHECK (gst_rate IS NULL OR (gst_rate >= 0 AND gst_rate <= 28));
