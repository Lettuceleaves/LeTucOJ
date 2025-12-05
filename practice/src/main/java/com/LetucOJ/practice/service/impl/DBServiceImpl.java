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
import com.LetucOJ.practice.model.DTO.TestCaseDTO;
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
import java.util.*;
import java.util.function.Supplier;

@Service
@Data
@AllArgsConstructor
public class DBServiceImpl implements DBService {

    private final MybatisRepos mybatisRepos;
    private final MinioRepos minioRepos;
    private final RunClient runClient;

    @Override
    public ResultVO<ProblemListVO> getList(ListConditionDTO listConditionDTO, String name, String role) {
        return executeSafe(() -> {
            if (listConditionDTO.getStart() == null || listConditionDTO.getLimit() == null) {
                return Result.failure(PracticeErrorCode.CLIENT_ERROR, null);
            }
            // 传入获取总数的逻辑
            return doGetProblemList(listConditionDTO, name, role,
                    () -> mybatisRepos.getAmount(listConditionDTO));
        });
    }

    @Override
    public ResultVO<ProblemListVO> searchList(ListConditionDTO listConditionDTO, String name, String role) {
        return executeSafe(() -> {
            if (listConditionDTO.getStart() == null || listConditionDTO.getLimit() == null) {
                return Result.failure(BaseErrorCode.CLIENT_ERROR, null);
            }

            // 设置默认排序逻辑
            String order = listConditionDTO.getOrder();
            if (order == null || order.isEmpty() ||
                    (!Objects.equals(order, "lang") && !Objects.equals(order, "difficulty") && !Objects.equals(order, "cnname"))) {
                listConditionDTO.setOrder("lang");
            }

            // 传入获取搜索总数的逻辑
            return doGetProblemList(listConditionDTO, name, role,
                    () -> mybatisRepos.getSearchAmount(listConditionDTO));
        });
    }

    @Override
    public ResultVO<Problem> getProblem(String name, String role) {
        return executeSafe(() -> {
            Problem dbDto = mybatisRepos.getProblem(name);
            if (dbDto == null) {
                return Result.failure(BaseErrorCode.PROBLEM_NOT_EXIST, null);
            } else if (!Boolean.TRUE.equals(dbDto.getShowsolution())) {
                dbDto.setSolution("题解已隐藏");
            }
            return Result.success(dbDto);
        });
    }

    @Override
    public ResultVO<Void> insertProblem(Problem dto) {
        dto.setCreatetime(new Date(System.currentTimeMillis()));
        return executeDbUpdate(() -> mybatisRepos.insertProblem(dto));
    }

    @Override
    public ResultVO<Void> updateProblem(Problem dto) {
        return executeDbUpdate(() -> mybatisRepos.updateProblem(dto));
    }

    @Override
    public ResultVO<Void> deleteProblem(String name) {
        // 原逻辑直接返回错误
        return Result.failure(BaseErrorCode.SERVICE_ERROR);
    }

    @Override
    public ResultVO<TestTaskVO> testCase(TestCaseDTO testCaseDTO, String language) {
        String input = testCaseDTO.getInput();
        String code = testCaseDTO.getCode();
        String problemName = testCaseDTO.getProblemName();
        if (input == null || code == null || problemName == null) {
            return Result.failure(BaseErrorCode.CLIENT_ERROR, null);
        }
        Problem exist = mybatisRepos.getProblem(problemName);
        if (exist == null) {
            return Result.failure(BaseErrorCode.PROBLEM_NOT_EXIST, null);
        }
        return runClient.runTestCase(new TestCaseDTO(problemName, code, input));
    }

    @Override
    @Transactional
    public ResultVO<Void> saveCase(CaseFile CaseFile) {
        return executeSafe(() -> {
            String name = CaseFile.getName();
            String input = CaseFile.getInput();
            String output = CaseFile.getOutput();

            if (input == null || output == null) {
                return Result.failure(BaseErrorCode.CLIENT_ERROR);
            }
            Integer result = mybatisRepos.incrementCaseAmount(name);
            if (result == null || result <= 0) {
                return Result.failure(BaseErrorCode.SERVICE_ERROR);
            }
            ProblemStatus problemStatus = mybatisRepos.getStatus(name);
            if (problemStatus == null) {
                return Result.failure(BaseErrorCode.SERVICE_ERROR);
            }
            String bucketName = "letucoj";
            String inputObjectName = "problems" + "/" + name + "/input/" + (problemStatus.getCaseAmount() + 1) + ".txt";
            String outputObjectName = "problems" + "/" + name + "/output/" + (problemStatus.getCaseAmount() + 1) + ".txt";

            minioRepos.addFile(bucketName, inputObjectName, input.getBytes());
            minioRepos.addFile(bucketName, outputObjectName, output.getBytes());
            return Result.success();
        });
    }

    @Override
    public ResultVO<SubmitRecordListVO> submitRecordListByName(String userName, int start, int limit) {
        return executeSafe(() -> doGetSubmitRecordList(
                () -> mybatisRepos.getRecordsByName(userName, start, limit),
                () -> mybatisRepos.getRecordsByNameCount(userName)
        ));
    }

    @Override
    public ResultVO<SubmitRecordListVO> submitRecordListAll(int start, int limit) {
        return executeSafe(() -> doGetSubmitRecordList(
                () -> mybatisRepos.getAllRecords(start, limit),
                mybatisRepos::getAllRecordsCount
        ));
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

    // ================= 私有辅助方法 =================

    /**
     * 统一处理 try-catch-log 逻辑
     */
    private <T> ResultVO<T> executeSafe(Supplier<ResultVO<T>> action) {
        try {
            return action.get();
        } catch (Exception e) {
            Logger.log(Type.SERVER, LogLevel.ERROR, e.getMessage());
            return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
        }
    }

    /**
     * 提取 getList 和 searchList 的公共核心逻辑
     */
    private ResultVO<ProblemListVO> doGetProblemList(ListConditionDTO condition, String name, String role, Supplier<Integer> countSupplier) {
        Integer amount = countSupplier.get();

        List<ProblemBrief> list;
        if ("USER".equals(role)) {
            list = mybatisRepos.getList(condition);
        } else {
            list = mybatisRepos.getListInRoot(condition);
        }

        // 校验逻辑
        if (list == null || list.isEmpty()) {
            return Result.failure(BaseErrorCode.PROBLEM_NOT_EXIST, null);
        } else if (amount == null || amount < 0) {
            return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
        }

        // 处理 AC 状态
        Set<String> acceptedSet = mybatisRepos.getCorrectByName(name);
        if (acceptedSet != null && !acceptedSet.isEmpty()) {
            for (ProblemBrief item : list) {
                if (acceptedSet.contains(item.getName())) {
                    item.setAccepted(1);
                }
            }
        }

        return Result.success(new ProblemListVO(list, amount));
    }

    /**
     * 提取 insert 和 update 的公共数据库操作逻辑
     */
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

    /**
     * 提取 submitRecordListByName 和 submitRecordListAll 的公共逻辑
     */
    private ResultVO<SubmitRecordListVO> doGetSubmitRecordList(Supplier<List<SubmitRecordDTO>> recordsSupplier, Supplier<Integer> countSupplier) {
        List<SubmitRecordDTO> records = recordsSupplier.get();
        Integer amount = countSupplier.get();

        if (records == null || records.isEmpty()) {
            return Result.failure(PracticeErrorCode.NO_RECORD_FOUND, null);
        } else if (amount == null || amount < 0) {
            return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
        }
        return Result.success(new SubmitRecordListVO(records, amount));
    }
}