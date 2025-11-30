package com.erp.smb.enquiry.domain;

import jakarta.persistence.*;

@Entity
@Table(name="enquiries", schema="enquiry")
public class Enquiry {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(nullable=false, unique=true)
  private String code;
  private String customer;
  private String channel;
  private String subject;
  private String status;
  public Long getId(){return id;} public void setId(Long id){this.id=id;}
  public String getCode(){return code;} public void setCode(String code){this.code=code;}
  public String getCustomer(){return customer;} public void setCustomer(String customer){this.customer=customer;}
  public String getChannel(){return channel;} public void setChannel(String channel){this.channel=channel;}
  public String getSubject(){return subject;} public void setSubject(String subject){this.subject=subject;}
  public String getStatus(){return status;} public void setStatus(String status){this.status=status;}
}
