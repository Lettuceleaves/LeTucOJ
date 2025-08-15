package com.LetucOJ.contest.repos.impl;

import com.LetucOJ.contest.model.minio.FileDTO;
import com.LetucOJ.contest.repos.MinioRepos;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
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

    private final String bucketName = "letucoj";

    public String getFile(String problemId, int testCaseNumber, FileDTO.fileType fileType) {
        String objectName = null;
        if (fileType == FileDTO.fileType.INPUT) {
            objectName = "problems/" + problemId + "/input/" + testCaseNumber + ".txt";
        } else if (fileType == FileDTO.fileType.OUTPUT) {
            objectName = "problems/" + problemId + "/output/" + testCaseNumber + ".txt";
        }

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
