package com.erp.smb.hrms.web;

import com.erp.smb.common.dto.PageResponse;
import com.erp.smb.hrms.domain.Attendance;
import com.erp.smb.hrms.domain.Employee;
import com.erp.smb.hrms.domain.LeaveRequest;
import com.erp.smb.hrms.domain.Payroll;
import com.erp.smb.hrms.repo.AttendanceRepository;
import com.erp.smb.hrms.repo.EmployeeRepository;
import com.erp.smb.hrms.repo.LeaveRequestRepository;
import com.erp.smb.hrms.repo.PayrollRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/hrms")
public class HrmsController {

    private final EmployeeRepository employeeRepo;
    private final LeaveRequestRepository leaveRepo;
    private final AttendanceRepository attendanceRepo;
    private final PayrollRepository payrollRepo;

    public HrmsController(
            EmployeeRepository employeeRepo,
            LeaveRequestRepository leaveRepo,
            AttendanceRepository attendanceRepo,
            PayrollRepository payrollRepo) {
        this.employeeRepo   = employeeRepo;
        this.leaveRepo      = leaveRepo;
        this.attendanceRepo = attendanceRepo;
        this.payrollRepo    = payrollRepo;
    }

    // ── Employee Directory ────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<PageResponse<Employee>> listEmployees(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        var p = employeeRepo.findAll(PageRequest.of(page, size));
        return ResponseEntity.ok(new PageResponse<>(
                p.getContent(), p.getNumber(), p.getSize(),
                p.getTotalElements(), p.getTotalPages()));
    }

    @PostMapping
    public Employee createEmployee(@RequestBody Employee e) {
        return employeeRepo.save(e);
    }

    // ── Leave Requests ────────────────────────────────────────────────────────

    @GetMapping("/leave")
    public ResponseEntity<PageResponse<LeaveRequest>> listLeave(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "50") int size) {
        var p = leaveRepo.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fromDate")));
        return ResponseEntity.ok(new PageResponse<>(
                p.getContent(), p.getNumber(), p.getSize(),
                p.getTotalElements(), p.getTotalPages()));
    }

    // ── Attendance ────────────────────────────────────────────────────────────

    /**
     * Returns attendance records.
     * If the {@code date} query param is supplied (ISO format: 2026-05-24),
     * returns records for that specific day; otherwise returns the most recent
     * day for which records exist (today falling back to yesterday).
     */
    @GetMapping("/attendance")
    public ResponseEntity<PageResponse<Attendance>> listAttendance(
            @RequestParam(name = "date", required = false) String dateParam,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "50") int size) {

        LocalDate target = resolveAttendanceDate(dateParam);
        Page<Attendance> p = attendanceRepo.findByDateOrderByEmployeeAsc(
                target, PageRequest.of(page, size));

        // If no records for today, fall back to yesterday
        if (p.isEmpty() && dateParam == null) {
            p = attendanceRepo.findByDateOrderByEmployeeAsc(
                    target.minusDays(1), PageRequest.of(page, size));
        }

        return ResponseEntity.ok(new PageResponse<>(
                p.getContent(), p.getNumber(), p.getSize(),
                p.getTotalElements(), p.getTotalPages()));
    }

    // ── Payroll ───────────────────────────────────────────────────────────────

    @GetMapping("/payroll")
    public ResponseEntity<PageResponse<Payroll>> listPayroll(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "50") int size) {
        var p = payrollRepo.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "employee")));
        return ResponseEntity.ok(new PageResponse<>(
                p.getContent(), p.getNumber(), p.getSize(),
                p.getTotalElements(), p.getTotalPages()));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private LocalDate resolveAttendanceDate(String dateParam) {
        if (dateParam != null && !dateParam.isBlank()) {
            try {
                return LocalDate.parse(dateParam);
            } catch (Exception ignored) {
                // Fall through to default
            }
        }
        return LocalDate.now();
    }
}
