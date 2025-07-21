package com.LetucOJ.practice.service.impl;

import com.LetucOJ.practice.client.RunClient;
import com.LetucOJ.practice.model.*;
import com.LetucOJ.practice.repos.MinioRepos;
import com.LetucOJ.practice.repos.MybatisRepos;
import com.LetucOJ.practice.service.PracticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
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

    public ResultVO submitTest(CodeDTO message) throws Exception {
        List<String> inputs = new ArrayList<>();
        inputs.add(message.getCode());
        String[] inputFiles;
        try {
            FileDTO fileDTO = getFile(message.getName(), FileDTO.fileType.INPUT);
            if (fileDTO.getStatus() == 1) {
                return new ResultVO((byte) 5, "TestCase Not Found", null);
            } else if (fileDTO.getStatus() == 2) {
                return new ResultVO((byte) 5, fileDTO.getFile()[0], null);
            } else {
                inputFiles = fileDTO.getFile();
            }
        } catch (RuntimeException e) {
            return new ResultVO((byte) 5, "Error retrieving input files: " + e.getMessage(), null);
        }
        inputs.addAll(Arrays.asList(inputFiles));
        List<String> outputs = new ArrayList<>();
        String[] expectedOutputs;
        try {
            FileDTO outputFileDTO = getFile(message.getName(), FileDTO.fileType.OUTPUT);
            if (outputFileDTO.getStatus() == 1) {
                return new ResultVO((byte) 5, "Output files not found", null);
            } else if (outputFileDTO.getStatus() == 2) {
                return new ResultVO((byte) 5, outputFileDTO.getFile()[0], null);
            } else {
                expectedOutputs = outputFileDTO.getFile();
            }
        } catch (RuntimeException e) {
            return new ResultVO((byte) 5, "Error retrieving output files: " + e.getMessage(), null);
        }
        ResultVO runResult = runClient.runTest(inputs); // TODO 写入文档，Windows系统下运行 只能用test
        System.out.println(runResult.getStatus());
        if (runResult.getStatus() != 0) {
            return runResult;
        }
        CheckDTO checkResult = checkAnswer(expectedOutputs, parseUserAnswer(runResult.getDataAsString()));
        if (checkResult.getStatus() == 0) {
            return new ResultVO((byte) 0, "All test cases passed", null);
        } else if (checkResult.getStatus() == 1) {
            return new ResultVO((byte) 1, checkResult.getMessage(), null);
        } else {
            return new ResultVO((byte) 5, checkResult.getMessage(), null);
        }
    }

    private String[] parseUserAnswer(String userAnswer) {
        // 用户格式是[a, b, c, ..., d]个字符串，提取出a, b, c, d...放入ans // TODO 检测机制要写入文档
        return userAnswer.replaceAll("[\\[\\]\\s]", "").split(",");
    }

    private CheckDTO checkAnswer(String[] expected, String[] actual) {
        if (expected.length != actual.length) {
            return new CheckDTO((byte) 2, "Test case count mismatch: expected " + expected.length + ", got " + actual.length);
        }
        for (int i = 0; i < expected.length; i++) {
            if (!expected[i].equals(actual[i])) {
                return new CheckDTO((byte) 1, "Test case " + (i + 1) + " failed: expected '" + expected[i] + "', got '" + actual[i] + "'");
            }
        }
        return new CheckDTO((byte) 0, "All test cases passed");
    }

    private FileDTO getFile(String problemId, FileDTO.fileType fileType) {

        FileDTO fileDTO = new FileDTO();

        Integer numTestCases;
        try {
            numTestCases = mybatisRepos.getCaseAmount(problemId);
            if (numTestCases == null || numTestCases <= 0) {
                fileDTO.setStatus((byte) 1);
                return fileDTO;
            }
        } catch (Exception e) {
            fileDTO.setStatus((byte) 2);
            fileDTO.setFile(new String[] {"Error retrieving test case amount: " + e.getMessage()});
            return fileDTO;
        }
        String[] files = new String[numTestCases];

        for (int i = 1; i <= numTestCases; i++) {
            String file = null;
            try {
                if (fileType == FileDTO.fileType.OUTPUT) {
                    file = minioRepos.getFile(problemId, i, FileDTO.fileType.OUTPUT);
                } else {
                    file = minioRepos.getFile(problemId, i, FileDTO.fileType.INPUT);
                }
                if (file == null) {
                    fileDTO.setStatus((byte) 1);
                    fileDTO.setFile(new String[] {"File " + i + " not found"});
                    return fileDTO;
                }
            } catch (Exception e) {
                fileDTO.setStatus((byte) 2);
                fileDTO.setFile(new String[] {"Error retrieving file " + i + ": " + e.getMessage()});
                return fileDTO;
            }
            files[i - 1] = file;
        }
        fileDTO.setFile(files);
        return fileDTO;
    }

    public ResultVO getProblemCaseAmount(String problemId) {
        Integer result;
        try {
            result = mybatisRepos.getCaseAmount(problemId);
        } catch (Exception e) {
            return new ResultVO((byte) 5, null, "Error retrieving test case amount: " + e.getMessage());
        }
        return new ResultVO((byte) 0, result, null);
    }

    public ResultVO getCase(CaseInputDTO caseInputDTO) {
        List<String> inputs = new ArrayList<>();
        inputs.add(caseInputDTO.getCode());
        inputs.add(caseInputDTO.getInput());
        return runClient.runTest(inputs);
    }

    public ResultVO submitCase(CasePairDTO casePairDTO) {
        String problemId = casePairDTO.getProblemId();
        String input = casePairDTO.getInput();
        String output = casePairDTO.getOutput();
        try {
            // 检查输入输出是否存在
            if (input == null || output == null) {
                return new ResultVO((byte) 5, "Input or output cannot be null", null);
            }
            Integer result = mybatisRepos.incrementCaseAmount(problemId);
            if (result == null || result <= 0) {
                return new ResultVO((byte) 5, "Error incrementing case amount", null);
            }
            String inputFile = minioRepos.addFilePair(problemId, result, input, output);
            if (inputFile == null) {
                return new ResultVO((byte) 5, "Error adding file pair", null);
            }
            return new ResultVO((byte) 0, "Test case submitted successfully", null);
        } catch (Exception e) {
            return new ResultVO((byte) 5, "Error submitting test case: " + e.getMessage(), null);
        }
    }
}
