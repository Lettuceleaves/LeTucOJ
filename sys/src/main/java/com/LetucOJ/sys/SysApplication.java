package com.LetucOJ.sys;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(
        scanBasePackages = {"com.LetucOJ.sys", "com.LetucOJ.common"}
)
@MapperScan(basePackages = {"com.LetucOJ.sys.repository", "com.LetucOJ.common.anno"})
@EnableFeignClients(basePackages = "com.LetucOJ")
@EnableScheduling
public class SysApplication {
    public static void main(String[] args) {
        SpringApplication.run(SysApplication.class, args);
    }
}
