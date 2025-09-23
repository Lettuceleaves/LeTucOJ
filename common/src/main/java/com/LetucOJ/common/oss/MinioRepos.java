package com.LetucOJ.common.oss;

public interface MinioRepos {
    byte[] getFile(String bucketName, String fileName);
    byte[] addFile(String bucketName, String fileName, byte[] data);
}