package com.LetucOJ.practice.repos;

import com.LetucOJ.practice.model.FileDTO;

public interface MinioRepos {
    String getFile(String name, int testCaseNumber, FileDTO.fileType fileType);
    String addFilePair(String problemId, int testCaseNumber, String input, String output);
}
