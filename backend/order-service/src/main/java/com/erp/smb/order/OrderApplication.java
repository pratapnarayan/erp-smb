package com.erp.smb.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.erp.smb.order", "com.erp.smb.common"})
public class OrderApplication { public static void main(String[] args){ SpringApplication.run(OrderApplication.class,args);} }
