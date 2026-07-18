package com.erp.smb.hrms.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "payroll", schema = "hrms")
public class Payroll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String employee;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private BigDecimal gross;

    @Column(nullable = false)
    private BigDecimal deductions;

    @Column(nullable = false)
    private BigDecimal net;

    /**
     * Stored as "pay_status" in DB to avoid ambiguity; serialized as "status"
     * in JSON via the standard getter name convention (getStatus → "status").
     */
    @Column(name = "pay_status", nullable = false)
    private String status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmployee() { return employee; }
    public void setEmployee(String employee) { this.employee = employee; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public BigDecimal getGross() { return gross; }
    public void setGross(BigDecimal gross) { this.gross = gross; }

    public BigDecimal getDeductions() { return deductions; }
    public void setDeductions(BigDecimal deductions) { this.deductions = deductions; }

    public BigDecimal getNet() { return net; }
    public void setNet(BigDecimal net) { this.net = net; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
