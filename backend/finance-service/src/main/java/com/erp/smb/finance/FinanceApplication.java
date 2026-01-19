package com.erp.smb.finance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.erp.smb.finance", "com.erp.smb.common"})
public class FinanceApplication {
    public static void main(String[] args){
        SpringApplication.run(FinanceApplication.class,args);
    }
}
