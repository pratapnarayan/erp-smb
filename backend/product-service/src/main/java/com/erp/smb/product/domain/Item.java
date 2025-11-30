package com.erp.smb.product.domain;

import jakarta.persistence.*;

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
  public Long getId(){return id;} public void setId(Long id){this.id=id;}
  public String getSku(){return sku;} public void setSku(String sku){this.sku=sku;}
  public String getName(){return name;} public void setName(String name){this.name=name;}
  public int getStock(){return stock;} public void setStock(int stock){this.stock=stock;}
  public int getReorder(){return reorder;} public void setReorder(int reorder){this.reorder=reorder;}
  public String getStatus(){return status;} public void setStatus(String status){this.status=status;}
}
