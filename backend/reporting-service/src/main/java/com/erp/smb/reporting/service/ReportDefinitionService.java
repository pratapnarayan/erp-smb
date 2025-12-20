package com.erp.smb.reporting.service;

import com.erp.smb.reporting.domain.ReportDefinition;
import java.util.List;

public interface ReportDefinitionService {
    List<ReportDefinition> getActiveDefinitions(String category);
}
