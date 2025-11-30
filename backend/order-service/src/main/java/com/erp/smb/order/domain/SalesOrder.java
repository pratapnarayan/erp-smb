package com.erp.smb.order.domain;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "sales_orders", schema = "orders")
public class SalesOrder {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(nullable = false, unique = true)
  private String code;
  private String customer;
  private String status;
  private java.math.BigDecimal total;
  private LocalDate orderDate;
  public Long getId(){return id;} public void setId(Long id){this.id=id;}
  public String getCode(){return code;} public void setCode(String code){this.code=code;}
  public String getCustomer(){return customer;} public void setCustomer(String customer){this.customer=customer;}
  public String getStatus(){return status;} public void setStatus(String status){this.status=status;}
  public java.math.BigDecimal getTotal(){return total;} public void setTotal(java.math.BigDecimal total){this.total=total;}
  public LocalDate getOrderDate(){return orderDate;} public void setOrderDate(LocalDate orderDate){this.orderDate=orderDate;}
}
