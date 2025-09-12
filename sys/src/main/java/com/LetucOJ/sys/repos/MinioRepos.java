package com.LetucOJ.sys.repos;

import com.LetucOJ.sys.model.FileDTO;

public interface MinioRepos {
    String get(String objectName, String bucketName);
    String put(String objectName, String bucketName, byte[] content, String mimeType);
}
