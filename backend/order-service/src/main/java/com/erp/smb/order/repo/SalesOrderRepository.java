package com.erp.smb.order.repo;

import com.erp.smb.order.domain.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {

  @Query(value = """
      SELECT COUNT(*)
      FROM orders.sales_orders so
      WHERE so.order_date >= :from
        AND so.order_date <= :to
      """, nativeQuery = true)
  long countByOrderDateBetweenInclusive(@Param("from") LocalDate from, @Param("to") LocalDate to);
}

