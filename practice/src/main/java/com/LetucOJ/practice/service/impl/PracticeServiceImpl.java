package com.LetucOJ.practice.service.impl;

import com.LetucOJ.common.log.LogLevel;
import com.LetucOJ.common.log.Logger;
import com.LetucOJ.common.log.Type;
import com.LetucOJ.common.oss.MinioRepos;
import com.LetucOJ.common.result.Result;
import com.LetucOJ.common.result.ResultVO;
import com.LetucOJ.common.result.errorcode.BaseErrorCode;
import com.LetucOJ.common.result.errorcode.PracticeErrorCode;
import com.LetucOJ.practice.client.RunClient;
import com.LetucOJ.practice.model.DTO.TestTaskDTO;
import com.LetucOJ.practice.model.ProblemStatus;
import com.LetucOJ.practice.repos.MybatisRepos;
import com.LetucOJ.practice.service.PracticeService;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@Data
@AllArgsConstructor
public class PracticeServiceImpl implements PracticeService {

    private RunClient runClient;

    private MinioRepos minioRepos;

    private MybatisRepos mybatisRepos;

    public ResultVO<Integer> submit(String userName, String problemName, String code, String language, boolean root) throws Exception {
        try {
            List<String> inputs = new ArrayList<>();

            ProblemStatus problemStatus = mybatisRepos.getStatus(problemName);


            if (problemStatus == null) {
                return Result.failure(BaseErrorCode.PROBLEM_NOT_EXIST, null);
            } else if (problemStatus.getCaseAmount() <= 0) {
                return Result.failure(BaseErrorCode.NO_CASE_EXIST, null);
            } else if (!problemStatus.isPublicProblem() && !root) {
                return Result.failure(PracticeErrorCode.NOT_PUBLIC, null);
            }
            byte[][] inputBytesArrays;
            try {
                inputBytesArrays = getCases(problemName, problemStatus.getCaseAmount(), 0);
            } catch (RuntimeException e) {
                return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
            }
            for (byte[] inputBytes : inputBytesArrays) {
                inputs.add(new String(inputBytes));
            }
            List<String> expectedOutputs;
            byte[][] outputBytesArray;
            try {
                outputBytesArray = getCases(problemName, problemStatus.getCaseAmount(), 1);
            } catch (RuntimeException e) {
                return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
            }
            ResultVO<TestTaskVO> runResult = runClient.run(new TestTaskDTO(code, inputs, language, problemName));
            if (!runResult.getCode().equals("0")) {
                return runResult;
            }
            expectedOutputs = getExpectedOutputs(outputBytesArray);
            ResultVO<TestTaskVO> resultVO = checkAnswer(expectedOutputs, (runResult.getData().getAnswer()));
            if (resultVO.getCode().equals("0")) {
                Integer check = mybatisRepos.checkCorrect(userName, problemName);
                if (check == null || check == 0) {
                    Integer res = mybatisRepos.insertCorrect(userName, problemName);
                    if (res == null || res == 0) {
                        return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
                    }
                }
                return Result.success(null);
            }
            return resultVO;
        } catch (Exception e) {
            Logger.log(Type.SERVER, LogLevel.ERROR, e.getMessage());
            return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
        }
    }

    private List<String> getExpectedOutputs(byte[][] outputBytesArray) {
        return List.of(Arrays.stream(outputBytesArray)
                .map(bytes -> new String(bytes, StandardCharsets.UTF_8))
                .flatMap(s -> Arrays.stream(s.split("\\R")))
                .toArray(String[]::new));
    }

    private ResultVO<Integer> checkAnswer(List<String> expected, List<String> actual) {
        if (expected.size() != actual.size()) {
            return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
        }
        for (int i = 0; i < expected.size(); i++) {
            if (!expected.get(i).equals(actual.get(i))) {
                String message = "expect: " + expected.get(i).substring(0, Math.min(500, expected.get(i).length())) + " but actual: " + actual.get(i).substring(0, Math.min(500, actual.get(i).length())) + " at case " + (i + 1);
                Logger.log(Type.CLIENT, LogLevel.INFO, message);
                return Result.failure(BaseErrorCode.WRONG_ANSWER, new TestTaskVO(null, message));
            }
        }
        return Result.success(null);
    }

    private byte[][] getCases(String problemId, int amount, int type) {

        byte[][] cases = new byte[amount][];
        for (int i = 1; i <= amount; i++) {
            byte[] file = new byte[0];
            String bucketName = "letucoj";
            try {
                String objectName;
                if (type == 1) {
                    objectName = "problems/" + problemId + "/output/" + i + ".txt";
                } else {
                    objectName = "problems/" + problemId + "/input/" + i + ".txt";
                }
                file = minioRepos.getFile(bucketName, objectName);
                if (file == null) {
                    throw new Exception("practice/getCases: File " + i + " not found");
                }
            } catch (Exception e) {
                Logger.log(Type.CLIENT, LogLevel.ERROR, "practice/getCases: Error retrieving file " + i + ": " + e.getMessage());
            }
            cases[i - 1] = file;
        }
        return cases;
    }
}
