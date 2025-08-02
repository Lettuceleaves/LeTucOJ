package com.LetucOJ.practice.repos.impl;

import com.LetucOJ.practice.repos.MinioRepos;
import com.LetucOJ.practice.model.FileDTO;
import io.minio.*;
import io.minio.errors.*;
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

    public String addFilePair(String problemId, int testCaseNumber, String input, String output) {
        String bucketName = "letucoj";
        String inputObjectName = "problems/" + problemId + "/input/" + testCaseNumber + ".txt";
        String outputObjectName = "problems/" + problemId + "/output/" + testCaseNumber + ".txt";

        // 创建输入文件
        String inputResult = createFile(inputObjectName, input);
        if (inputResult != null) {
            return inputResult;
        }

        // 创建输出文件
        String outputResult = createFile(outputObjectName, output);
        if (outputResult != null) {
            return outputResult;
        }

        return null; // 成功添加文件对
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
