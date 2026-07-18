package com.erp.smb.hrms.repo;

import com.erp.smb.hrms.domain.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    /** Fetch attendance records for a specific date, sorted by employee name. */
    Page<Attendance> findByDateOrderByEmployeeAsc(LocalDate date, Pageable pageable);
}
