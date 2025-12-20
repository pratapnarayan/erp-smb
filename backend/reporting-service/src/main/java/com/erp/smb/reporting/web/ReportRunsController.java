package com.erp.smb.reporting.web;

import com.erp.smb.reporting.domain.ReportRun;
import com.erp.smb.reporting.service.ReportRunService;
import com.erp.smb.reporting.web.dto.RunDetailsDTO;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/v1/reports")
@RequiredArgsConstructor
public class ReportRunsController {

    private final ReportRunService reportRunService;

    @PostMapping("/run")
    public ResponseEntity<RunResponse> run(@RequestBody RunRequest requestBody,
                                           Principal principal,
                                           jakarta.servlet.http.HttpServletRequest request) {
        String tenantId = com.erp.smb.reporting.util.TenantUtils.getTenantId(request);
        String user = principal != null ? principal.getName() : "system";
        ReportRun run = reportRunService.queueRun(requestBody.getDefinitionCode(), tenantId, user, requestBody.getParamsJson(), requestBody.getFormat());
        RunResponse resp = new RunResponse();
        resp.setRunId(run.getId());
        resp.setStatus(run.getStatus());
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/runs/{id}")
    public ResponseEntity<RunDetailsDTO> get(@PathVariable Long id) {
        var run = reportRunService.getRun(id);
        var dto = new com.erp.smb.reporting.web.dto.RunDetailsDTO();
        dto.setId(run.getId());
        dto.setDefinitionCode(run.getDefinition().getCode());
        dto.setStatus(run.getStatus());
        dto.setRequestedAt(run.getRequestedAt());
        dto.setStartedAt(run.getStartedAt());
        dto.setCompletedAt(run.getCompletedAt());
        dto.setRowCount(run.getRowCount());
        // load exports
        java.util.List<com.erp.smb.reporting.web.dto.RunDetailsDTO.ExportDTO> exDtos = new java.util.ArrayList<>();
        var exportRepo = com.erp.smb.reporting.SpringContext.getBean(com.erp.smb.reporting.repo.ReportExportRepository.class);
        for (var ex : exportRepo.findByRunId(run.getId())) {
            var e = new com.erp.smb.reporting.web.dto.RunDetailsDTO.ExportDTO();
            e.setId(ex.getId());
            e.setFormat(ex.getFormat());
            e.setSizeBytes(ex.getFileSizeBytes());
            e.setDownloadPath("/v1/reports/exports/" + ex.getId() + "/download");
            exDtos.add(e);
        }
        dto.setExports(exDtos);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/runs")
    public ResponseEntity<Page<ReportRun>> list(@RequestParam(name = "page", defaultValue = "0") int page,
                                                @RequestParam(name = "size", defaultValue = "50") int size,
                                                Principal principal,
                                                jakarta.servlet.http.HttpServletRequest request) {
        String tenantId = com.erp.smb.reporting.util.TenantUtils.getTenantId(request);
        PageRequest pr = PageRequest.of(Math.max(page, 0), Math.max(1, Math.min(size, 200)));
        Page<ReportRun> runs = reportRunService.listRuns(tenantId, pr);
        return ResponseEntity.ok(runs);
    }

    @Data
    public static class RunRequest {
        private String definitionCode;
        private String paramsJson;
        private String format; // CSV|XLSX|PDF
    }

    @Data
    public static class RunResponse {
        private Long runId;
        private String status;
    }
}
