package com.erp.smb.reporting.service;

import com.erp.smb.reporting.domain.ReportRun;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReportRunService {
    ReportRun queueRun(String definitionCode, String tenantId, String requestedBy, String paramsJson, String format);
    ReportRun getRun(Long id);
    Page<ReportRun> listRuns(String tenantId, Pageable pageable);
}
