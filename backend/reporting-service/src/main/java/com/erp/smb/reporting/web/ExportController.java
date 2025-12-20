package com.erp.smb.reporting.web;

import com.erp.smb.reporting.domain.ReportExport;
import com.erp.smb.reporting.repo.ReportExportRepository;
import com.erp.smb.reporting.repo.ReportRunRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

@RestController
@RequestMapping("/v1/reports")
@RequiredArgsConstructor
public class ExportController {
    private final ReportExportRepository exportRepo;
    private final ReportRunRepository runRepo;

    // List exports for a given run
    @GetMapping("/runs/{runId}/exports")
    public ResponseEntity<java.util.List<ReportExport>> listByRun(@PathVariable Long runId, jakarta.servlet.http.HttpServletRequest request) {
        String tenantId = com.erp.smb.reporting.util.TenantUtils.getTenantId(request);
        var run = runRepo.findById(runId).orElse(null);
        if (run == null || (tenantId != null && !tenantId.equals(run.getTenantId()))) {
            return ResponseEntity.status(404).build();
        }
        var list = exportRepo.findByRunId(runId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/exports/{id}/download")
    public ResponseEntity<FileSystemResource> download(@PathVariable Long id, jakarta.servlet.http.HttpServletRequest request) {
        ReportExport ex = exportRepo.findById(id).orElseThrow();
        var run = runRepo.findById(ex.getRun().getId()).orElseThrow();
        String tenantId = com.erp.smb.reporting.util.TenantUtils.getTenantId(request);
        if (tenantId == null || !tenantId.equals(run.getTenantId())) {
            return ResponseEntity.status(403).build();
        }
        File file = new File(ex.getFilePath());
        if (!file.exists()) return ResponseEntity.notFound().build();
        FileSystemResource res = new FileSystemResource(file);
        String filename = file.getName();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(res);
    }
}
