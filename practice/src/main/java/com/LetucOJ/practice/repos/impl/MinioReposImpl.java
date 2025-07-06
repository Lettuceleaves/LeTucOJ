package com.LetucOJ.practice.repos.impl;

import com.LetucOJ.practice.repos.MinioRepos;
import com.LetucOJ.practice.model.FileDTO;
import io.minio.*;
import io.minio.errors.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;


@Repository
public class MinioReposImpl implements MinioRepos {

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    public String getFile(String problemId, int testCaseNumber, FileDTO.fileType fileType) {
        String objectName = null;
        if (fileType == FileDTO.fileType.INPUT) {
            objectName = "input/" + testCaseNumber + ".txt";
        } else if (fileType == FileDTO.fileType.OUTPUT) {
            objectName = "output/" + testCaseNumber + ".txt";
        }

        // 检查文件是否存在
        if (checkExistFile(problemId, objectName)) {
            return null;
        }

        // 下载文件内容
        try (InputStream inputStream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(problemId)
                        .object(objectName)
                        .build())) {
            return new String(inputStream.readAllBytes());
        } catch (Exception e) {
            return null;
        }
    }

    private boolean checkExistFile(String bucketName, String objectName) {
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
            if (e.getMessage().contains("NoSuchKey")) {
                return true; // 文件不存在
            }
            e.printStackTrace();
            return true; // 发生其他错误
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
            return true; // 发生其他错误
        }
    }

}
