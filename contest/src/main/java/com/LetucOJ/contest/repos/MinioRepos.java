package com.LetucOJ.contest.repos;

import com.LetucOJ.contest.model.minio.FileDTO;

public interface MinioRepos {
    String getFile(String name, int testCaseNumber, FileDTO.fileType fileType);
}
