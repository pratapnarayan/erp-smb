package com.erp.smb.hrms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.erp.smb.hrms", "com.erp.smb.common"})
public class HrmsApplication { public static void main(String[] args){ SpringApplication.run(HrmsApplication.class,args);} }
