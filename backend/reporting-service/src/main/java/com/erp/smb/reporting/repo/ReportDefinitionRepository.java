package com.erp.smb.reporting.repo;

import com.erp.smb.reporting.domain.ReportDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReportDefinitionRepository extends JpaRepository<ReportDefinition, Long> {
    Optional<ReportDefinition> findByCodeAndActiveTrue(String code);
}
