package com.erp.smb.reporting.service;

import java.util.List;
import java.util.Map;

public interface MetricsService {
    List<Map<String, Object>> getDashboardMetrics(String tenantId, String period);
}
