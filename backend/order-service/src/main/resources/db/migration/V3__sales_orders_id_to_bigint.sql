SET search_path TO orders;
ALTER TABLE IF EXISTS sales_orders
    ALTER COLUMN id TYPE bigint;
