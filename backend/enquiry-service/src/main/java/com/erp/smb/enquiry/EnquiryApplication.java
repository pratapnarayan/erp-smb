package com.erp.smb.enquiry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.erp.smb.enquiry", "com.erp.smb.common"})
public class EnquiryApplication { public static void main(String[] args){ SpringApplication.run(EnquiryApplication.class,args);} }
