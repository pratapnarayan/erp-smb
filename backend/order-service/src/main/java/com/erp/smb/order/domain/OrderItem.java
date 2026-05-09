package com.erp.smb.order.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items", schema = "orders")
public class OrderItem {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "order_id", nullable = false)
  private Long orderId;

  @Column(nullable = false)
  private String product;

  private String sku;

  @Column(nullable = false)
  private int qty;

  @Column(name = "unit_price", nullable = false)
  private BigDecimal unitPrice;

  private BigDecimal total;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Long getOrderId() { return orderId; }
  public void setOrderId(Long orderId) { this.orderId = orderId; }
  public String getProduct() { return product; }
  public void setProduct(String product) { this.product = product; }
  public String getSku() { return sku; }
  public void setSku(String sku) { this.sku = sku; }
  public int getQty() { return qty; }
  public void setQty(int qty) { this.qty = qty; }
  public BigDecimal getUnitPrice() { return unitPrice; }
  public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
  public BigDecimal getTotal() { return total; }
  public void setTotal(BigDecimal total) { this.total = total; }
}
