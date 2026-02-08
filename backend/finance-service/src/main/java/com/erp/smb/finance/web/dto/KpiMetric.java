package com.erp.smb.finance.web.dto;

import java.math.BigDecimal;

/**
 * Standard DTO for dashboard KPIs.
 *
 * API contract note: this is serialized as an object (label/value/delta/trend) and
 * is returned inside a map keyed by KPI id (mrr, orders, arOverdue, inventoryTurnover).
 */
public class KpiMetric {
  private String label;
  private BigDecimal value;
  private BigDecimal delta;
  private String trend;

  public KpiMetric() {}

  public KpiMetric(String label, BigDecimal value, BigDecimal delta, String trend) {
    this.label = label;
    this.value = value;
    this.delta = delta;
    this.trend = trend;
  }

  public String getLabel() { return label; }
  public void setLabel(String label) { this.label = label; }

  public BigDecimal getValue() { return value; }
  public void setValue(BigDecimal value) { this.value = value; }

  public BigDecimal getDelta() { return delta; }
  public void setDelta(BigDecimal delta) { this.delta = delta; }

  public String getTrend() { return trend; }
  public void setTrend(String trend) { this.trend = trend; }
}
