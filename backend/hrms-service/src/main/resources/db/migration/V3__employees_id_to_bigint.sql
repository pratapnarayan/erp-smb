SET search_path TO hrms;
ALTER TABLE IF EXISTS employees
    ALTER COLUMN id TYPE bigint;
