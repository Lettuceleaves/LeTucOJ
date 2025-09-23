package com.LetucOJ.practice.service.impl;

import com.LetucOJ.common.oss.MinioRepos;
import com.LetucOJ.common.result.ResultVO;
import com.LetucOJ.practice.client.RunClient;
import com.LetucOJ.practice.model.*;
import com.LetucOJ.practice.repos.MybatisRepos;
import com.LetucOJ.practice.service.PracticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
            System.out.println(problemStatus);
            if (problemStatus == null) {
                return new ResultVO( 5, null, "practice/submit: Problem not found or not available");
            } else if (problemStatus.getCaseAmount() <= 0) {
                return new ResultVO( 5, null, "practice/submit: No test cases available for this problem: " + qname + " " + problemStatus.getCaseAmount());
            } else if (!problemStatus.isPublicProblem() && !root) {
                return new ResultVO( 5, null, "practice/submit: Problem is not available for practice");
            }
            byte[][] inputBytesArrays;
            try {
                inputBytesArrays = getCases(qname, problemStatus.getCaseAmount(), 0);
            } catch (RuntimeException e) {
                return new ResultVO( 5, null, "practice/submit: Error retrieving input files: " + e.getMessage());
            }
            for (byte[] inputBytes : inputBytesArrays) {
                inputs.add(new String(inputBytes));
            }
            String[] expectedOutputs;
            byte[][] outputBytesArray;
            try {
                outputBytesArray = getCases(qname, problemStatus.getCaseAmount(), 1);
            } catch (RuntimeException e) {
                return new ResultVO( 5, null, "practice/submit: Error retrieving output files: " + e.getMessage());
            }
            ResultVO runResult = runClient.run(inputs, language);
            System.out.println(runResult.getStatus());
            if (runResult.getStatus() != 0) {
                return runResult;
            }
            expectedOutputs = Arrays.toString(outputBytesArray).split("\n");
            ResultVO checkVO = checkAnswer(expectedOutputs, ((List<String>)runResult.getData()).toArray(new String[expectedOutputs.length]));
            switch (checkVO.getStatus()) {
                case 0 -> {
                    Integer check = mybatisRepos.checkCorrect(pname, qname);
                    if (check == null || check == 0) {
                        Integer res = mybatisRepos.insertCorrect(pname, qname);
                        if (res == null || res == 0) {
                            return new ResultVO( 5, null, "practice/submit: Error recording correct submission");
                        }
                    }
                    return new ResultVO( 0, null, null);
                }
                case 1 -> {
                    if (!problemStatus.isShowsolution()) {
                        Map<String, Object> data = (Map<String, Object>) checkVO.getData();
                        if (data != null) {
                            data.put("expected", null);
                            data.put("actual", null);
                        }
                    }
                    return new ResultVO( 1, checkVO.getData(), null);
                }
                default -> {
                    return new ResultVO( 5, null, "practice/submit: " + checkVO.getError());
                }
            }
        } catch (Exception e) {
            return new ResultVO( 5, null, "practice/submit: " + e.getMessage());
        }
    }

    private ResultVO checkAnswer(String[] expected, String[] actual) {
        if (expected.length != actual.length) {
            return new ResultVO(5, null, "practice/submit: Wrong number of answers");
        }
        for (int i = 0; i < expected.length; i++) {
            if (!expected[i].equals(actual[i])) {
                return new ResultVO(1, Map.of(
                        "index", i + 1,
                        "expected", expected[i],
                        "actual", actual[i]
                ), "Wrong Answer on case " + (i + 1));
            }
        }
        return new ResultVO(0, null, null);
    }

    private byte[][] getCases(String problemId, int amount, int type) { // 0: Input 1: Output 3: Config

        byte[][] cases = new byte[amount][];
        for (int i = 1; i <= amount; i++) {
            byte[] file;
            String bucketName = "letucoj";
            try {
                if (type == 1) {
                    String fileName = "problems/" + problemId + "/output/" + i + ".txt";
                    file = minioRepos.getFile(bucketName, fileName);
                } else {
                    String fileName = "problems/" + problemId + "/input/" + i + ".txt";
                    file = minioRepos.getFile(bucketName, fileName);
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
