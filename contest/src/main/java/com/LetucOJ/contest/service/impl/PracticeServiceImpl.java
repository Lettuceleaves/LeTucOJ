package com.LetucOJ.contest.service.impl;

import com.LetucOJ.common.oss.MinioRepos;
import com.LetucOJ.common.result.Result;
import com.LetucOJ.common.result.ResultVO;
import com.LetucOJ.common.result.errorcode.BaseErrorCode;
import com.LetucOJ.common.result.errorcode.ContestErrorCode;
import com.LetucOJ.contest.client.RunClient;
import com.LetucOJ.contest.model.Contest;
import com.LetucOJ.contest.model.DTO.BoardDTO;
import com.LetucOJ.contest.model.DTO.ProblemStatusDTO;
import com.LetucOJ.contest.model.VO.TestTaskVO;
import com.LetucOJ.contest.repos.MybatisRepos;
import com.LetucOJ.contest.service.DBService;
import com.LetucOJ.contest.service.PracticeService;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Data
@AllArgsConstructor
public class PracticeServiceImpl implements PracticeService {

    private RunClient runClient;

    private MinioRepos minioRepos;

    private MybatisRepos mybatisRepos;

    private DBService dbService;

    public ResultVO<TestTaskVO> submit(String userName, String cnname, String questionName, String contestName, String code, String lang, boolean root) throws Exception {
        try {

            List<String> inputs = new ArrayList<>();
            inputs.add(code);

            Contest contestInfo = mybatisRepos.getContest(contestName);

            ResultVO<Void> attended = dbService.attended(userName, contestName);
            if (!attended.getCode().equals("0") && !root) {
                return Result.failure(ContestErrorCode.USER_NOT_ATTEND, null);
            }

            if (contestInfo == null) {
                return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
            } else if (!contestInfo.isPublicContest() && !root) {
                return Result.failure(ContestErrorCode.CONTEST_NOT_PUBLIC, null);
            }

            Integer score = mybatisRepos.getScoreByContestAndProblem(contestName, questionName);
            if (score == null || score == 0) {
                return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
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
                        return Result.failure(ContestErrorCode.CONTEST_NOT_START, null);
                    } else if (now.isAfter(end)) {
                        return Result.failure(ContestErrorCode.CONTEST_FINISHED, null);
                    }
                } else {
                    return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
                }
            }

            // 获取测试数据

            ProblemStatusDTO problemStatus = mybatisRepos.getStatus(questionName);
            if (problemStatus == null) {
                return Result.failure(BaseErrorCode.PROBLEM_NOT_EXIST, null);
            } else if (problemStatus.getCaseAmount() <= 0) {
                return Result.failure(BaseErrorCode.NO_CASE_EXIST, null);
            }

            byte[][] inputBytesArrays;
            try {
                inputBytesArrays = getCases(questionName, problemStatus.getCaseAmount(), 0);
            } catch (RuntimeException e) {
                return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
            }
            for (byte[] inputBytes : inputBytesArrays) {
                inputs.add(new String(inputBytes));
            }
            String[] expectedOutputs;
            byte[][] outputBytesArray;
            try {
                outputBytesArray = getCases(questionName, problemStatus.getCaseAmount(), 1);
            } catch (RuntimeException e) {
                return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
            }


            // 运行用户代码

            ResultVO<TestTaskVO> runResult = runClient.run(inputs, lang, questionName);


            // 处理运行结果


            System.out.println(runResult.getCode());
            if (!runResult.getCode().equals("0")) {
                return runResult;
            }
            expectedOutputs = getExpectedOutputs(outputBytesArray);

            ResultVO<TestTaskVO> resultVO = checkAnswer(expectedOutputs, runResult.getData().getAnswer().toArray(new String[expectedOutputs.length]));

            int getScore;
            if (contestInfo.getMode().equals("add")) {
                getScore = resultVO.getCode().equals("0") ? score : (int) ( ( (float) Integer.getInteger(resultVO.getData().getMsg()) / (float) expectedOutputs.length) * (float) score);
            } else if (contestInfo.getMode().equals("all")) {
                getScore = resultVO.getCode().equals("0") ? score : 0;
            } else {
                return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
            }

            BoardDTO boardDTO = mybatisRepos.getContestBoardByUserAndProblem(contestName, userName, questionName);
            if (boardDTO == null) {
                boardDTO = new BoardDTO(contestName, userName, cnname, questionName, getScore, 1, LocalDateTime.now());
                Integer res = mybatisRepos.insertContestBoard(boardDTO);
                if (res == null || res <= 0) {
                    System.out.println("failed to insert board");
                    return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
                }
            } else {
                boardDTO.setScore(Math.max(boardDTO.getScore(), getScore));
                Integer res = mybatisRepos.updateContestBoard(boardDTO);
                if (res == null || res <= 0) {
                    System.out.println("failed to update board");
                    return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
                }
            }
            if (resultVO.getCode().equals("0")) {
                return Result.success(null);
            } else {
                return Result.failure(BaseErrorCode.WRONG_ANSWER, new TestTaskVO(null, "wrong in case " + resultVO.getData().getMsg()));
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
        }
    }

    private ResultVO<TestTaskVO> checkAnswer(String[] expected, String[] actual) {
        if (expected.length != actual.length) {
            return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
        }
        for (int i = 0; i < expected.length; i++) {
            if (!expected[i].equals(actual[i])) {
                return Result.failure(BaseErrorCode.WRONG_ANSWER, new TestTaskVO(null, String.valueOf(i)));
            }
        }
        return Result.success(null);
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
                throw new RuntimeException("practice/getCases: Error retrieving file " + i + ": " + e.getMessage());
            }
            cases[i - 1] = file;
        }
        return cases;
    }
}
