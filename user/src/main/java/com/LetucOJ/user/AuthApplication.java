package com.LetucOJ.user;

import lombok.AllArgsConstructor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication(
        scanBasePackages = {"com.LetucOJ.user", "com.LetucOJ.common"}
)
@MapperScan(basePackages = {"com.LetucOJ.user.repos", "com.LetucOJ.common.anno"})
@AllArgsConstructor
public class AuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}
