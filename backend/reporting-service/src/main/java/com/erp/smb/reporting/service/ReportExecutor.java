package com.erp.smb.reporting.service;

import com.erp.smb.reporting.config.ReportProperties;
import com.erp.smb.reporting.domain.ReportDefinition;
import com.erp.smb.reporting.domain.ReportExport;
import com.erp.smb.reporting.domain.ReportRun;
import com.erp.smb.reporting.repo.ReportExportRepository;
import com.erp.smb.reporting.repo.ReportRunRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class ReportExecutor {
    private final ReportRunRepository runRepo;
    private final ReportExportRepository exportRepo;
    private final ReportProperties props;
    private final CsvExportService csvExportService;
    private final ReportSqlBuilder sqlBuilder = new ReportSqlBuilder();

    @Async
    @Transactional
    public void executeAsync(Long runId) {
        ReportRun run = runRepo.findById(runId).orElseThrow();
        try {
            run.setStatus("running");
            run.setStartedAt(OffsetDateTime.now());
            runRepo.save(run);

            ReportDefinition def = run.getDefinition();
            ReportSqlBuilder.SqlAndParams sp = sqlBuilder.build(def.getCode(), run.getParamsJson(), run.getTenantId());
            Path file = buildFilePath(run, def.getCode(), run.getFormat());

            long rows;
            if ("CSV".equalsIgnoreCase(run.getFormat())) {
                rows = csvExportService.exportQueryToCsv(sp.sql(), sp.params(), file, props.getMaxExportRows());
            } else {
                // TODO: XLSX/PDF in Phase 2
                rows = csvExportService.exportQueryToCsv(sp.sql(), sp.params(), file, props.getMaxExportRows());
            }

            run.setRowCount(rows);
            run.setStatus("completed");
            run.setCompletedAt(OffsetDateTime.now());
            runRepo.save(run);

            ReportExport ex = ReportExport.builder()
                    .run(run)
                    .format(run.getFormat())
                    .filePath(file.toString())
                    .fileSizeBytes(java.nio.file.Files.size(file))
                    .createdAt(OffsetDateTime.now())
                    .build();
            exportRepo.save(ex);
        } catch (Exception e) {
            run.setStatus("failed");
            run.setErrorMessage(e.getMessage());
            run.setCompletedAt(OffsetDateTime.now());
            runRepo.save(run);
        }
    }

    private Path buildFilePath(ReportRun run, String code, String format) {
        String yyyy = DateTimeFormatter.ofPattern("yyyy").format(OffsetDateTime.now());
        String mm = DateTimeFormatter.ofPattern("MM").format(OffsetDateTime.now());
        String fname = run.getId() + "." + format.toLowerCase();
        return Path.of(props.getStorageDir(), run.getTenantId(), yyyy, mm, fname);
    }
}
