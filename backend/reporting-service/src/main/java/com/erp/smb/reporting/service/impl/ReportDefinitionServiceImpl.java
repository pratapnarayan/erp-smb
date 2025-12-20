package com.erp.smb.reporting.service.impl;

import com.erp.smb.reporting.domain.ReportDefinition;
import com.erp.smb.reporting.repo.ReportDefinitionRepository;
import com.erp.smb.reporting.service.ReportDefinitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportDefinitionServiceImpl implements ReportDefinitionService {

    private final ReportDefinitionRepository repository;

    @Override
    public List<ReportDefinition> getActiveDefinitions(String category) {
        List<ReportDefinition> all = repository.findAll()
                .stream()
                .filter(ReportDefinition::isActive)
                .toList();
        if (category == null || category.isBlank()) return all;
        String c = category.toLowerCase();
        return all.stream().filter(d -> d.getCategory() != null && d.getCategory().equalsIgnoreCase(c)).toList();
    }
}
