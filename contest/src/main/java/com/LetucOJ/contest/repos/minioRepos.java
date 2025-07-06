package com.LetucOJ.contest.repos;

import io.minio.errors.MinioException;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public interface minioRepos {
    byte[] downloadFileAsMultipartFile(String bucketName, String objectName) throws IOException, MinioException, NoSuchAlgorithmException, InvalidKeyException;
}
