package com.erp.smb.reporting.web;

import com.erp.smb.reporting.domain.ReportDefinition;
import com.erp.smb.reporting.service.MetricsService;
import com.erp.smb.reporting.service.ReportDefinitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/reports")
@RequiredArgsConstructor
public class ReportsController {

    private final ReportDefinitionService definitionService;
    private final MetricsService metricsService;

    @GetMapping("/definitions")
    public ResponseEntity<List<ReportDefinition>> getDefinitions(@RequestParam(required = false) String category) {
        return ResponseEntity.ok(definitionService.getActiveDefinitions(category));
    }

    @GetMapping("/metrics")
    public ResponseEntity<List<Map<String, Object>>> metrics(@RequestParam(defaultValue = "month") String period,
                                                             Principal principal) {
        String tenantId = "tenant"; // TODO: extract from JWT claims
        return ResponseEntity.ok(metricsService.getDashboardMetrics(tenantId, period));
    }
}
