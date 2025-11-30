package com.erp.smb.order.repo;

import com.erp.smb.order.domain.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {}
