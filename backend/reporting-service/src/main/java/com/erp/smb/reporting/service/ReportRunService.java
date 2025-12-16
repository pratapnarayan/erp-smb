package com.erp.smb.reporting.service;

import com.erp.smb.reporting.domain.ReportRun;

public interface ReportRunService {
    ReportRun queueRun(String definitionCode, String tenantId, String requestedBy, String paramsJson, String format);
    ReportRun getRun(Long id);
}
