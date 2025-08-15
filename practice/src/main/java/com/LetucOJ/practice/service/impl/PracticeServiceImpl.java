package com.LetucOJ.practice.service.impl;

import com.LetucOJ.practice.client.RunClient;
import com.LetucOJ.practice.model.*;
import com.LetucOJ.practice.repos.MinioRepos;
import com.LetucOJ.practice.repos.MybatisRepos;
import com.LetucOJ.practice.service.PracticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class PracticeServiceImpl implements PracticeService {

    @Autowired
    private RunClient runClient;

    @Autowired
    private MinioRepos minioRepos;

    @Autowired
    private MybatisRepos mybatisRepos;

    public ResultVO submit(String qname, String code, boolean root) throws Exception {
        try {
            List<String> inputs = new ArrayList<>();
            inputs.add(code);
            String[] inputFiles;

            ProblemStatusDTO problemStatus = mybatisRepos.getStatus(qname);
            System.out.println(problemStatus);
            if (problemStatus == null) {
                return new ResultVO((byte) 5, null, "practice/submit: Problem not found or not available");
            } else if (problemStatus.getCaseAmount() <= 0) {
                return new ResultVO((byte) 5, null, "practice/submit: No test cases available for this problem: " + qname + " " + problemStatus.getCaseAmount());
            } else if (!problemStatus.isPublicProblem() && !root) {
                return new ResultVO((byte) 5, null, "practice/submit: Problem is not available for practice");
            }

            try {
                FileDTO fileDTO = getFile(qname, problemStatus.getCaseAmount(), FileDTO.fileType.INPUT);
                if (fileDTO.getStatus() == 1) {
                    return new ResultVO((byte) 5, null, "practice/submit: TestCase Not Found");
                } else if (fileDTO.getStatus() == 2) {
                    return new ResultVO((byte) 5, null, fileDTO.getFile()[0]);
                } else {
                    inputFiles = fileDTO.getFile();
                }
            } catch (RuntimeException e) {
                return new ResultVO((byte) 5, null, "practice/submit: Error retrieving input files: " + e.getMessage());
            }
            inputs.addAll(Arrays.asList(inputFiles));
            List<String> outputs = new ArrayList<>();
            String[] expectedOutputs;
            try {
                FileDTO outputFileDTO = getFile(qname, problemStatus.getCaseAmount(), FileDTO.fileType.OUTPUT);
                if (outputFileDTO.getStatus() == 1) {
                    return new ResultVO((byte) 5, null, "practice/submit: Output files not found");
                } else if (outputFileDTO.getStatus() == 2) {
                    return new ResultVO((byte) 5, null, outputFileDTO.getFile()[0]);
                } else {
                    expectedOutputs = outputFileDTO.getFile();
                }
            } catch (RuntimeException e) {
                return new ResultVO((byte) 5, null, "practice/submit: Error retrieving output files: " + e.getMessage());
            }
            ResultVO runResult = runClient.run(inputs, "C");
            System.out.println(runResult.getStatus());
            if (runResult.getStatus() != 0) {
                return runResult;
            }
            CheckDTO checkResult = checkAnswer(expectedOutputs, ((List<String>)runResult.getData()).toArray(new String[expectedOutputs.length]));
            if (checkResult.getStatus() == 0) {
                return new ResultVO((byte) 0, null, null);
            } else if (checkResult.getStatus() == 1) {
                return new ResultVO((byte) 1, null, checkResult.getMessage());
            } else {
                return new ResultVO((byte) 5, null, checkResult.getMessage());
            }
        } catch (Exception e) {
            return new ResultVO((byte) 5, null, "practice/submit: " + e.getMessage());
        }
    }

    private CheckDTO checkAnswer(String[] expected, String[] actual) {
        if (expected.length != actual.length) {
            return new CheckDTO((byte) 2, "practice/checkAnswer: Test case count mismatch: expected " + expected.length + ", got " + actual.length);
        }
        for (int i = 0; i < expected.length; i++) {
            if (!expected[i].equals(actual[i])) {
                return new CheckDTO((byte) 1, "practice/checkAnswer: Test case " + (i + 1) + " failed: expected '" + expected[i] + "', got '" + actual[i] + "'");
            }
        }
        return new CheckDTO((byte) 0, null);
    }

    private FileDTO getFile(String problemId, int count, FileDTO.fileType fileType) {

        FileDTO fileDTO = new FileDTO();

        String[] files = new String[count];

        for (int i = 1; i <= count; i++) {
            String file;
            try {
                if (fileType == FileDTO.fileType.OUTPUT) {
                    file = minioRepos.getFile(problemId, i, FileDTO.fileType.OUTPUT);
                } else {
                    file = minioRepos.getFile(problemId, i, FileDTO.fileType.INPUT);
                }
                if (file == null) {
                    fileDTO.setStatus((byte) 1);
                    fileDTO.setFile(new String[] {"practice/getFile: File " + i + " not found"});
                    return fileDTO;
                }
            } catch (Exception e) {
                fileDTO.setStatus((byte) 2);
                fileDTO.setFile(new String[] {"practice/getFile: Error retrieving file " + i + ": " + e.getMessage()});
                return fileDTO;
            }
            files[i - 1] = file;
        }
        fileDTO.setFile(files);
        return fileDTO;
    }
}
