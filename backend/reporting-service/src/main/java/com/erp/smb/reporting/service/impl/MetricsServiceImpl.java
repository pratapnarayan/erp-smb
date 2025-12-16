package com.erp.smb.reporting.service.impl;

import com.erp.smb.reporting.service.MetricsService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class MetricsServiceImpl implements MetricsService {
    @Override
    public List<Map<String, Object>> getDashboardMetrics(String tenantId, String period) {
        // Placeholder that queries aggregates will be implemented in Phase 1
        return Collections.emptyList();
    }
}
