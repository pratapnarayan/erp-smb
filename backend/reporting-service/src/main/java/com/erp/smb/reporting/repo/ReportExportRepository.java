package com.erp.smb.reporting.repo;

import com.erp.smb.reporting.domain.ReportExport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportExportRepository extends JpaRepository<ReportExport, Long> {
    java.util.List<ReportExport> findByRunIdOrderByCreatedAtDesc(Long runId);

    java.util.List<ReportExport> findByRunId(Long runId);
}
