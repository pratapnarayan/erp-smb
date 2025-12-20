SET search_path TO reporting;
ALTER TABLE report_runs ADD COLUMN IF NOT EXISTS format TEXT;
