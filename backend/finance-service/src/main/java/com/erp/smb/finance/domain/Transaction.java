package com.erp.smb.finance.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "transactions", schema = "finance")
public class Transaction {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private LocalDate txDate;
  private String account;
  private String txType;
  private BigDecimal amount;
  private String memo;
  public Long getId(){return id;} public void setId(Long id){this.id=id;}
  public LocalDate getTxDate(){return txDate;} public void setTxDate(LocalDate txDate){this.txDate=txDate;}
  public String getAccount(){return account;} public void setAccount(String account){this.account=account;}
  public String getTxType(){return txType;} public void setTxType(String txType){this.txType=txType;}
  public BigDecimal getAmount(){return amount;} public void setAmount(BigDecimal amount){this.amount=amount;}
  public String getMemo(){return memo;} public void setMemo(String memo){this.memo=memo;}
}
