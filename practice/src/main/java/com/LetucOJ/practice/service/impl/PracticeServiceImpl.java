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
import com.LetucOJ.practice.model.VO.TestTaskVO;
import com.LetucOJ.practice.repos.MybatisRepos;
import com.LetucOJ.practice.service.PracticeService;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Service;


@Service
@Data
@AllArgsConstructor
public class PracticeServiceImpl implements PracticeService {

    private RunClient runClient;

    private MinioRepos minioRepos;

    private MybatisRepos mybatisRepos;

    public ResultVO<TestTaskVO> submit(String userName, String problemName, String code, String language, String role) {
        try {

            ProblemStatus problemStatus = mybatisRepos.getStatus(problemName);

            if (problemStatus == null) {
                return Result.failure(BaseErrorCode.PROBLEM_NOT_EXIST, null);
            }

            if (problemStatus.getCaseAmount() <= 0) {
                return Result.failure(BaseErrorCode.NO_CASE_EXIST, null);
            }

            if (!problemStatus.isPublicProblem() && role.equals("USER")) {
                return Result.failure(PracticeErrorCode.NOT_PUBLIC, null);
            }

            ResultVO<TestTaskVO> runResult = runClient.runTestTask(new TestTaskDTO(problemName, language, code, problemStatus.getCaseAmount()));

            if (runResult.getCode().equals("0")) {
                Integer res = mybatisRepos.insertCorrect(userName, problemName);
                if (res == null) {
                    return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
                }
                return Result.success(null);
            }

            return runResult;
        } catch (Exception e) {
            Logger.log(Type.SERVER, LogLevel.ERROR, e.getMessage());
            return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
        }
    }
}
