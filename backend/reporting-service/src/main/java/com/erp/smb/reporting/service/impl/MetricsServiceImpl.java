package com.erp.smb.reporting.service.impl;

import com.erp.smb.reporting.service.MetricsService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class MetricsServiceImpl implements MetricsService {
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;
    public MetricsServiceImpl(org.springframework.jdbc.core.JdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }

    @Override
    public List<Map<String, Object>> getDashboardMetrics(String tenantId, String period) {
        if (tenantId == null || tenantId.isBlank()) return List.of();
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate from;
        switch (period == null ? "month" : period) {
            case "week" -> from = today.minusDays(7);
            case "quarter" -> from = today.minusMonths(3);
            case "year" -> from = today.minusYears(1);
            default -> from = today.minusMonths(1);
        }
        String sql = "select coalesce(sum(revenue),0) as revenue, coalesce(sum(orders_count),0) as orders, coalesce(sum(qty),0) as qty from reporting.sales_daily_summary where tenant_id = ? and date >= ?";
        Map<String,Object> row = jdbcTemplate.queryForMap(sql, tenantId, java.sql.Date.valueOf(from));
        return List.of(
                Map.of("label","Total Revenue","value", row.get("revenue"), "change","n/a","trend","neutral"),
                Map.of("label","Total Orders","value", row.get("orders"), "change","n/a","trend","neutral"),
                Map.of("label","Total Quantity","value", row.get("qty"), "change","n/a","trend","neutral")
        );
    }
}
