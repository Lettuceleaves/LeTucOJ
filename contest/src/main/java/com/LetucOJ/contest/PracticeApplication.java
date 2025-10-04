package com.LetucOJ.contest;

import com.LetucOJ.common.config.MinioConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ApplicationContext;

@SpringBootApplication(
        scanBasePackages = {"com.LetucOJ.contest", "com.LetucOJ.common"}
)
@EnableFeignClients
public class PracticeApplication {

    public static void main(String[] args) {
        SpringApplication.run(PracticeApplication.class, args);
    }

}
