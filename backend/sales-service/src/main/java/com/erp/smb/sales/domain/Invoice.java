package com.erp.smb.sales.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "invoices", schema = "sales")
public class Invoice {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(nullable = false, unique = true)
  private String invoiceNo;
  private String customer;
  private String status;
  private LocalDate due;
  private BigDecimal amount;
  public Long getId(){return id;} public void setId(Long id){this.id=id;}
  public String getInvoiceNo(){return invoiceNo;} public void setInvoiceNo(String invoiceNo){this.invoiceNo=invoiceNo;}
  public String getCustomer(){return customer;} public void setCustomer(String customer){this.customer=customer;}
  public String getStatus(){return status;} public void setStatus(String status){this.status=status;}
  public LocalDate getDue(){return due;} public void setDue(LocalDate due){this.due=due;}
  public BigDecimal getAmount(){return amount;} public void setAmount(BigDecimal amount){this.amount=amount;}
}
