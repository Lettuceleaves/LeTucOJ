package com.LetucOJ.contest.config;

import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    @Bean
    public MinioClient minioClient() {
        // 替换为你的MinIO服务器地址、访问密钥和秘密密钥
        String endpoint = "http://minio:9000";
        String accessKey = "ROOTUSER";
        String secretKey = "LETUC0J123";

        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
}
