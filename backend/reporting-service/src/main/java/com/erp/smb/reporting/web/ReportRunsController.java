package com.erp.smb.reporting.web;

import com.erp.smb.reporting.domain.ReportRun;
import com.erp.smb.reporting.service.ReportRunService;
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
    public ResponseEntity<RunResponse> run(@RequestBody RunRequest request, Principal principal) {
        String tenantId = "tenant"; // TODO: extract from JWT
        String user = principal != null ? principal.getName() : "system";
        ReportRun run = reportRunService.queueRun(request.getDefinitionCode(), tenantId, user, request.getParamsJson(), request.getFormat());
        RunResponse resp = new RunResponse();
        resp.setRunId(run.getId());
        resp.setStatus(run.getStatus());
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/runs/{id}")
    public ResponseEntity<ReportRun> get(@PathVariable Long id) {
        return ResponseEntity.ok(reportRunService.getRun(id));
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
