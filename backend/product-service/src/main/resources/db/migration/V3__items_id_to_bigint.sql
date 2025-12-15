DO $$
BEGIN
  IF EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = 'products'
      AND table_name = 'items'
      AND column_name = 'id'
      AND data_type = 'integer'
  ) THEN
    ALTER TABLE products.items
      ALTER COLUMN id TYPE BIGINT;
  END IF;
END $$;
