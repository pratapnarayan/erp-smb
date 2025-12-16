package com.erp.smb.reporting.service.impl;

import com.erp.smb.reporting.domain.ReportDefinition;
import com.erp.smb.reporting.domain.ReportRun;
import com.erp.smb.reporting.repo.ReportDefinitionRepository;
import com.erp.smb.reporting.repo.ReportRunRepository;
import com.erp.smb.reporting.service.ReportRunService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class ReportRunServiceImpl implements ReportRunService {

    private final ReportRunRepository runRepository;
    private final ReportDefinitionRepository definitionRepository;

    @Override
    public ReportRun queueRun(String definitionCode, String tenantId, String requestedBy, String paramsJson, String format) {
        ReportDefinition def = definitionRepository.findByCodeAndActiveTrue(definitionCode)
                .orElseThrow(() -> new IllegalArgumentException("Unknown or inactive report: " + definitionCode));
        ReportRun run = ReportRun.builder()
                .definition(def)
                .tenantId(tenantId)
                .requestedBy(requestedBy)
                .requestedAt(OffsetDateTime.now())
                .paramsJson(paramsJson)
                .status("queued")
                .build();
        return runRepository.save(run);
    }

    @Override
    public ReportRun getRun(Long id) {
        return runRepository.findById(id).orElseThrow();
    }
}
