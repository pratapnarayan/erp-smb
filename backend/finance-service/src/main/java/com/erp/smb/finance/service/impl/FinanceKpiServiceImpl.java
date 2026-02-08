package com.erp.smb.finance.service.impl;

import com.erp.smb.finance.repo.TransactionRepository;
import com.erp.smb.finance.service.FinanceKpiService;
import com.erp.smb.finance.web.dto.KpiMetric;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

@Service
public class FinanceKpiServiceImpl implements FinanceKpiService {

  private final TransactionRepository transactionRepository;
  private final RestTemplate restTemplate;
  private final Clock clock;

  private final String ordersServiceBaseUrl;
  private final String productServiceBaseUrl;

  public FinanceKpiServiceImpl(
      TransactionRepository transactionRepository,
      RestTemplate restTemplate,
      @Value("${app.services.orders-base-url:http://order-service:8084}") String ordersServiceBaseUrl,
      @Value("${app.services.products-base-url:http://product-service:8083}") String productServiceBaseUrl,
      Clock clock
  ) {
    this.transactionRepository = transactionRepository;
    this.restTemplate = restTemplate;
    this.ordersServiceBaseUrl = trimTrailingSlash(ordersServiceBaseUrl);
    this.productServiceBaseUrl = trimTrailingSlash(productServiceBaseUrl);
    this.clock = clock;
  }

  @Override
  public Map<String, KpiMetric> getKpis() {
    Map<String, KpiMetric> result = new HashMap<>();

    // ---- MRR (v1) ----
    // Logic: Sum of all CREDIT transactions in current calendar month. Optional filter: account = 'AR'.
    // For SMB v1 this is a pragmatic proxy for "monthly revenue".
    YearMonth currentYm = YearMonth.now(clock);
    YearMonth prevYm = currentYm.minusMonths(1);

    BigDecimal currentMrr = transactionRepository.sumCredits(currentYm.atDay(1), currentYm.plusMonths(1).atDay(1), "AR");
    BigDecimal prevMrr = transactionRepository.sumCredits(prevYm.atDay(1), prevYm.plusMonths(1).atDay(1), "AR");
    result.put("mrr", metric("MRR", currentMrr, prevMrr));

    // ---- AR Overdue (v1) ----
    // Logic: Sum of CREDIT AR transactions older than 30 days.
    LocalDate today = LocalDate.now(clock);
    LocalDate cutoffCurrent = today.minusDays(30);
    LocalDate cutoffPrev = cutoffCurrent.minusDays(30);

    BigDecimal currentArOverdue = transactionRepository.sumArCreditsBefore(cutoffCurrent);
    BigDecimal prevArOverdue = transactionRepository.sumArCreditsBefore(cutoffPrev);
    result.put("arOverdue", metric("AR Overdue", currentArOverdue, prevArOverdue));

    // ---- Orders count (cross-service) ----
    // Current period vs previous period: current calendar month count vs previous calendar month count.
    BigDecimal ordersCount = safeGetBigDecimal(
        ordersServiceBaseUrl + "/api/orders/count?from=" + currentYm.atDay(1) + "&to=" + currentYm.atEndOfMonth(),
        BigDecimal.ZERO);
    BigDecimal prevOrdersCount = safeGetBigDecimal(
        ordersServiceBaseUrl + "/api/orders/count?from=" + prevYm.atDay(1) + "&to=" + prevYm.atEndOfMonth(),
        BigDecimal.ZERO);
    result.put("orders", metric("Orders", ordersCount, prevOrdersCount));

    // ---- Inventory Turnover (cross-service) ----
    // v1 endpoint does not provide historical turnover, so delta is flat.
    BigDecimal invTurnover = safeGetBigDecimal(productServiceBaseUrl + "/api/products/inventory/turnover", BigDecimal.ZERO);
    result.put("inventoryTurnover", metric("Inventory Turnover", invTurnover, invTurnover));

    return result;
  }

  @Override
  public Map<String, BigDecimal> getBankBalances() {
    // Logic (v1): group by account, sum amount per account.
    // Mapping: Cash -> operating, Savings -> savings.
    BigDecimal operating = BigDecimal.ZERO;
    BigDecimal savings = BigDecimal.ZERO;

    for (var row : transactionRepository.sumAmountByAccount()) {
      if (row.getAccount() == null) continue;
      String acc = row.getAccount().trim();
      if (acc.equalsIgnoreCase("Cash")) operating = nz(row.getBalance());
      if (acc.equalsIgnoreCase("Savings")) savings = nz(row.getBalance());
    }

    return Map.of(
        "operating", operating,
        "savings", savings
    );
  }

  private KpiMetric metric(String label, BigDecimal current, BigDecimal previous) {
    BigDecimal delta = percentDelta(current, previous);
    String trend = trend(delta);
    return new KpiMetric(label, nz(current), delta, trend);
  }

  private BigDecimal percentDelta(BigDecimal current, BigDecimal previous) {
    current = nz(current);
    previous = nz(previous);
    if (previous.compareTo(BigDecimal.ZERO) == 0) {
      return BigDecimal.ZERO;
    }
    // ((current - previous) / previous) * 100
    return current.subtract(previous)
        .divide(previous, 4, RoundingMode.HALF_UP)
        .multiply(BigDecimal.valueOf(100))
        .setScale(2, RoundingMode.HALF_UP);
  }

  private String trend(BigDecimal delta) {
    int cmp = nz(delta).compareTo(BigDecimal.ZERO);
    if (cmp > 0) return "up";
    if (cmp < 0) return "down";
    return "flat";
  }

  private BigDecimal safeGetBigDecimal(String url, BigDecimal fallback) {
    try {
      Object v = restTemplate.getForObject(url, Object.class);
      if (v == null) return fallback;
      if (v instanceof Number n) {
        // Prefer integer/long exactness when possible.
        if (n instanceof Long || n instanceof Integer || n instanceof Short || n instanceof Byte) {
          return BigDecimal.valueOf(n.longValue());
        }
        return BigDecimal.valueOf(n.doubleValue());
      }
      return new BigDecimal(String.valueOf(v));
    } catch (Exception ex) {
      return fallback;
    }
  }

  private BigDecimal nz(BigDecimal v) {
    return v == null ? BigDecimal.ZERO : v;
  }

  private String trimTrailingSlash(String s) {
    if (s == null) return "";
    return s.endsWith("/") ? s.substring(0, s.length() - 1) : s;
  }
}
