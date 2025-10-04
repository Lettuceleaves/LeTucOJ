package com.LetucOJ.common.oss.impl;

import com.LetucOJ.common.oss.MinioRepos;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.InputStream;


@Repository
public class MinioReposImpl implements MinioRepos {

    @Autowired
    private MinioClient minioClient;

    @Override
    public byte[] getFile(String bucketName, String objectName) {
        try (InputStream inputStream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build())) {
            return inputStream.readAllBytes();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addFile(String bucketName, String objectName, byte[] data) {
        try {

            // 上传文件内容
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(new java.io.ByteArrayInputStream(data), data.length, 5242880)
                            .contentType("text/plain")
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}