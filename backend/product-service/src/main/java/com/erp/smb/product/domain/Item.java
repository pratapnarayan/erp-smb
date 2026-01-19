package com.erp.smb.product.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "items", schema = "products")
public class Item {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(nullable = false, unique = true)
  private String sku;
  @Column(nullable = false)
  private String name;
  private int stock;
  private int reorder;
  @Column(nullable = false)
  private String status;
  
  // New fields for import feature
  private String category;
  private String unit;
  
  @Column(precision = 15, scale = 2)
  private BigDecimal costPrice;
  
  @Column(precision = 15, scale = 2)
  private BigDecimal sellingPrice;
  
  @Column(precision = 5, scale = 2)
  private BigDecimal gstRate;
  
  public Long getId(){return id;} public void setId(Long id){this.id=id;}
  public String getSku(){return sku;} public void setSku(String sku){this.sku=sku;}
  public String getName(){return name;} public void setName(String name){this.name=name;}
  public int getStock(){return stock;} public void setStock(int stock){this.stock=stock;}
  public int getReorder(){return reorder;} public void setReorder(int reorder){this.reorder=reorder;}
  public String getStatus(){return status;} public void setStatus(String status){this.status=status;}
  
  public String getCategory(){return category;} public void setCategory(String category){this.category=category;}
  public String getUnit(){return unit;} public void setUnit(String unit){this.unit=unit;}
  public BigDecimal getCostPrice(){return costPrice;} public void setCostPrice(BigDecimal costPrice){this.costPrice=costPrice;}
  public BigDecimal getSellingPrice(){return sellingPrice;} public void setSellingPrice(BigDecimal sellingPrice){this.sellingPrice=sellingPrice;}
  public BigDecimal getGstRate(){return gstRate;} public void setGstRate(BigDecimal gstRate){this.gstRate=gstRate;}
}
