package com.LetucOJ.practice.service.impl;

import com.LetucOJ.common.oss.MinioRepos;
import com.LetucOJ.common.result.Result;
import com.LetucOJ.common.result.ResultVO;
import com.LetucOJ.common.result.errorcode.BaseErrorCode;
import com.LetucOJ.common.result.errorcode.PracticeErrorCode;
import com.LetucOJ.practice.client.RunClient;
import com.LetucOJ.practice.model.*;
import com.LetucOJ.practice.repos.MybatisRepos;
import com.LetucOJ.practice.service.PracticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class PracticeServiceImpl implements PracticeService {

    @Autowired
    private RunClient runClient;

    @Autowired
    private MinioRepos minioRepos;

    @Autowired
    private MybatisRepos mybatisRepos;

    public ResultVO submit(String pname, String qname, String code, String language, boolean root) throws Exception {
        try {
            List<String> inputs = new ArrayList<>();
            inputs.add(code);

            ProblemStatusDTO problemStatus = mybatisRepos.getStatus(qname);

            if (problemStatus == null) {
                return Result.failure(BaseErrorCode.PROBLEM_NOT_EXIST);
            } else if (problemStatus.getCaseAmount() <= 0) {
                return Result.failure(BaseErrorCode.NO_CASE_EXIST);
            } else if (!problemStatus.isPublicProblem() && !root) {
                return Result.failure(PracticeErrorCode.NOT_PUBLIC);
            }
            byte[][] inputBytesArrays;
            try {
                inputBytesArrays = getCases(qname, problemStatus.getCaseAmount(), 0);
            } catch (RuntimeException e) {
                System.out.println(e.getMessage());
                return Result.failure(BaseErrorCode.SERVICE_ERROR);
            }
            for (byte[] inputBytes : inputBytesArrays) {
                inputs.add(new String(inputBytes));
            }
            String[] expectedOutputs;
            byte[][] outputBytesArray;
            try {
                outputBytesArray = getCases(qname, problemStatus.getCaseAmount(), 1);
            } catch (RuntimeException e) {
                System.out.println(e.getMessage());
                return Result.failure(BaseErrorCode.SERVICE_ERROR);
            }
            ResultVO runResult = runClient.run(inputs, language);
            if (!runResult.getCode().equals("0")) {
                return runResult;
            }
            expectedOutputs = getExpectedOutputs(outputBytesArray);
            ResultVO resultVO = checkAnswer(expectedOutputs, ((List<String>)runResult.getData()).toArray(new String[expectedOutputs.length]));
            if (resultVO.getCode().equals("0")) {
                Integer check = mybatisRepos.checkCorrect(pname, qname);
                if (check == null || check == 0) {
                    Integer res = mybatisRepos.insertCorrect(pname, qname);
                    if (res == null || res == 0) {
                        System.out.println("insertCorrect error");
                        return Result.failure(BaseErrorCode.SERVICE_ERROR);
                    }
                }
                return Result.success();
            }
            System.out.println(resultVO.getCode());
            return resultVO;
        } catch (Exception e) {
            System.out.println(e);
            return Result.failure(BaseErrorCode.SERVICE_ERROR);
        }
    }

    private String[] getExpectedOutputs(byte[][] outputBytesArray) {
        return Arrays.stream(outputBytesArray)
                .map(bytes -> new String(bytes, StandardCharsets.UTF_8))
                .flatMap(s -> Arrays.stream(s.split("\\R")))
                .toArray(String[]::new);
    }

    private ResultVO checkAnswer(String[] expected, String[] actual) {
        if (expected.length != actual.length) {
            System.out.println(expected.length + " != " + actual.length);
            return Result.failure(BaseErrorCode.SERVICE_ERROR);
        }
        for (int i = 0; i < expected.length; i++) {
            System.out.println(expected[i] + " == " + actual[i]);
            if (!expected[i].equals(actual[i])) {
                String msg = "expect: " + expected[i].substring(0, Math.min(500, expected[i].length())) + " but actual: " + actual[i].substring(0, Math.min(500, actual[i].length())) + " at case " + (i + 1);
                System.out.println(msg);
                return Result.failure(BaseErrorCode.WRONG_ANSWER, msg);
            }
        }
        System.out.println("check success");
        return Result.success();
    }

    private byte[][] getCases(String problemId, int amount, int type) {

        byte[][] cases = new byte[amount][];
        for (int i = 1; i <= amount; i++) {
            byte[] file;
            String bucketName = "letucoj";
            try {
                if (type == 1) {
                    String objectName = "problems/" + problemId + "/output/" + i + ".txt";
                    file = minioRepos.getFile(bucketName, objectName);
                } else {
                    String objectName = "problems/" + problemId + "/input/" + i + ".txt";
                    file = minioRepos.getFile(bucketName, objectName);
                }
                if (file == null) {
                    throw new Exception("practice/getCases: File " + i + " not found");
                }
            } catch (Exception e) {
                throw new RuntimeException("practice/getCases: Error retrieving file " + i + ": " + e.getMessage());
            }
            cases[i - 1] = file;
        }
        return cases;
    }
}
