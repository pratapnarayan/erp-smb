package com.erp.smb.order.web;

import com.erp.smb.order.repo.SalesOrderRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/orders")
public class OrderAggregateController {

  private final SalesOrderRepository repo;

  public OrderAggregateController(SalesOrderRepository repo) {
    this.repo = repo;
  }

  /**
   * Read-only aggregate endpoint for dashboard KPIs.
   *
   * Returns a total count of sales orders. Optional date filter to keep it future-proof.
   * No joins, no business logic.
   */
  @GetMapping("/count")
  public long count(
      @RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
  ) {
    if (from == null && to == null) {
      return repo.count();
    }

    // If only one bound is provided, treat the other as unbounded.
    LocalDate fromDate = from == null ? LocalDate.of(1970, 1, 1) : from;
    LocalDate toDate = to == null ? LocalDate.of(3000, 1, 1) : to;
    return repo.countByOrderDateBetweenInclusive(fromDate, toDate);
  }
}
