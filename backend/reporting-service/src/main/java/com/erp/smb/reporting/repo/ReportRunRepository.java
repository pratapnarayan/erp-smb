package com.erp.smb.reporting.repo;

import com.erp.smb.reporting.domain.ReportRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReportRunRepository extends JpaRepository<ReportRun, Long> {
    Page<ReportRun> findByTenantIdOrderByRequestedAtDesc(String tenantId, Pageable pageable);
}
