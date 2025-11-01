package com.LetucOJ.contest.service.impl;

import com.LetucOJ.common.oss.MinioRepos;
import com.LetucOJ.common.result.Result;
import com.LetucOJ.common.result.ResultVO;
import com.LetucOJ.common.result.errorcode.BaseErrorCode;
import com.LetucOJ.common.result.errorcode.ContestErrorCode;
import com.LetucOJ.contest.client.RunClient;
import com.LetucOJ.contest.model.BoardDTO;
import com.LetucOJ.contest.model.ContestInfoDTO;
import com.LetucOJ.contest.model.ProblemStatusDTO;
import com.LetucOJ.contest.repos.MybatisRepos;
import com.LetucOJ.contest.service.DBService;
import com.LetucOJ.contest.service.PracticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
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

    @Autowired
    private DBService dbService;

    public ResultVO submit(String userName, String cnname, String questionName, String contestName, String code, String lang, boolean root) throws Exception {
        try {

            // 合法性检验

            List<String> inputs = new ArrayList<>();
            inputs.add(code);

            ContestInfoDTO contestInfo = mybatisRepos.getContest(contestName);

            ResultVO attended = dbService.getUserStatus(userName, contestName);
            if (!attended.getCode().equals("0") && !root) {
                return Result.failure(ContestErrorCode.USER_NOT_ATTEND);
            }

            if (contestInfo == null) {
                return Result.failure(BaseErrorCode.SERVICE_ERROR);
            } else if (!contestInfo.isPublicContest() && !root) {
                return Result.failure(ContestErrorCode.CONTEST_NOT_PUBLIC);
            }

            Integer score = mybatisRepos.getScoreByContestAndProblem(contestName, questionName);
            if (score == null || score == 0) {
                return Result.failure(BaseErrorCode.SERVICE_ERROR);
            }

            if (!root) {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime start = contestInfo.getStart();
                LocalDateTime end = contestInfo.getEnd();
                System.out.println(now);
                System.out.println(start);
                System.out.println(end);
                if (start != null && end != null) {
                    if (now.isBefore(start)) {
                        long secondsToStart = Duration.between(now, start).getSeconds();
                        return Result.failure(ContestErrorCode.CONTEST_NOT_START, secondsToStart);
                    } else if (now.isAfter(end)) {
                        return Result.failure(ContestErrorCode.CONTEST_FINISHED);
                    }
                } else {
                    return Result.failure(BaseErrorCode.SERVICE_ERROR);
                }
            }

            // 获取测试数据

            ProblemStatusDTO problemStatus = mybatisRepos.getStatus(questionName);
            if (problemStatus == null) {
                return Result.failure(BaseErrorCode.PROBLEM_NOT_EXIST);
            } else if (problemStatus.getCaseAmount() <= 0) {
                return Result.failure(BaseErrorCode.NO_CASE_EXIST);
            }

            byte[][] inputBytesArrays;
            try {
                inputBytesArrays = getCases(questionName, problemStatus.getCaseAmount(), 0);
            } catch (RuntimeException e) {
                return Result.failure(BaseErrorCode.SERVICE_ERROR);
            }
            for (byte[] inputBytes : inputBytesArrays) {
                inputs.add(new String(inputBytes));
            }
            String[] expectedOutputs;
            byte[][] outputBytesArray;
            try {
                outputBytesArray = getCases(questionName, problemStatus.getCaseAmount(), 1);
            } catch (RuntimeException e) {
                return Result.failure(BaseErrorCode.SERVICE_ERROR);
            }


            // 运行用户代码

            ResultVO runResult = runClient.run(inputs, lang, questionName);


            // 处理运行结果


            System.out.println(runResult.getCode());
            if (!runResult.getCode().equals("0")) {
                return runResult;
            }
            expectedOutputs = getExpectedOutputs(outputBytesArray);

            ResultVO resultVO = checkAnswer(expectedOutputs, ((List<String>) runResult.getData()).toArray(new String[expectedOutputs.length]));

            int getScore;
            if (contestInfo.getMode().equals("add")) {
                getScore = resultVO.getCode().equals("0") ? score : (int) ( ( (float) resultVO.getData() / (float) expectedOutputs.length) * (float) score);
            } else if (contestInfo.getMode().equals("all")) {
                getScore = resultVO.getCode().equals("0") ? score : 0;
            } else {
                return Result.failure(BaseErrorCode.SERVICE_ERROR);
            }

            BoardDTO boardDTO = mybatisRepos.getContestBoardByUserAndProblem(contestName, userName, questionName);
            if (boardDTO == null) {
                boardDTO = new BoardDTO(contestName, userName, cnname, questionName, getScore, 1, LocalDateTime.now());
                Integer res = mybatisRepos.insertContestBoard(boardDTO);
                if (res == null || res <= 0) {
                    System.out.println("failed to insert board");
                    return Result.failure(BaseErrorCode.SERVICE_ERROR);
                }
            } else {
                boardDTO.setScore(Math.max(boardDTO.getScore(), getScore));
                Integer res = mybatisRepos.updateContestBoard(boardDTO);
                if (res == null || res <= 0) {
                    System.out.println("failed to update board");
                    return Result.failure(BaseErrorCode.SERVICE_ERROR);
                }
            }
            if (resultVO.getCode().equals("0")) {
                return Result.success();
            } else {
                Integer caseIndex = (Integer) resultVO.getData();
                return Result.failure(BaseErrorCode.WRONG_ANSWER, "wrong in case " + (caseIndex + 1));
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return Result.failure(BaseErrorCode.SERVICE_ERROR);
        }
    }

    private ResultVO checkAnswer(String[] expected, String[] actual) {
        if (expected.length != actual.length) {
            return Result.failure(BaseErrorCode.SERVICE_ERROR);
        }
        for (int i = 0; i < expected.length; i++) {
            if (!expected[i].equals(actual[i])) {
                return Result.failure(BaseErrorCode.WRONG_ANSWER, i);
            }
        }
        return Result.success();
    }

    private String[] getExpectedOutputs(byte[][] outputBytesArray) {
        return Arrays.stream(outputBytesArray)
                .map(bytes -> new String(bytes, StandardCharsets.UTF_8))
                .flatMap(s -> Arrays.stream(s.split("\\R")))
                .toArray(String[]::new);
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
