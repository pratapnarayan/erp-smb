SET search_path TO sales;
ALTER TABLE IF EXISTS invoices
    ALTER COLUMN id TYPE bigint;
