package com.LetucOJ.practice.repos;

import com.LetucOJ.practice.model.FileDTO;

public interface MinioRepos {
    String getFile(String problemId, int testCaseNumber, FileDTO.fileType fileType);
}
