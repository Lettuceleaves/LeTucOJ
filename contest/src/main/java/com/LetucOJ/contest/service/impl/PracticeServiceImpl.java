package com.LetucOJ.contest.service.impl;

import com.LetucOJ.common.oss.MinioRepos;
import com.LetucOJ.common.result.Result;
import com.LetucOJ.common.result.ResultVO;
import com.LetucOJ.common.result.errorcode.BaseErrorCode;
import com.LetucOJ.common.result.errorcode.ContestErrorCode;
import com.LetucOJ.common.result.errorcode.RunErrorCode;
import com.LetucOJ.contest.client.RunClient;
import com.LetucOJ.contest.model.Contest;
import com.LetucOJ.contest.model.DTO.BoardDTO;
import com.LetucOJ.contest.model.DTO.ProblemStatusDTO;
import com.LetucOJ.contest.model.DTO.TestTaskDTO;
import com.LetucOJ.contest.model.VO.TestTaskVO;
import com.LetucOJ.contest.repos.MybatisRepos;
import com.LetucOJ.contest.service.DBService;
import com.LetucOJ.contest.service.PracticeService;
import com.LetucOJ.contest.tool.TimeChecker;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Data
@AllArgsConstructor
public class PracticeServiceImpl implements PracticeService {

    private RunClient runClient;

    private MinioRepos minioRepos;

    private MybatisRepos mybatisRepos;

    private DBService dbService;

    public ResultVO<TestTaskVO> submit(String userName, String nickName, String problemName, String contestName, String code, String language, String role) throws Exception {
        try {

            Contest contest = mybatisRepos.getContest(contestName);

            ResultVO<Void> attended = dbService.attended(userName, contestName);
            if (!attended.getCode().equals("0") && role.equals("USER")) {
                return Result.failure(ContestErrorCode.USER_NOT_ATTEND, null);
            }

            if (contest == null) {
                return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
            } else if (!contest.isPublicContest() && role.equals("USER")) {
                return Result.failure(ContestErrorCode.CONTEST_NOT_PUBLIC, null);
            }

            Integer totalScore = mybatisRepos.getScoreByContestAndProblem(contestName, problemName);
            if (totalScore == null || totalScore == 0) {
                return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
            }

            if (role.equals("USER")) {
                ResultVO<TestTaskVO> check = TimeChecker.checkTime(contest);
                if (!check.getCode().equals("0")) {
                    return check;
                }
            }

            // 检测题目是否存在
            ProblemStatusDTO problemStatus = mybatisRepos.getStatus(problemName);
            if (problemStatus == null) {
                return Result.failure(BaseErrorCode.PROBLEM_NOT_EXIST, null);
            } else if (problemStatus.getCaseAmount() <= 0) {
                return Result.failure(BaseErrorCode.NO_CASE_EXIST, null);
            }

            // 运行用户代码
            ResultVO<TestTaskVO> runResult = runClient.runTestTask(new TestTaskDTO(problemName, language, code, problemStatus.getCaseAmount()));

            // 处理运行结果，后续只需要处理正确/错误结果，编译错误，运行时错误，超时，系统错误就直接返回
            if (!runResult.getCode().equals("0") && !runResult.getCode().equals(RunErrorCode.WRONG_ANSWER.code())) {
                return runResult;
            }

            int userScore = totalScore;

            // 回答错误，根据赛制扣分
            if (runResult.getCode().equals(RunErrorCode.WRONG_ANSWER.code())) {
                if ("ACM".equals(contest.getMode())) {
                    userScore = 0;
                } else if ("IO".equals(contest.getMode())) {
                    TestTaskVO taskData = runResult.getData();
                    int passedCases = (taskData != null) ? taskData.getFailAt() : 0;
                    int totalCases = problemStatus.getCaseAmount();
                    double ratio = (double) passedCases / totalCases;
                    userScore = (int) Math.ceil(ratio * totalScore);
                } else {
                    return Result.failure(ContestErrorCode.SERVICE_ERROR, null);
                }
            }

            BoardDTO boardDTO = mybatisRepos.getContestBoardByUserAndProblem(contestName, userName, problemName);

            boolean isCurrentAc = runResult.getCode().equals("0");
            int statusAc = 1; // 已通过
            int statusUnsolved = 0; // 未通过

            if (boardDTO == null) {
                boardDTO = new BoardDTO();
                boardDTO.setContestName(contestName);
                boardDTO.setUserName(userName);
                boardDTO.setNickName(nickName);
                boardDTO.setProblemName(problemName);
                boardDTO.setScore(userScore);
                boardDTO.setTryCount(1); // 第一次尝试
                boardDTO.setCreateTime(LocalDateTime.now());

                if (isCurrentAc) {
                    boardDTO.setStatus(statusAc);
                } else {
                    boardDTO.setStatus(statusUnsolved);
                }

                Integer res = mybatisRepos.insertContestBoard(boardDTO);
                if (res == null || res <= 0) {
                    return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
                }

            } else {
                if ("ACM".equals(contest.getMode())) {
                    if (boardDTO.getStatus() == statusAc) {
                        return Result.success(null);
                    }

                    if (isCurrentAc) {
                        boardDTO.setScore(userScore);
                        boardDTO.setStatus(statusAc);
                        boardDTO.setTryCount(boardDTO.getTryCount() + 1); // 尝试次数+1
                         boardDTO.setAcTime(LocalDateTime.now());
                    }
                    else {
                        boardDTO.setTryCount(boardDTO.getTryCount() + 1); // 仅增加错误次数
                    }

                } else {
                    boardDTO.setScore(Math.max(boardDTO.getScore(), userScore));
                    if (boardDTO.getScore() == totalScore) {
                        boardDTO.setStatus(statusAc);
                    }
                    boardDTO.setTryCount(boardDTO.getTryCount() + 1);
                }
                Integer res = mybatisRepos.updateContestBoard(boardDTO);
                if (res == null || res <= 0) {
                    return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
                }
                return runResult;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
        }
        return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
    }
}
