CREATE SCHEMA IF NOT EXISTS reporting;
SET search_path TO reporting;

CREATE TABLE IF NOT EXISTS report_definitions (
    id BIGSERIAL PRIMARY KEY,
    code TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    category TEXT NOT NULL,
    description TEXT,
    input_schema JSONB,
    output_schema JSONB,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS report_runs (
    id BIGSERIAL PRIMARY KEY,
    report_definition_id BIGINT NOT NULL REFERENCES report_definitions(id),
    tenant_id TEXT NOT NULL,
    requested_by TEXT NOT NULL,
    requested_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    params_json JSONB,
    status TEXT NOT NULL,
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    row_count BIGINT,
    error_message TEXT
);

CREATE TABLE IF NOT EXISTS report_exports (
    id BIGSERIAL PRIMARY KEY,
    report_run_id BIGINT NOT NULL REFERENCES report_runs(id),
    format TEXT NOT NULL,
    file_path TEXT NOT NULL,
    file_size_bytes BIGINT,
    checksum TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMPTZ
);

-- Sample sales aggregate tables (stubs for future implementation)
CREATE TABLE IF NOT EXISTS sales_order_facts (
    order_id BIGINT PRIMARY KEY,
    tenant_id TEXT NOT NULL,
    order_date DATE NOT NULL,
    customer_id BIGINT,
    product_id BIGINT,
    qty NUMERIC,
    unit_price NUMERIC,
    revenue NUMERIC,
    region_id BIGINT,
    salesperson_id BIGINT,
    status TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS sales_daily_summary (
    date DATE NOT NULL,
    tenant_id TEXT NOT NULL,
    region_id BIGINT,
    product_id BIGINT,
    revenue NUMERIC,
    qty NUMERIC,
    orders_count BIGINT,
    PRIMARY KEY(date, tenant_id, COALESCE(region_id,0), COALESCE(product_id,0))
);

CREATE INDEX IF NOT EXISTS idx_sales_facts_tenant_date ON sales_order_facts(tenant_id, order_date);
CREATE INDEX IF NOT EXISTS idx_runs_tenant_requested ON report_runs(tenant_id, requested_at DESC);
