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
import com.LetucOJ.practice.model.*;
import com.LetucOJ.practice.model.DTO.*;
import com.LetucOJ.practice.model.VO.ProblemListVO;
import com.LetucOJ.practice.model.VO.SubmitRecordListVO;
import com.LetucOJ.practice.model.VO.TestTaskVO;
import com.LetucOJ.practice.repos.MybatisRepos;
import com.LetucOJ.practice.service.DBService;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

@Service
@Data
@AllArgsConstructor
public class DBServiceImpl implements DBService {

    private final MybatisRepos mybatisRepos;
    private final MinioRepos minioRepos;
    private final RunClient runClient;

    @Override
    public ResultVO<ProblemListVO> getList(ListConditionDTO listConditionDTO, String userName, String role) {
         listConditionDTO.setLike(null);
        return doQueryProblemList(listConditionDTO, userName, role);
    }

    @Override
    public ResultVO<ProblemListVO> searchList(ListConditionDTO listConditionDTO, String userName, String role) {
        return doQueryProblemList(listConditionDTO, userName, role);
    }

    private ResultVO<ProblemListVO> doQueryProblemList(ListConditionDTO dto, String userName, String role) {
        return executeSafe(() -> {
            if (dto.getStart() == null || dto.getLimit() == null) {
                return Result.failure(PracticeErrorCode.CLIENT_ERROR, null);
            }

            // 1. 默认排序处理
            if (dto.getOrder() == null || dto.getOrder().trim().isEmpty()) {
                dto.setOrder("difficulty");
            }
            // TODO: 增加 Order 字段的白名单校验，防止 SQL 注入

            // 设置 DTO 标志位
            boolean isUser = "USER".equals(role);
            dto.setOnlyPublic(isUser);

            List<ProblemBrief> list = mybatisRepos.selectProblemList(dto);
            Integer amount = mybatisRepos.countProblemList(dto);

            if (list == null || list.isEmpty()) {
                // 如果是翻页导致的空，也应该返回成功，只是列表为空
                if (amount != null && amount > 0) {
                    return Result.success(new ProblemListVO(list, amount));
                }
                return Result.failure(BaseErrorCode.PROBLEM_NOT_EXIST, null);
            }

            // 填充 AC 状态
            if (userName != null && !userName.isEmpty()) {
                Set<String> acceptedSet = mybatisRepos.getCorrectByName(userName);
                if (acceptedSet != null && !acceptedSet.isEmpty()) {
                    for (ProblemBrief item : list) {
                        if (acceptedSet.contains(item.getProblemName())) {
                            item.setAccepted(1);
                        }
                    }
                }
            }

            return Result.success(new ProblemListVO(list, amount));
        });
    }

    @Override
    public ResultVO<Problem> getProblem(String name, String role) {
        return executeSafe(() -> {
            // 区分前台后台：前台只查 public，后台查所有
            boolean onlyPublic = "USER".equals(role);

            Problem problem = mybatisRepos.selectProblemDetail(name, onlyPublic);

            if (problem == null) {
                return Result.failure(BaseErrorCode.PROBLEM_NOT_EXIST, null);
            }

            // 处理题解隐藏逻辑
            if (!Boolean.TRUE.equals(problem.getShowSolution())) {
                problem.setSolution("题解已隐藏");
            }

            return Result.success(problem);
        });
    }

    @Override
    public ResultVO<Void> insertProblem(Problem dto) {
        dto.setCreateTime(new Date(System.currentTimeMillis()));
        return executeDbUpdate(() -> mybatisRepos.insertProblem(dto));
    }

    @Override
    public ResultVO<Void> updateProblem(Problem dto) {
        return executeDbUpdate(() -> mybatisRepos.updateProblem(dto));
    }

    @Override
    public ResultVO<Void> deleteProblem(String name) {
        return executeSafe(() -> {
            mybatisRepos.deleteProblem(name);
            return Result.success();
        });
    }

    @Override
    public ResultVO<SubmitRecordListVO> submitRecordListByName(String userName, int start, int limit) {
        // 查特定用户
        return executeSafe(() -> queryRecords(userName, start, limit));
    }

