SET search_path TO finance;
ALTER TABLE IF EXISTS transactions
    ALTER COLUMN id TYPE bigint;
