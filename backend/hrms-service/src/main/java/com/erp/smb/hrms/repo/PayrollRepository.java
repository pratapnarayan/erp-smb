package com.erp.smb.hrms.repo;

import com.erp.smb.hrms.domain.Payroll;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayrollRepository extends JpaRepository<Payroll, Long> {
}
