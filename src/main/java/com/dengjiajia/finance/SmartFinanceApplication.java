package com.dengjiajia.finance;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.dengjiajia.finance", "com.dengjiajia.smartfinance"})
@MapperScan("com.dengjiajia.finance.mapper")

public class SmartFinanceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartFinanceApplication.class, args);
        System.out.println("项目启动成功...");
    }

}