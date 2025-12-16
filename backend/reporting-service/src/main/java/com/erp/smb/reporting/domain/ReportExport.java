package com.erp.smb.reporting.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "report_exports")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReportExport {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_run_id", nullable = false)
    private ReportRun run;

    @Column(nullable = false)
    private String format; // CSV|XLSX|PDF

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    private String checksum;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;
}
