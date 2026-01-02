package com.LetucOJ.practice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(
        scanBasePackages = {"com.LetucOJ.practice", "com.LetucOJ.common"}
)
@MapperScan(basePackages = {"com.LetucOJ.practice.repos", "com.LetucOJ.common.anno"})
@EnableFeignClients
@EnableScheduling
public class PracticeApplication {
    public static void main(String[] args) {
        SpringApplication.run(PracticeApplication.class, args);
    }
}