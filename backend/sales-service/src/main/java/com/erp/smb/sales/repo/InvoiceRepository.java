package com.erp.smb.sales.repo;

import com.erp.smb.sales.domain.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {}
