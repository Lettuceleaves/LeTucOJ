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

            // 1. 默认排序处理：如果为空，默认按 difficulty 排序
            String order = listConditionDTO.getOrder();
            if (order == null || order.trim().isEmpty()) {
                listConditionDTO.setOrder("difficulty");
            }

            // 2. 根据角色准备查询策略
            Supplier<List<ProblemBrief>> listSupplier;
            Supplier<Integer> countSupplier;

            if ("USER".equals(role)) {
                listSupplier = () -> mybatisRepos.getList(listConditionDTO);
                countSupplier = () -> mybatisRepos.getAmount(listConditionDTO);
            } else {
                listSupplier = () -> mybatisRepos.getListInRoot(listConditionDTO);
                // 注意：这里应该用 getAmountInRoot，因为Root能看到隐藏题目，总数不一样
                countSupplier = () -> mybatisRepos.getAmountInRoot(listConditionDTO);
            }

            // 3. 执行公共逻辑
            return doGetProblemList(name, listSupplier, countSupplier);
        });
    }

    @Override
    public ResultVO<ProblemListVO> searchList(ListConditionDTO listConditionDTO, String name, String role) {
        return executeSafe(() -> {
            if (listConditionDTO.getStart() == null || listConditionDTO.getLimit() == null) {
                return Result.failure(BaseErrorCode.CLIENT_ERROR, null);
            }

            // 1. 默认排序处理：防止SQL报错
            String order = listConditionDTO.getOrder();
            if (order == null || order.trim().isEmpty()) {
                listConditionDTO.setOrder("difficulty");
            }
            // 你的原逻辑允许 "lang" "cnname" 等，这里为了防止SQL注入建议做白名单校验，
            // 但既然要求默认difficulty，上面代码已满足 "为空就用difficulty"

            // 2. 根据角色准备查询策略 (使用 searchList 系列方法)
            Supplier<List<ProblemBrief>> listSupplier;
            Supplier<Integer> countSupplier;

            if ("USER".equals(role)) {
                listSupplier = () -> mybatisRepos.searchList(listConditionDTO);
                countSupplier = () -> mybatisRepos.getSearchAmount(listConditionDTO);
            } else {
                listSupplier = () -> mybatisRepos.searchListInRoot(listConditionDTO);
                countSupplier = () -> mybatisRepos.getSearchAmountInRoot(listConditionDTO);
            }

            // 3. 执行公共逻辑
            return doGetProblemList(name, listSupplier, countSupplier);
        });
    }

    // ... (getProblem, insertProblem 等其他方法保持不变，省略以节省篇幅) ...
    // ... (testCase, saveCase, submitRecordList 等方法保持不变) ...

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

    private <T> ResultVO<T> executeSafe(Supplier<ResultVO<T>> action) {
        try {
            return action.get();
        } catch (Exception e) {
            Logger.log(Type.SERVER, LogLevel.ERROR, e.getMessage());
            return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
        }
    }

    /**
     * 重构后的核心方法：将列表查询逻辑 (listSupplier) 从外部传入，
     * 从而复用于 getList (普通列表) 和 searchList (带LIKE的搜索)
     */
    private ResultVO<ProblemListVO> doGetProblemList(String name, Supplier<List<ProblemBrief>> listSupplier, Supplier<Integer> countSupplier) {
        // 1. 获取总数
        Integer amount = countSupplier.get();

        // 2. 获取列表 (具体是 getList 还是 searchList 由外部传入的 Supplier 决定)
        List<ProblemBrief> list = listSupplier.get();

        // 3. 基础校验
        if (list == null || list.isEmpty()) {
            return Result.failure(BaseErrorCode.PROBLEM_NOT_EXIST, null);
        } else if (amount == null || amount < 0) {
            return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
        }

        // 4. 填充 AC (已通过) 状态
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