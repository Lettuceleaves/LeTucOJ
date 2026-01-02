package com.LetucOJ.run;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
        scanBasePackages = {"com.LetucOJ.run", "com.LetucOJ.common"}
)
@MapperScan(basePackages = {"com.LetucOJ.common.anno"})
public class runApplication {

    public static void main(String[] args) {
        SpringApplication.run(runApplication.class, args);
    }
}
