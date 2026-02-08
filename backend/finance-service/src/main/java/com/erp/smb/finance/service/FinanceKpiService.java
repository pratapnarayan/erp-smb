package com.erp.smb.finance.service;

import com.erp.smb.finance.web.dto.KpiMetric;

import java.math.BigDecimal;
import java.util.Map;

public interface FinanceKpiService {
  Map<String, KpiMetric> getKpis();

  /**
   * Bank account balances used by the dashboard "Bank Accounts" widget.
   * Keys are stable ids used by the frontend.
   */
  Map<String, BigDecimal> getBankBalances();
}
