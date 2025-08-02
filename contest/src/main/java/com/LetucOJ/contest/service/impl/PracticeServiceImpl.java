package com.LetucOJ.contest.service.impl;

import com.LetucOJ.contest.client.RunClient;
import com.LetucOJ.contest.model.db.BoardDTO;
import com.LetucOJ.contest.model.db.ContestInfoDTO;
import com.LetucOJ.contest.model.db.ProblemStatusDTO;
import com.LetucOJ.contest.model.method.CheckDTO;
import com.LetucOJ.contest.model.minio.FileDTO;
import com.LetucOJ.contest.model.net.CodeDTO;
import com.LetucOJ.contest.model.net.ResultVO;
import com.LetucOJ.contest.repos.MinioRepos;
import com.LetucOJ.contest.repos.MybatisRepos;
import com.LetucOJ.contest.service.PracticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

    public ResultVO submit(CodeDTO message, boolean root, String cnname) throws Exception {
        List<String> inputs = new ArrayList<>();
        inputs.add(message.getCode());
        String[] inputFiles;

        ContestInfoDTO contestInfo = mybatisRepos.getContest(message.getContestName());

        if (contestInfo == null) {
            return new ResultVO((byte) 5, null, "practice/submit: Contest not found or not available");
        } else if (!contestInfo.isIspublic()) {
            return new ResultVO((byte) 5, null, "practice/submit: Contest is not available");
        }

        Integer score = mybatisRepos.getScoreByContestAndProblem(message.getContestName(), message.getProblemName());
        if (score == null || score == 0) {
            return new ResultVO((byte) 5, null, "practice/submit: Problem not found in contest");
        }

        // check time
        if (contestInfo.getStart() != null && contestInfo.getEnd() != null) {
            Timestamp currentTime = new Timestamp(System.currentTimeMillis());
            if (currentTime.before(contestInfo.getStart())) {
                return new ResultVO((byte) 5, null, "practice/submit: Contest has not started yet");
            } else if (currentTime.after(contestInfo.getEnd())) {
                return new ResultVO((byte) 5, null, "practice/submit: Contest has already ended");
            }
        } else {
            return new ResultVO((byte) 5, null, "practice/submit: Contest start or end time is not set");
        }

        ProblemStatusDTO problemStatus = mybatisRepos.getStatus(message.getProblemName());
        if (problemStatus == null) {
            return new ResultVO((byte) 5, null, "practice/submit: Problem not found or not available");
        } else if (problemStatus.getCount() <= 0) {
            return new ResultVO((byte) 5, null, "practice/submit: No test cases available for this problem");
        }

        try {
            FileDTO fileDTO = getFile(message.getProblemName(), problemStatus.getCount(), FileDTO.fileType.INPUT);
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
            FileDTO outputFileDTO = getFile(message.getProblemName(), problemStatus.getCount(), FileDTO.fileType.OUTPUT);
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
        ResultVO runResult = runClient.run(inputs);
        System.out.println(runResult.getStatus());
        if (runResult.getStatus() != 0) {
            return runResult;
        }
        CheckDTO checkResult = checkAnswer(expectedOutputs, ((List<String>)runResult.getData()).toArray(new String[expectedOutputs.length]));

        BoardDTO boardDTO = mybatisRepos.getContestBoardByUserAndProblem(message.getProblemName(), message.getContestName(), cnname);
        if (boardDTO == null) {
            return new ResultVO((byte) 5, null, "practice/submit: Board not found for user and problem");
        } else {
            if (contestInfo.getMode().equals("add")) {
                boardDTO.setScore(Math.max(boardDTO.getScore(), (int)((float)(checkResult.getCaseIndex() / expectedOutputs.length) * (float)score)));
            } else if (contestInfo.getMode().equals("all")) {
                boardDTO.setScore(Math.max(boardDTO.getScore(), checkResult.getStatus() == 0 ? score : 0));
            } else {
                return new ResultVO((byte) 5, null, "practice/submit: Invalid contest mode");
            }
            boardDTO.setTimes(boardDTO.getTimes() + 1);
            mybatisRepos.updateContestBoard(boardDTO);
        }

        if (checkResult.getStatus() == 0) {
            return new ResultVO((byte) 0, null, null);
        } else if (checkResult.getStatus() == 1) {
            return new ResultVO((byte) 1, null, checkResult.getMessage());
        } else {
            return new ResultVO((byte) 5, null, checkResult.getMessage());
        }
    }

    private CheckDTO checkAnswer(String[] expected, String[] actual) {
        if (expected.length != actual.length) {
            return new CheckDTO((byte) 2, "practice/checkAnswer: Test case count mismatch: expected " + expected.length + ", got " + actual.length, -1);
        }
        for (int i = 0; i < expected.length; i++) {
            if (!expected[i].equals(actual[i])) {
                return new CheckDTO((byte) 1, "practice/checkAnswer: Test case " + (i + 1) + " failed", i);
            }
        }
        return new CheckDTO((byte) 0, null, expected.length);
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
