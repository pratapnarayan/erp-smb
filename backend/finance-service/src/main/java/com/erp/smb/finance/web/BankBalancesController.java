package com.erp.smb.finance.web;

import com.erp.smb.finance.service.FinanceKpiService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/finance/bank-balances")
public class BankBalancesController {

  private final FinanceKpiService financeKpiService;

  public BankBalancesController(FinanceKpiService financeKpiService) {
    this.financeKpiService = financeKpiService;
  }

  @GetMapping
  public Map<String, BigDecimal> getBankBalances() {
    return financeKpiService.getBankBalances();
  }
}
