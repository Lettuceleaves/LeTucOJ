package com.LetucOJ.contest.config;

import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class minioConfig {

    @Bean
    public MinioClient minioClient() {
        // 替换为你的MinIO服务器地址、访问密钥和秘密密钥
        String endpoint = "http://127.0.0.1:9005";
        String accessKey = "xwfjM0h8kJpcfTTwQUcE";
        String secretKey = "kDJB1yQSuMYVLbpC9ez1KsFTGr5nJMscUp8diIwy";

        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
}
