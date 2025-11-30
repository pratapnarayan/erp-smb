package com.erp.smb.finance.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/finance/kpis")
public class KpiController {
  @GetMapping
  public Map<String, Object> kpis(){
    return Map.of(
      "mrr", Map.of("label", "MRR", "value", 42980, "delta", 6.2, "trend", "up"),
      "orders", Map.of("label", "Orders", "value", 1284, "delta", 3.1, "trend", "up"),
      "arOverdue", Map.of("label", "AR Overdue", "value", 12450, "delta", -1.2, "trend", "down"),
      "inventoryTurnover", Map.of("label", "Inventory Turnover", "value", 5.2, "delta", 0.4, "trend", "up")
    );
  }
}
