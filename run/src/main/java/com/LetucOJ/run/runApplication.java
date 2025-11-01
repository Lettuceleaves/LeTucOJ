package com.LetucOJ.run;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static java.lang.Thread.sleep;

@SpringBootApplication(
        scanBasePackages = {"com.LetucOJ.run", "com.LetucOJ.common"}
)
@MapperScan(basePackages = {"com.LetucOJ.common.anno"})
public class runApplication {

    public static void main(String[] args) {
        SpringApplication.run(runApplication.class, args);
        System.out.println("runApplication starts successfully!");
    }
}
