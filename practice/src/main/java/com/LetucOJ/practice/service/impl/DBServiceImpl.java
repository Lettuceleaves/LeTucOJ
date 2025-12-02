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
import com.LetucOJ.practice.model.TestCaseDTO;
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

@Service
@Data
@AllArgsConstructor
public class DBServiceImpl implements DBService {

    private MybatisRepos mybatisRepos;

    private MinioRepos minioRepos;

    private RunClient runClient;

    public ResultVO<ProblemListVO> getList(ListConditionDTO listConditionDTO, String name) {

        try {

            if (listConditionDTO.getStart() == null || listConditionDTO.getLimit() == null) {
                return Result.failure(BaseErrorCode.CLIENT_ERROR, null);
            }

            Integer amount = mybatisRepos.getAmount(listConditionDTO);
            List<ProblemBrief> list = mybatisRepos.getList(listConditionDTO);
            Set<String> acceptedSet = mybatisRepos.getCorrectByName(name);

            if (list == null || list.isEmpty()) {
                return Result.failure(BaseErrorCode.PROBLEM_NOT_EXIST, null);
            } else if (amount == null || amount < 0) {
                return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
            }

            for (ProblemBrief item : list) {
                if (acceptedSet.contains(item.getName())) {
                    item.setAccepted(1);
                }
            }

            return Result.success(new ProblemListVO(list, amount));
        } catch (Exception e) {
            Logger.log(Type.SERVER, LogLevel.ERROR, e.getMessage());
            return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
        }
    }

    public ResultVO<ProblemListVO> getListInRoot(ListConditionDTO dto, String name) {

        try {

            if (dto.getStart() == null || dto.getLimit() == null) {
                return Result.failure(BaseErrorCode.CLIENT_ERROR, null);
            }

            Integer amount = mybatisRepos.getAmountInRoot(dto);
            List<ProblemBrief> list = mybatisRepos.getListInRoot(dto);
            Set<String> acceptedSet = mybatisRepos.getCorrectByName(name);

            if (list == null || list.isEmpty()) {
                return Result.failure(BaseErrorCode.PROBLEM_NOT_EXIST, null);
            } else if (amount == null || amount < 0) {
                return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
            }

            for (ProblemBrief item : list) {
                if (acceptedSet.contains(item.getName())) {
                    item.setAccepted(1);
                }
            }

            return Result.success(new ProblemListVO(list, amount));
        } catch (Exception e) {
            Logger.log(Type.SERVER, LogLevel.ERROR, e.getMessage());
            return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
        }
    }

    @Override
    public ResultVO<ProblemListVO> searchList(ListConditionDTO dto, String name) {
        try {
            if (dto.getStart() == null || dto.getLimit() == null) {
                return Result.failure(BaseErrorCode.CLIENT_ERROR, null);
            }

            if (dto.getOrder() == null || dto.getOrder().isEmpty()) {
                dto.setOrder("lang");
            }

            if (!Objects.equals(dto.getOrder(), "lang") && !Objects.equals(dto.getOrder(), "difficulty") && !Objects.equals(dto.getOrder(), "cnname")) {
                dto.setOrder("lang");
            }

            Integer amount = mybatisRepos.getSearchAmount(dto);
            List<ProblemBrief> list = mybatisRepos.searchList(dto);
            Set<String> acceptedSet = mybatisRepos.getCorrectByName(name);

            if (list == null || list.isEmpty()) {
                return Result.failure(BaseErrorCode.PROBLEM_NOT_EXIST, null);
            } else if (amount == null || amount < 0) {
                return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
            }

            for (ProblemBrief item : list) {
                if (acceptedSet.contains(item.getName())) {
                    item.setAccepted(1);
                }
            }

            return Result.success(new ProblemListVO(list, amount));
        } catch (Exception e) {
            Logger.log(Type.SERVER, LogLevel.ERROR, e.getMessage());
            return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
        }
    }

    @Override
    public ResultVO<ProblemListVO> searchListInRoot(ListConditionDTO dto, String name) {
        try {
            if (dto.getStart() == null || dto.getLimit() == null) {
                return Result.failure(BaseErrorCode.CLIENT_ERROR, null);
            }

            if (dto.getOrder() == null || dto.getOrder().isEmpty()) {
                dto.setOrder("lang");
            }

            if (!Objects.equals(dto.getOrder(), "lang") && !Objects.equals(dto.getOrder(), "difficulty") && !Objects.equals(dto.getOrder(), "cnname")) {
                dto.setOrder("lang");
            }

            Integer amount = mybatisRepos.getSearchAmountInRoot(dto);
            List<ProblemBrief> list = mybatisRepos.searchListInRoot(dto);
            Set<String> acceptedSet = mybatisRepos.getCorrectByName(name);

            if (list == null || list.isEmpty()) {
                return Result.failure(BaseErrorCode.PROBLEM_NOT_EXIST, null);
            } else if (amount == null || amount < 0) {
                return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
            }

            for (ProblemBrief item : list) {
                if (acceptedSet.contains(item.getName())) {
                    item.setAccepted(1);
                }
            }

            return Result.success(new ProblemListVO(list, amount));
        } catch (Exception e) {
            Logger.log(Type.SERVER, LogLevel.ERROR, e.getMessage());
            return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
        }
    }

    public ResultVO<Problem> getProblem(String name) {

        try {

            Problem dbDto = mybatisRepos.getProblem(name);

            if (dbDto == null) {
                return Result.failure(BaseErrorCode.PROBLEM_NOT_EXIST, null);
            } else if (dbDto.getShowsolution() == true) {
                return Result.success(dbDto);
            } else {
                dbDto.setSolution("题解已隐藏");
                return Result.success(dbDto);
            }
        } catch (Exception e) {
            Logger.log(Type.SERVER, LogLevel.ERROR, e.getMessage());
            return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
        }
    }