    @Override
    public ResultVO<SubmitRecordListVO> submitRecordListAll(int start, int limit) {
        // 查所有用户 (传 null)
        return executeSafe(() -> queryRecords(null, start, limit));
    }

    private ResultVO<SubmitRecordListVO> queryRecords(String userName, int start, int limit) {
        List<SubmitRecordDTO> records = mybatisRepos.selectRecordList(userName, start, limit);
        Integer amount = mybatisRepos.countRecordList(userName);

        if (records == null || records.isEmpty()) {
            return Result.failure(PracticeErrorCode.NO_RECORD_FOUND, null);
        }
        return Result.success(new SubmitRecordListVO(records, amount));
    }

    @Override
    public ResultVO<TestTaskVO> testCase(TestCaseDTO testCaseDTO, String language, String role) {
        String input = testCaseDTO.getInput();
        String code = testCaseDTO.getCode();
        String problemName = testCaseDTO.getProblemName();

        if (input == null || code == null || problemName == null) {
            return Result.failure(BaseErrorCode.CLIENT_ERROR, null);
        }

        Boolean onlyPublic = "USER".equals(role);
        Problem exist = mybatisRepos.selectProblemDetail(problemName, onlyPublic);

        if (exist == null) {
            return Result.failure(BaseErrorCode.PROBLEM_NOT_EXIST, null);
        }
        return runClient.runTestCase(new TestCaseDTO(problemName, code, input));
    }

    @Override
    @Transactional
    public ResultVO<Void> saveCase(CaseFile CaseFile) {
        return executeSafe(() -> {
            String name = CaseFile.getProblemName();
            String input = CaseFile.getInput();
            String output = CaseFile.getOutput();

            if (input == null || output == null) {
                return Result.failure(BaseErrorCode.CLIENT_ERROR);
            }

            // 增加用例计数
            Integer result = mybatisRepos.incrementCaseAmount(name);
            if (result == null || result <= 0) {
                return Result.failure(BaseErrorCode.SERVICE_ERROR);
            }

            ProblemStatus problemStatus = mybatisRepos.getStatus(name);
            if (problemStatus == null) {
                return Result.failure(BaseErrorCode.SERVICE_ERROR);
            }

            String bucketName = "letucoj";
            String inputObjectName = "problems/" + name + "/input/" + problemStatus.getCaseAmount() + ".txt";
            String outputObjectName = "problems/" + name + "/output/" + problemStatus.getCaseAmount() + ".txt";

            minioRepos.addFile(bucketName, inputObjectName, input.getBytes());
            minioRepos.addFile(bucketName, outputObjectName, output.getBytes());
            return Result.success();
        });
    }

    @Override
    public ResultVO<CaseFile> getCase(String problemName, Integer id) {
        return executeSafe(() -> {
            byte[] inputFile = minioRepos.getFile("letucoj", "problems/" + problemName + "/input/" + id + ".txt");
            byte[] outputFile = minioRepos.getFile("letucoj", "problems/" + problemName + "/output/" + id + ".txt");
            if (inputFile == null || outputFile == null) {
                return Result.failure(PracticeErrorCode.CASE_NOT_EXIST, null);
            }
            return Result.success(new CaseFile(problemName, new String(inputFile), new String(outputFile)));
        });
    }

    @Override
    public ResultVO<byte[]> getConfigFile(String qname) {
        return executeSafe(() -> {
            byte[] configFile = minioRepos.getFile("letucoj", "problems/" + qname + "/config.yaml");
            if (configFile == null) {
                return Result.failure(PracticeErrorCode.CONFIG_NOT_EXIST, null);
            }
            return Result.success(configFile);
        });
    }

    // ================= 辅助方法 =================

    private <T> ResultVO<T> executeSafe(Supplier<ResultVO<T>> action) {
        try {
            return action.get();
        } catch (Exception e) {
            Logger.log(Type.SERVER, LogLevel.ERROR, e.getMessage());
            return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
        }
    }

    private ResultVO<Void> executeDbUpdate(Supplier<Integer> dbAction) {
        return executeSafe(() -> {
            Integer rows = dbAction.get();
            if (rows != null && rows > 0) {
                return Result.success();
            } else {
                return Result.failure(BaseErrorCode.SERVICE_ERROR);
            }
        });
    }
}