package com.erp.smb.enquiry.repo;

import com.erp.smb.enquiry.domain.Enquiry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnquiryRepository extends JpaRepository<Enquiry, Long> {}
