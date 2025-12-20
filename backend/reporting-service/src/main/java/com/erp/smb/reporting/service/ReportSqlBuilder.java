package com.erp.smb.reporting.service;

import com.fasterxml.jackson.databind.JsonNode;

public class ReportSqlBuilder {

    public record SqlAndParams(String sql, Object[] params) {}

    public SqlAndParams build(String code, JsonNode params, String tenantId) {
        if (params == null) {
            params = com.fasterxml.jackson.databind.node.JsonNodeFactory.instance.objectNode();
        }
        return switch (code) {
            case "sales_performance_monthly" -> buildSalesMonthly(params, tenantId);
            case "top_products_by_revenue" -> buildTopProducts(params, tenantId);
            case "sales_by_region" -> buildSalesByRegion(params, tenantId);
            case "salesperson_performance" -> buildSalesperson(params, tenantId);
            default -> new SqlAndParams("select 1 as no_data where 1=0", new Object[]{});
        };
    }

    private SqlAndParams buildSalesMonthly(JsonNode p, String tenant) {
        java.time.LocalDate from = getDate(p, "dateFrom", java.time.LocalDate.now().minusMonths(1));
        java.time.LocalDate to = getDate(p, "dateTo", java.time.LocalDate.now());
        String sql = "select date, sum(revenue) as revenue, sum(orders_count) as orders, sum(qty) as qty from reporting.sales_daily_summary where tenant_id = ? and date between ? and ? group by date order by date";
        return new SqlAndParams(sql, new Object[]{tenant, java.sql.Date.valueOf(from), java.sql.Date.valueOf(to)});
    }

    private SqlAndParams buildTopProducts(JsonNode p, String tenant) {
        java.time.LocalDate from = getDate(p, "dateFrom", java.time.LocalDate.now().minusMonths(1));
        java.time.LocalDate to = getDate(p, "dateTo", java.time.LocalDate.now());
        int limit = getInt(p, "limit", 10);
        String sql = "select product_id, sum(revenue) as revenue, sum(qty) as qty from reporting.sales_order_facts where tenant_id = ? and order_date between ? and ? group by product_id order by revenue desc limit ?";
        return new SqlAndParams(sql, new Object[]{tenant, java.sql.Date.valueOf(from), java.sql.Date.valueOf(to), limit});
    }

    private SqlAndParams buildSalesByRegion(JsonNode p, String tenant) {
        java.time.LocalDate from = getDate(p, "dateFrom", java.time.LocalDate.now().minusMonths(1));
        java.time.LocalDate to = getDate(p, "dateTo", java.time.LocalDate.now());
        String sql = "select region_id, sum(revenue) as revenue, sum(qty) as qty from reporting.sales_order_facts where tenant_id = ? and order_date between ? and ? group by region_id order by revenue desc";
        return new SqlAndParams(sql, new Object[]{tenant, java.sql.Date.valueOf(from), java.sql.Date.valueOf(to)});
    }

    private SqlAndParams buildSalesperson(JsonNode p, String tenant) {
        java.time.LocalDate from = getDate(p, "dateFrom", java.time.LocalDate.now().minusMonths(1));
        java.time.LocalDate to = getDate(p, "dateTo", java.time.LocalDate.now());
        String sql = "select salesperson_id, sum(revenue) as revenue, sum(qty) as qty, count(distinct order_id) as orders from reporting.sales_order_facts where tenant_id = ? and order_date between ? and ? group by salesperson_id order by revenue desc";
        return new SqlAndParams(sql, new Object[]{tenant, java.sql.Date.valueOf(from), java.sql.Date.valueOf(to)});
    }

    private java.time.LocalDate getDate(JsonNode p, String name, java.time.LocalDate def) {
        if (p.hasNonNull(name)) return java.time.LocalDate.parse(p.get(name).asText());
        return def;
    }
    private int getInt(JsonNode p, String name, int def) {
        if (p.hasNonNull(name)) return p.get(name).asInt();
        return def;
    }
}
