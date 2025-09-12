package com.LetucOJ.sys.repos.impl;

import com.LetucOJ.sys.repos.MinioRepos;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.MinioException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Repository
public class MinioReposImpl implements MinioRepos {

    @Autowired
    private MinioClient minioClient;

    /**
     * 按对象名读取文本内容
     *
     * @param objectName 对象名
     * @param bucketName 桶名
     * @return 文本内容字符串
     */
    @Override
    public String get(String objectName, String bucketName) {
        if (!checkExistFile(objectName, bucketName)) {
            throw new RuntimeException("File does not exist: " + objectName);
        }

        try (InputStream in = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build())) {
            return new String(in.readAllBytes());
        } catch (Exception e) {
            throw new RuntimeException("Failed to get object: " + e.getMessage(), e);
        }
    }

    /**
     * 上传或覆盖对象
     *
     * @param objectName 对象名
     * @param bucketName 桶名
     * @param data    文本内容
     * @param mimeType   MIME 类型（如 text/plain）
     * @return 成功返回 null，失败返回错误信息
     */
    public String put(String objectName, String bucketName, byte[] data, String mimeType) {
        try {
            // 1. 桶不存在就创建
            boolean exists = minioClient.bucketExists(
                    io.minio.BucketExistsArgs.builder().bucket(bucketName).build());
            if (!exists) {
                minioClient.makeBucket(
                        io.minio.MakeBucketArgs.builder().bucket(bucketName).build());
            }

            // 2. 上传对象
            try (ByteArrayInputStream in = new ByteArrayInputStream(data)) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(objectName)
                                .stream(in, data.length, -1)
                                .contentType(mimeType == null ? "application/octet-stream" : mimeType)
                                .build());
            }
            return null;           // 成功
        } catch (Exception e) {
            return "Failed to put object: " + e.getMessage();
        }
    }


    /**
     * 判断对象是否存在
     *
     * @param objectName 对象名
     * @param bucketName 桶名
     * @return true=存在，false=不存在
     */
    private boolean checkExistFile(String objectName, String bucketName) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build());
            return true;           // 文件存在
        } catch (ErrorResponseException e) {   // 这里精准捕获
            String err = e.errorResponse().code();
            if ("NoSuchKey".equals(err) || "NotFound".equals(err)) {
                return false;      // 文件不存在
            }
            throw new RuntimeException("Minio error: " + err, e);
        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Error checking file existence", e);
        }
    }
}
