package com.erp.smb.reporting.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "report_runs", schema = "reporting")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReportRun {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_definition_id", nullable = false)
    private ReportDefinition definition;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "requested_by", nullable = false)
    private String requestedBy;

    @Column(name = "requested_at", nullable = false)
    private OffsetDateTime requestedAt;

    @Column(name = "params_json", columnDefinition = "jsonb")
    private String paramsJson;

    @Column(name = "format")
    private String format; // CSV|XLSX|PDF

    @Column(nullable = false)
    private String status; // queued|running|completed|failed

    private OffsetDateTime startedAt;
    private OffsetDateTime completedAt;

    private Long rowCount;

    @Column(name = "error_message", length = 2000)
    private String errorMessage;
}