    public ResultVO<Problem> getProblemInRoot(String name) {

        try {

            Problem dbDto = mybatisRepos.getProblemInRoot(name);

            if (dbDto == null) {
                return Result.failure(BaseErrorCode.PROBLEM_NOT_EXIST, null);
            } else {
                return Result.success(dbDto);
            }
        } catch (Exception e) {
            Logger.log(Type.SERVER, LogLevel.ERROR, e.getMessage());
            return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
        }
    }
    public ResultVO<Void> insertProblem(Problem dto) {
        dto.setCreatetime(new Date(System.currentTimeMillis()));

        try {
            Integer rows = mybatisRepos.insertProblem(dto);
            if (rows != null && rows > 0) {
                return Result.success();
            } else {
                return Result.failure(BaseErrorCode.SERVICE_ERROR);
            }
        } catch (Exception e) {
            Logger.log(Type.SERVER, LogLevel.ERROR, e.getMessage());
            return Result.failure(BaseErrorCode.SERVICE_ERROR);
        }
    }

    public ResultVO<Void> updateProblem(Problem dto) {
        try {
            Integer rows = mybatisRepos.updateProblem(dto);

            if (rows != null && rows > 0) {
                return Result.success();
            } else {
                return Result.failure(BaseErrorCode.SERVICE_ERROR);
            }
        } catch (Exception e) {
            Logger.log(Type.SERVER, LogLevel.ERROR, e.getMessage());
            return Result.failure(BaseErrorCode.SERVICE_ERROR);
        }
    }

    public ResultVO<Void> deleteProblem(String name) {
        return Result.failure(BaseErrorCode.SERVICE_ERROR);
    }

    public ResultVO<TestTaskVO> testCase(TestCaseDTO testCaseDTO, String language) {
        String input = testCaseDTO.getInput();
        String code = testCaseDTO.getCode();
        String questionName = testCaseDTO.getQuestionName();
        if (input == null || code == null || questionName == null) {
            return Result.failure(BaseErrorCode.CLIENT_ERROR, null);
        }
        Problem exist = mybatisRepos.getProblem(questionName);
        if (exist == null) {
            return Result.failure(BaseErrorCode.PROBLEM_NOT_EXIST, null);
        }
        List<String> inputs = new ArrayList<>();
        inputs.add(input);
        return runClient.run(new TestTaskDTO(code, inputs, language, questionName));
    }

    @Transactional
    public ResultVO<Void> saveCase(CaseFile CaseFile) {
        String name = CaseFile.getName();
        String input = CaseFile.getInput();
        String output = CaseFile.getOutput();
        try {
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
            String inputObjectName = "problems" + "/" + name + "/input/" + problemStatus.getCaseAmount() + 1 + ".txt";
            String outputObjectName = "problems" + "/" + name + "/output/" + problemStatus.getCaseAmount() + 1 + ".txt";
//            String configObjectName = "problems" + "/" + name + "/config.yaml";
//            minioRepos.addFile(bucketName, configObjectName, config);
            minioRepos.addFile(bucketName, inputObjectName, input.getBytes());
            minioRepos.addFile(bucketName, outputObjectName, output.getBytes());
            return Result.success();
        } catch (Exception e) {
            Logger.log(Type.SERVER, LogLevel.ERROR, e.getMessage());
            return Result.failure(BaseErrorCode.SERVICE_ERROR);
        }
    }

    @Override
    public ResultVO<SubmitRecordListVO> submitRecordListByName(String userName, int start, int limit) {
        try {
            List<SubmitRecord> records = mybatisRepos.getRecordsByName(userName, start, limit);
            Integer amount = mybatisRepos.getRecordsByNameCount(userName);
            if (records == null || records.isEmpty()) {
                return Result.failure(PracticeErrorCode.NO_RECORD_FOUND, null);
            } else if (amount == null || amount < 0) {
                return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
            }
            return Result.success(new SubmitRecordListVO(records, amount));
        } catch (Exception e) {
            Logger.log(Type.SERVER, LogLevel.ERROR, e.getMessage());
            return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
        }
    }

    @Override
    public ResultVO<SubmitRecordListVO> submitRecordListAll(int start, int limit) {
        try {
            List<SubmitRecord> records = mybatisRepos.getAllRecords(start, limit);
            Integer amount = mybatisRepos.getAllRecordsCount();
            if (records == null || records.isEmpty()) {
                return Result.failure(PracticeErrorCode.NO_RECORD_FOUND, null);
            } else if (amount == null || amount < 0) {
                return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
            }
            return Result.success(new SubmitRecordListVO(records, amount));
        } catch (Exception e) {
            return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
        }
    }

    @Override
    public ResultVO<CaseFile> getCase(String questionName, Integer id) {
        try {
            byte[] inputFile = minioRepos.getFile("letucoj", "problems/" + questionName + "/input/" + id + ".txt");
            byte[] outputFile = minioRepos.getFile("letucoj", "problems/" + questionName + "/output/" + id + ".txt");
            if (inputFile == null || outputFile == null) {
                return Result.failure(PracticeErrorCode.CASE_NOT_EXIST, null);
            }
            return Result.success(new CaseFile(questionName, new String(inputFile), new String(outputFile)));
        } catch (Exception e) {
            return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
        }
    }

    @Override
    public ResultVO<byte[]> getConfigFile(String qname) {
        try {
            byte[] configFile = minioRepos.getFile("letucoj", "problems/" + qname + "/config.yaml");
            if (configFile == null) {
                return Result.failure(PracticeErrorCode.CONFIG_NOT_EXIST, null);
            }
            return Result.success(configFile);
        } catch (Exception e) {
            return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
        }
    }
}
