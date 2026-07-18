package com.erp.smb.hrms.repo;

import com.erp.smb.hrms.domain.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
}
