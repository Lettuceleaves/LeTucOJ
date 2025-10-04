package com.LetucOJ.common.config;

import io.minio.MinioClient;
import lombok.Data;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// TODO 统一配置改成yaml文件读取
@Data
@Configuration
public class MinioConfig {
    // 替换为你的MinIO服务器地址、访问密钥和秘密密钥
    String endpoint = "http://minio:9000";
    String accessKey = "ROOTUSER";
    String secretKey = "LETUC0J123";

    @Bean
    public MinioClient minioClient() {

        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
}
