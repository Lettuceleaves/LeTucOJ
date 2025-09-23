package com.LetucOJ.common.oss.impl;

import com.LetucOJ.common.oss.MinioRepos;
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

    @Override
    public byte[] getFile(String bucketName, String fileName) {
        return new byte[0];
    }

    @Override
    public byte[] addFile(String bucketName, String fileName, byte[] data) {
        return new byte[0];
    }
}
