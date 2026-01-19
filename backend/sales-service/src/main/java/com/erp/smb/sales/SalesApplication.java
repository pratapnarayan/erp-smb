package com.erp.smb.sales;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.erp.smb.sales", "com.erp.smb.common"})
public class SalesApplication { public static void main(String[] args){ SpringApplication.run(SalesApplication.class,args);} }
