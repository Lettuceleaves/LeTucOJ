package com.LetucOJ.contest.repos.impl;

import com.LetucOJ.contest.repos.minioRepos;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.errors.MinioException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;


@Repository
public class minioReposImpl implements minioRepos {

    @Autowired
    private MinioClient minioClient;

    public byte[] downloadFileAsMultipartFile(String bucketName, String objectName) throws IOException, MinioException, NoSuchAlgorithmException, InvalidKeyException {
        // 从MinIO下载文件，获取InputStream
        try (InputStream inputStream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build())) {

            // 将InputStream转换为byte数组
            return inputStream.readAllBytes();
        }
    }

}
