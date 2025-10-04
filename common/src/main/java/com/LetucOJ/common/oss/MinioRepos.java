package com.LetucOJ.common.oss;

public interface MinioRepos {
    byte[] getFile(String bucketName, String objectName);
    void addFile(String bucketName, String objectName, byte[] data);
}