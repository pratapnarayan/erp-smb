package com.erp.smb.reporting.service.impl;

import com.erp.smb.reporting.domain.ReportDefinition;
import com.erp.smb.reporting.domain.ReportRun;
import com.erp.smb.reporting.repo.ReportDefinitionRepository;
import com.erp.smb.reporting.repo.ReportRunRepository;
import com.erp.smb.reporting.service.ReportExecutor;
import com.erp.smb.reporting.service.ReportRunService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class ReportRunServiceImpl implements ReportRunService {

    private void requireExportPermission(String category) {
        String role = "REPORT_EXPORT_" + (category == null ? "" : category.toUpperCase());
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) {
            throw new org.springframework.security.access.AccessDeniedException("Not authenticated");
        }
        // Allow ROLE_ADMIN to bypass category-specific export permission in local/testing scenarios
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) return;
        boolean hasCategoryPerm = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(role));
        if (!hasCategoryPerm) {
            throw new org.springframework.security.access.AccessDeniedException("Missing permission: " + role);
        }
    }

    private final ReportRunRepository runRepository;
    private final ReportDefinitionRepository definitionRepository;
    private final ReportExecutor executor;

    @Override
    public ReportRun queueRun(String definitionCode, String tenantId, String requestedBy, String paramsJson, String format) {
        ReportDefinition def = definitionRepository.findByCodeAndActiveTrue(definitionCode)
                .orElseThrow(() -> new IllegalArgumentException("Unknown or inactive report: " + definitionCode));
        // simple RBAC by category: require REPORT_EXPORT_<CATEGORY>
        requireExportPermission(def.getCategory());
        ReportRun run = ReportRun.builder()
                .definition(def)
                .tenantId(tenantId)
                .requestedBy(requestedBy)
                .requestedAt(OffsetDateTime.now())
                .paramsJson(paramsJson)
                .format(format)
                .status("queued")
                .build();
        run = runRepository.save(run);
        executor.executeAsync(run.getId());
        return run;
    }

    @Override
    public ReportRun getRun(Long id) {
        return runRepository.findById(id).orElseThrow();
    }

    @Override
    public org.springframework.data.domain.Page<ReportRun> listRuns(String tenantId, org.springframework.data.domain.Pageable pageable) {
        return runRepository.findByTenantIdOrderByRequestedAtDesc(tenantId, pageable);
    }
}
