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
        // 启动 Spring Boot 应用
        ApplicationContext context = SpringApplication.run(PracticeApplication.class, args);

        // 获取 MinioClient Bean
        MinioConfig minioConfig = context.getBean(MinioConfig.class);

        // 打印 MinioClient 配置信息（例如：打印客户端的端点）
        System.out.println("MinIO Client Endpoint: " + minioConfig.getEndpoint());
    }

}
