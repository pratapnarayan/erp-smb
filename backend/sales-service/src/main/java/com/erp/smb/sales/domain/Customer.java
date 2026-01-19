package com.erp.smb.sales.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "customers", schema = "sales")
public class Customer {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String customerName;

  private String phone;
  private String email;

  @Column(length = 500)
  private String address;

  private String gstNumber;

  @Column(precision = 15, scale = 2)
  private BigDecimal openingBalance;

  @Column(length = 10)
  private String balanceType; // Credit/Debit

  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(nullable = false)
  private LocalDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }

  // Getters and Setters
  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }

  public String getCustomerName() { return customerName; }
  public void setCustomerName(String customerName) { this.customerName = customerName; }

  public String getPhone() { return phone; }
  public void setPhone(String phone) { this.phone = phone; }

  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }

  public String getAddress() { return address; }
  public void setAddress(String address) { this.address = address; }

  public String getGstNumber() { return gstNumber; }
  public void setGstNumber(String gstNumber) { this.gstNumber = gstNumber; }

  public BigDecimal getOpeningBalance() { return openingBalance; }
  public void setOpeningBalance(BigDecimal openingBalance) { this.openingBalance = openingBalance; }

  public String getBalanceType() { return balanceType; }
  public void setBalanceType(String balanceType) { this.balanceType = balanceType; }

  public LocalDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

  public LocalDateTime getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
