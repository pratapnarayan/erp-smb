-- Ensure required PostgreSQL extensions are enabled.
-- Some managed Postgres setups restrict CREATE EXTENSION for non-superusers.
-- This migration attempts to enable extensions but will NOT fail if privileges are missing.

DO $$
BEGIN
  BEGIN
    CREATE EXTENSION IF NOT EXISTS pg_trgm;
  EXCEPTION
    WHEN insufficient_privilege THEN
      RAISE NOTICE 'Skipping CREATE EXTENSION pg_trgm due to insufficient privileges';
    WHEN others THEN
      RAISE NOTICE 'Skipping CREATE EXTENSION pg_trgm due to error: %', SQLERRM;
  END;

  BEGIN
    CREATE EXTENSION IF NOT EXISTS btree_gin;
  EXCEPTION
    WHEN insufficient_privilege THEN
      RAISE NOTICE 'Skipping CREATE EXTENSION btree_gin due to insufficient privileges';
    WHEN others THEN
      RAISE NOTICE 'Skipping CREATE EXTENSION btree_gin due to error: %', SQLERRM;
  END;
END $$;
