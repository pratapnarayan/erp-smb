package com.erp.smb.hrms.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "employees", schema = "hrms")
public class Employee {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String name;
  private String role;
  private String dept;
  private String status;
  public Long getId(){return id;} public void setId(Long id){this.id=id;}
  public String getName(){return name;} public void setName(String name){this.name=name;}
  public String getRole(){return role;} public void setRole(String role){this.role=role;}
  public String getDept(){return dept;} public void setDept(String dept){this.dept=dept;}
  public String getStatus(){return status;} public void setStatus(String status){this.status=status;}
}
