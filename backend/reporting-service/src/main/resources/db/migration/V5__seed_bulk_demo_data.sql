-- Seed reporting with deterministic definitions, runs, and exports while respecting FKs
SET search_path TO reporting;

-- Insert report_definitions with explicit IDs to ensure FK consistency
INSERT INTO report_definitions (id, code, name, category, description, input_schema, output_schema, is_active, created_at, updated_at)
VALUES
  (1201, 'demo_sales_trends', 'Demo Sales Trends', 'sales', 'Monthly sales trends for demo dataset', '{"fields":[{"name":"from","type":"date"},{"name":"to","type":"date"}]}', NULL, TRUE, TIMESTAMPTZ '2025-06-08 10:00:00+05:30', TIMESTAMPTZ '2025-09-16 10:00:00+05:30'),
  (1202, 'demo_top_customers', 'Demo Top Customers', 'sales', 'Top customers by revenue in demo', '{"fields":[{"name":"from","type":"date"},{"name":"to","type":"date"},{"name":"limit","type":"number","default":10}]}', NULL, TRUE, TIMESTAMPTZ '2025-06-28 10:00:00+05:30', TIMESTAMPTZ '2025-09-26 10:00:00+05:30'),
  (1203, 'demo_finance_summary', 'Demo Finance Summary', 'finance', 'Monthly finance summary demo', '{"fields":[{"name":"month","type":"string"}]}', NULL, TRUE, TIMESTAMPTZ '2025-07-29 10:00:00+05:30', TIMESTAMPTZ '2025-10-26 10:00:00+05:30');

-- Insert report_runs referencing above definition IDs
INSERT INTO report_runs (id, report_definition_id, tenant_id, requested_by, requested_at, params_json, status, started_at, completed_at, row_count, error_message, format)
VALUES
  (1301, 1201, 'demo', 'system', TIMESTAMPTZ '2025-11-25 10:00:00+05:30', '{"from":"2025-01-01","to":"2025-12-31"}', 'COMPLETED', TIMESTAMPTZ '2025-11-25 10:00:00+05:30', TIMESTAMPTZ '2025-11-26 10:00:00+05:30', 300, NULL, 'CSV'),
  (1302, 1202, 'demo', 'system', TIMESTAMPTZ '2025-12-05 10:00:00+05:30', '{"from":"2025-06-01","to":"2025-12-31","limit":10}', 'COMPLETED', TIMESTAMPTZ '2025-12-05 10:00:00+05:30', TIMESTAMPTZ '2025-12-06 10:00:00+05:30', 10, NULL, 'CSV'),
  (1303, 1203, 'demo', 'system', TIMESTAMPTZ '2025-12-15 10:00:00+05:30', '{"month":"2025-11"}', 'FAILED', TIMESTAMPTZ '2025-12-15 10:00:00+05:30', TIMESTAMPTZ '2025-12-15 10:05:00+05:30', 0, 'Timeout while executing', 'CSV');

-- Insert report_exports referencing runs
INSERT INTO report_exports (id, report_run_id, format, file_path, file_size_bytes, checksum, created_at, expires_at)
VALUES
  (1401, 1301, 'CSV', '/data/reports/demo_sales_trends_1301.csv', 20480, 'abc123demo', TIMESTAMPTZ '2025-11-26 10:00:00+05:30', TIMESTAMPTZ '2026-02-23 10:00:00+05:30'),
  (1402, 1302, 'CSV', '/data/reports/demo_top_customers_1302.csv', 10240, 'def456demo', TIMESTAMPTZ '2025-12-06 10:00:00+05:30', TIMESTAMPTZ '2026-02-23 10:00:00+05:30');
