SET search_path TO reporting;

INSERT INTO report_definitions(code, name, category, description, input_schema)
VALUES
('sales_performance_monthly','Monthly Sales Performance','sales','Aggregate revenue and orders per month', '{"fields":[{"name":"dateFrom","type":"date"},{"name":"dateTo","type":"date"},{"name":"regionId","type":"number","required":false}]}'),
('top_products_by_revenue','Top Products by Revenue','sales','Top N products by revenue in period', '{"fields":[{"name":"dateFrom","type":"date"},{"name":"dateTo","type":"date"},{"name":"limit","type":"number","default":10}]}'),
('sales_by_region','Sales by Region','sales','Revenue distribution by region','{"fields":[{"name":"dateFrom","type":"date"},{"name":"dateTo","type":"date"}]}'),
('salesperson_performance','Salesperson Performance','sales','KPIs per salesperson','{"fields":[{"name":"dateFrom","type":"date"},{"name":"dateTo","type":"date"}]}')
ON CONFLICT (code) DO NOTHING;
