package com.erp.smb.hrms.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "leave_requests", schema = "hrms")
public class LeaveRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String employee;

    /** "type" is a valid Java field name; mapped to column "type". */
    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "from_date", nullable = false)
    private LocalDate fromDate;

    @Column(name = "to_date", nullable = false)
    private LocalDate toDate;

    @Column(nullable = false)
    private int days;

    @Column(nullable = false)
    private String status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmployee() { return employee; }
    public void setEmployee(String employee) { this.employee = employee; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    /** Serialized as "from" in JSON to match the frontend column key. */
    @JsonProperty("from")
    public LocalDate getFromDate() { return fromDate; }
    public void setFromDate(LocalDate fromDate) { this.fromDate = fromDate; }

    /** Serialized as "to" in JSON to match the frontend column key. */
    @JsonProperty("to")
    public LocalDate getToDate() { return toDate; }
    public void setToDate(LocalDate toDate) { this.toDate = toDate; }

    public int getDays() { return days; }
    public void setDays(int days) { this.days = days; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
