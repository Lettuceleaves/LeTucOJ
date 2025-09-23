package com.LetucOJ.sys;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

import java.net.UnknownHostException;

@SpringBootApplication(
        scanBasePackages = {"com.LetucOJ.sys", "com.LetucOJ.common"}
)
@EnableFeignClients(basePackages = "com.LetucOJ")
public class SysApplication {
    public static void main(String[] args) {
        SpringApplication.run(SysApplication.class, args);
    }
}
