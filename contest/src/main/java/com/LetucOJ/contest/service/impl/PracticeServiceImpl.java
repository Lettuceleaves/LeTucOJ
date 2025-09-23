package com.LetucOJ.contest.service.impl;

import com.LetucOJ.common.oss.MinioRepos;
import com.LetucOJ.common.result.ResultVO;
import com.LetucOJ.contest.client.RunClient;
import com.LetucOJ.contest.model.db.BoardDTO;
import com.LetucOJ.contest.model.db.ContestInfoDTO;
import com.LetucOJ.contest.model.db.ProblemStatusDTO;
import com.LetucOJ.contest.model.method.CheckDTO;
import com.LetucOJ.contest.repos.MybatisRepos;
import com.LetucOJ.contest.service.PracticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PracticeServiceImpl implements PracticeService {

    @Autowired
    private RunClient runClient;

    @Autowired
    private MinioRepos minioRepos;

    @Autowired
    private MybatisRepos mybatisRepos;

    public ResultVO submit(String userName, String cnname, String questionName, String contestName, String code, String lang, boolean root) throws Exception {
        try {
            List<String> inputs = new ArrayList<>();
            inputs.add(code);
            String[] inputFiles;

            ContestInfoDTO contestInfo = mybatisRepos.getContest(contestName);

            if (contestInfo == null) {
                return new ResultVO((byte) 5, null, "contest/submit: Contest not found or not available");
            } else if (!contestInfo.isPublicContest() && !root) {
                return new ResultVO((byte) 5, null, "contest/submit: Contest is not available");
            }

            Integer score = mybatisRepos.getScoreByContestAndProblem(contestName, questionName);
            if (score == null || score == 0) {
                return new ResultVO((byte) 5, null, "contest/submit: Problem not found in contest");
            }

            // check time
            if (!root) {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime start = contestInfo.getStart();
                LocalDateTime end = contestInfo.getEnd();
                if (start != null && end != null) {
                    if (now.isBefore(start)) {
                        long secondsToStart = Duration.between(now, start).getSeconds();
                        return new ResultVO((byte) 5, null,
                                "contest/submit: Contest has not started yet, start in "
                                        + secondsToStart + " seconds");
                    } else if (now.isAfter(end)) {
                        return new ResultVO((byte) 5, null, "contest/submit: Contest has already ended");
                    }
                } else {
                    return new ResultVO((byte) 5, null, "contest/submit: Contest start or end time is not set");
                }
            }

            ProblemStatusDTO problemStatus = mybatisRepos.getStatus(questionName);
            if (problemStatus == null) {
                return new ResultVO((byte) 5, null, "contest/submit: Problem not found or not available");
            } else if (problemStatus.getCaseAmount() <= 0) {
                return new ResultVO((byte) 5, null, "contest/submit: No test cases available for this problem");
            }

            String bucketName = "letucoj";
            try {
                for (int i = 1; i <= problemStatus.getCaseAmount(); i++) {
                    String fileName = "problems/" + questionName + "/input/" + i + ".txt";
                    byte[] inputFile = minioRepos.getFile(bucketName, fileName);
                    inputs.add(new String(inputFile));
                }
            } catch (RuntimeException e) {
                return new ResultVO((byte) 5, null, "contest/submit: Error retrieving input files: " + e.getMessage());
            }
            String[] expectedOutputs = new String[problemStatus.getCaseAmount() + 1];
            try {
                for (int i = 1; i <= problemStatus.getCaseAmount(); i++) {
                    String fileName = "problems/" + questionName + "/output/" + i + ".txt";
                    byte[] outputFile = minioRepos.getFile(bucketName, fileName);
                    expectedOutputs[i] = new String(outputFile).trim();
                }
            } catch (RuntimeException e) {
                return new ResultVO((byte) 5, null, "contest/submit: Error retrieving output files: " + e.getMessage());
            }
            ResultVO runResult = runClient.run(inputs, lang);
            System.out.println(runResult.getStatus());
            if (runResult.getStatus() != 0) {
                return runResult;
            }
            CheckDTO checkResult = checkAnswer(expectedOutputs, ((List<String>) runResult.getData()).toArray(new String[expectedOutputs.length]));

            if (checkResult.getStatus() == 2) {
                return new ResultVO((byte) 5, null, checkResult.getMessage());
            }

            int getScore;
            if (contestInfo.getMode().equals("add")) {
                getScore = (int) ( ( (float) checkResult.getCaseIndex() / (float) expectedOutputs.length) * (float) score);
            } else if (contestInfo.getMode().equals("all")) {
                getScore = checkResult.getStatus() == 0 ? score : 0;
            } else {
                return new ResultVO((byte) 5, null, "contest/submit: Invalid contest mode");
            }

            System.out.println("getScore: " + getScore);
            System.out.println("score: " + score);
            System.out.println("index: " + checkResult.getCaseIndex());
            System.out.println("expectedOutputs: " + expectedOutputs.length);

            BoardDTO boardDTO = mybatisRepos.getContestBoardByUserAndProblem(contestName, userName, questionName);
            if (boardDTO == null) {
                boardDTO = new BoardDTO(contestName, userName, cnname, questionName, getScore, 1, LocalDateTime.now());
                Integer res = mybatisRepos.insertContestBoard(boardDTO);
                if (res == null || res == 0) {
                    return new ResultVO((byte) 5, null, "contest/submit: Board Insert failed");
                }
            } else {
                boardDTO.setScore(Math.max(boardDTO.getScore(), getScore));
                Integer res = mybatisRepos.updateContestBoard(boardDTO);
                if (res == null || res <= 0) {
                    return new ResultVO((byte) 5, null, "contest/submit: Error updating contest board");
                }
            }

            if (checkResult.getStatus() == 1) {
                return new ResultVO((byte) 1, null, "failed in " + checkResult.getCaseIndex() + "th case");
            } else {
                return new ResultVO((byte) 0, null, null);
            }
        } catch (Exception e) {
            return new ResultVO((byte) 5, null, "contest/submit: Error during submission: " + e.getMessage());
        }
    }

    private CheckDTO checkAnswer(String[] expected, String[] actual) {
        if (expected.length != actual.length) {
            return new CheckDTO(2, 0, "contest/checkAnswer: Wrong number of answers");
        }
        for (int i = 0; i < expected.length; i++) {
            if (!expected[i].equals(actual[i])) {
                return new CheckDTO(1, i, "contest/checkAnswer: Wrong answer");
            }
        }
        return new CheckDTO(0, expected.length, "contest/checkAnswer: Correct answer");
    }
}
