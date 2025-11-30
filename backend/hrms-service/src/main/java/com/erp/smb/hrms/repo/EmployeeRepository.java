package com.erp.smb.hrms.repo;

import com.erp.smb.hrms.domain.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {}
