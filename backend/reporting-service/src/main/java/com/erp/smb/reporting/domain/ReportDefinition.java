package com.erp.smb.reporting.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "report_definitions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReportDefinition {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String category; // sales, inventory, financial, operational

    @Column(length = 1000)
    private String description;

    @Column(name = "input_schema", columnDefinition = "jsonb")
    private String inputSchema;

    @Column(name = "output_schema", columnDefinition = "jsonb")
    private String outputSchema;

    @Column(name = "is_active")
    private boolean active = true;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
