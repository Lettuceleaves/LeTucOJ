package com.LetucOJ.sys.repos.impl;

import com.LetucOJ.sys.model.FileDTO;
import com.LetucOJ.sys.repos.MinioRepos;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.errors.MinioException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;


@Repository
public class MinioReposImpl implements MinioRepos {

    @Autowired
    private MinioClient minioClient;

    private final String bucketName = "doc";

    public String get() {
        String objectName = "doc";

        // 检查文件是否存在
        if (checkExistFile(objectName)) {
            throw new RuntimeException("File not exist: " + objectName);
        }

        // 下载文件内容
        try (InputStream inputStream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build())) {
            return new String(inputStream.readAllBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String update(byte[] doc) {
        String objectName = "doc";

        // 检查文件是否已存在
        if (checkExistFile(objectName)) {
            return "File does not exist: " + bucketName + "/" + objectName;
        }

        // 上传文件内容
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(new java.io.ByteArrayInputStream(doc), doc.length, -1)
                            .contentType("text/plain")
                            .build()
            );
            return null;
        } catch (Exception e) {
            return "Error updating file: " + e.getMessage();
        }
    }

    // 新建文件
    private String createFile(String objectName, String content) {
        try {
            // 检查文件是否已存在
            if (!checkExistFile(objectName)) {
                return "File already exists: " + bucketName + "/" + objectName;
            }

            // 上传文件内容
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(new java.io.ByteArrayInputStream(content.getBytes()), content.length(), -1)
                            .contentType("text/plain")
                            .build()
            );
            return null;
        } catch (Exception e) {
            return "Error creating file: " + e.getMessage();
        }
    }

    private boolean checkExistFile(String objectName) {
        try {
            // 检查文件是否存在
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            return false; // 文件存在
        } catch (MinioException e) {
            if (e.getMessage().contains("not exist")) {
                return true; // 文件不存在
            } else {
                e.printStackTrace();
                throw new RuntimeException("Error checking file existence: " + e.getMessage());
            }
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
