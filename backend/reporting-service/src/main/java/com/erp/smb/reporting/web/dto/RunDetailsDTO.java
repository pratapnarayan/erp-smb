package com.erp.smb.reporting.web.dto;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
public class RunDetailsDTO {
    private Long id;
    private String definitionCode;
    private String status;
    private OffsetDateTime requestedAt;
    private OffsetDateTime startedAt;
    private OffsetDateTime completedAt;
    private Long rowCount;
    private List<ExportDTO> exports;

    @Data
    public static class ExportDTO {
        private Long id;
        private String format;
        private Long sizeBytes;
        private String downloadPath;
    }
}
