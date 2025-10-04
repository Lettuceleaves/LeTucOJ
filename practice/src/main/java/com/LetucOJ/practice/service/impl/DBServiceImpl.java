package com.LetucOJ.practice.service.impl;

import com.LetucOJ.common.oss.MinioRepos;
import com.LetucOJ.common.result.Result;
import com.LetucOJ.common.result.ResultVO;
import com.LetucOJ.common.result.errorcode.BaseErrorCode;
import com.LetucOJ.common.result.errorcode.PracticeErrorCode;
import com.LetucOJ.practice.client.RunClient;
import com.LetucOJ.practice.model.*;
import com.LetucOJ.practice.repos.MybatisRepos;
import com.LetucOJ.practice.service.DBService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.*;

@Service
public class DBServiceImpl implements DBService {

    @Autowired
    private MybatisRepos mybatisRepos;

    @Autowired
    private MinioRepos minioRepos;

    @Autowired
    private RunClient runClient;

    public ResultVO getList(ListServiceDTO dto, String name) {

        try {

            if (dto.getStart() == null || dto.getLimit() == null) {
                return Result.failure(BaseErrorCode.CLIENT_ERROR);
            }

            Integer amount = mybatisRepos.getAmount(dto);
            List<ListDTO> list = mybatisRepos.getList(dto);
            Set<String> acceptedSet = mybatisRepos.getCorrectByName(name);

            if (list == null || list.isEmpty()) {
                return Result.failure(BaseErrorCode.PROBLEM_NOT_EXIST);
            } else if (amount == null || amount < 0) {
                return Result.failure(BaseErrorCode.SERVICE_ERROR);
            }

            for (ListDTO item : list) {
                if (acceptedSet.contains(item.getName())) {
                    item.setAccepted(1);
                }
            }

            return Result.success(Map.of(
                "list",   list,
                "amount", amount
            ));
        } catch (Exception e) {
            return Result.failure(BaseErrorCode.SERVICE_ERROR);
        }
    }

    public ResultVO getListInRoot(ListServiceDTO dto, String name) {

        try {

            if (dto.getStart() == null || dto.getLimit() == null) {
                return Result.failure(BaseErrorCode.CLIENT_ERROR);
            }

            Integer amount = mybatisRepos.getAmountInRoot(dto);
            List<ListDTO> list = mybatisRepos.getListInRoot(dto);
            Set<String> acceptedSet = mybatisRepos.getCorrectByName(name);

            if (list == null || list.isEmpty()) {
                return Result.failure(BaseErrorCode.PROBLEM_NOT_EXIST);
            } else if (amount == null || amount < 0) {
                return Result.failure(BaseErrorCode.SERVICE_ERROR);
            }

            for (ListDTO item : list) {
                if (acceptedSet.contains(item.getName())) {
                    item.setAccepted(1);
                }
            }

            return Result.success(
                    Map.of(
                            "list",   list,
                            "amount", amount
                    ));
        } catch (Exception e) {
            return Result.failure(BaseErrorCode.SERVICE_ERROR);
        }
    }

    @Override
    public ResultVO searchList(ListServiceDTO dto, String name) {
        try {
            if (dto.getStart() == null || dto.getLimit() == null) {
                return Result.failure(BaseErrorCode.CLIENT_ERROR);
            }

            if (dto.getOrder() == null || dto.getOrder().isEmpty()) {
                dto.setOrder("name");
            }

            if (!Objects.equals(dto.getOrder(), "name") && !Objects.equals(dto.getOrder(), "difficulty") && !Objects.equals(dto.getOrder(), "cnname")) {
                dto.setOrder("name");
            }

            Integer amount = mybatisRepos.getSearchAmount(dto);
            List<ListDTO> list = mybatisRepos.searchList(dto);
            Set<String> acceptedSet = mybatisRepos.getCorrectByName(name);

            if (list == null || list.isEmpty()) {
                return Result.failure(BaseErrorCode.PROBLEM_NOT_EXIST);
            } else if (amount == null || amount < 0) {
                return Result.failure(BaseErrorCode.SERVICE_ERROR);
            }

            for (ListDTO item : list) {
                if (acceptedSet.contains(item.getName())) {
                    item.setAccepted(1);
                }
            }

            return Result.success(
                    Map.of(
                            "list",   list,
                            "amount", amount
                    ));
        } catch (Exception e) {
            return Result.failure(BaseErrorCode.SERVICE_ERROR);
        }
    }

    @Override
    public ResultVO searchListInRoot(ListServiceDTO dto, String name) {
        try {
            if (dto.getStart() == null || dto.getLimit() == null) {
                return Result.failure(BaseErrorCode.CLIENT_ERROR);
            }

            if (dto.getOrder() == null || dto.getOrder().isEmpty()) {
                dto.setOrder("name");
            }

            if (!Objects.equals(dto.getOrder(), "name") && !Objects.equals(dto.getOrder(), "difficulty") && !Objects.equals(dto.getOrder(), "cnname")) {
                dto.setOrder("name");
            }

            Integer amount = mybatisRepos.getSearchAmountInRoot(dto);
            List<ListDTO> list = mybatisRepos.searchListInRoot(dto);
            Set<String> acceptedSet = mybatisRepos.getCorrectByName(name);

            if (list == null || list.isEmpty()) {
                return Result.failure(BaseErrorCode.PROBLEM_NOT_EXIST);
            } else if (amount == null || amount < 0) {
                return Result.failure(BaseErrorCode.SERVICE_ERROR);
            }

            for (ListDTO item : list) {
                if (acceptedSet.contains(item.getName())) {
                    item.setAccepted(1);
                }
            }

            return Result.success(
                    Map.of(
                            "list",   list,
                            "amount", amount
                    ));
        } catch (Exception e) {
            return Result.failure(BaseErrorCode.SERVICE_ERROR);
        }
    }

    public ResultVO getProblem(String name) {

        try {

            FullInfoDTO dbDto = mybatisRepos.getProblem(name);

            if (dbDto == null) {
                return Result.failure(BaseErrorCode.PROBLEM_NOT_EXIST);
            } else if (dbDto.getShowsolution() == true) {
                return Result.success(dbDto);
            } else {
                dbDto.setSolution("题解已隐藏");
                return Result.success(dbDto);
            }
        } catch (Exception e) {
            return Result.failure(BaseErrorCode.SERVICE_ERROR);
        }
    }

    public ResultVO getProblemInRoot(String name) {

        try {

            FullInfoDTO dbDto = mybatisRepos.getProblemInRoot(name);

            if (dbDto == null) {
                return Result.failure(BaseErrorCode.PROBLEM_NOT_EXIST);
            } else {
                return Result.success(dbDto);
            }
        } catch (Exception e) {
            return Result.failure(BaseErrorCode.SERVICE_ERROR);
        }
    }
    public ResultVO insertProblem(FullInfoDTO dto) {
        dto.setCreatetime(new Date(System.currentTimeMillis()));

        try {
            Integer rows = mybatisRepos.insertProblem(dto);
            if (rows != null && rows > 0) {
                return Result.success();
            } else {
                return Result.failure(BaseErrorCode.SERVICE_ERROR);
            }
        } catch (Exception e) {
            return Result.failure(BaseErrorCode.SERVICE_ERROR);
        }
    }

    public ResultVO updateProblem(FullInfoDTO dto) {
        try {
            Integer rows = mybatisRepos.updateProblem(dto);

            if (rows != null && rows > 0) {
                return Result.success();
            } else {
                return Result.failure(BaseErrorCode.SERVICE_ERROR);
            }
        } catch (Exception e) {
            return Result.failure(BaseErrorCode.SERVICE_ERROR);
        }
    }

    public ResultVO deleteProblem(String name) {
        return Result.failure(BaseErrorCode.SERVICE_ERROR);
    }

    public ResultVO getCase(CaseInputDTO caseInputDTO) {
        List<String> inputs = new ArrayList<>();
        inputs.add(caseInputDTO.getCode());
        inputs.add(caseInputDTO.getInput());
        return runClient.run(inputs, "c");
    }

    @Transactional
    public ResultVO submitCase(CasePairDTO casePairDTO) {
        String name = casePairDTO.getName();
        String input = casePairDTO.getInput();
        String output = casePairDTO.getOutput();
        try {
            // 检查输入输出是否存在
            if (input == null || output == null) {
                return Result.failure(BaseErrorCode.CLIENT_ERROR);
            }
            Integer result = mybatisRepos.incrementCaseAmount(name);
            if (result == null || result <= 0) {
                return Result.failure(BaseErrorCode.SERVICE_ERROR);
            }
            ProblemStatusDTO problemStatus = mybatisRepos.getStatus(name);
            if (problemStatus == null) {
                return Result.failure(BaseErrorCode.SERVICE_ERROR);
            }
            String bucketName = "letucoj";
            String inputObjectName = "problems" + "/" + name + "/input/" + problemStatus.getCaseAmount() + 1 + ".txt";
            String outputObjectName = "problems" + "/" + name + "/output/" + problemStatus.getCaseAmount() + 1 + ".txt";
            minioRepos.addFile(bucketName, inputObjectName, input.getBytes());
            minioRepos.addFile(bucketName, outputObjectName, output.getBytes());
            return Result.success();
        } catch (Exception e) {
            return Result.failure(BaseErrorCode.SERVICE_ERROR);
        }
    }

    @Override
    public ResultVO recordListByName(String pname, int start, int limit) {
        try {
            List<RecordDTO> records = mybatisRepos.getRecordsByName(pname, start, limit);
            Integer amount = mybatisRepos.getRecordsByNameCount(pname);
            if (records == null || records.isEmpty()) {
                return Result.failure(PracticeErrorCode.NO_RECORD_FOUND);
            } else if (amount == null || amount < 0) {
                return Result.failure(BaseErrorCode.SERVICE_ERROR);
            }
            return Result.success(Map.of(
                    "records",   records,
                    "amount", amount
            ));
        } catch (Exception e) {
            return Result.failure(BaseErrorCode.SERVICE_ERROR);
        }
    }

    @Override
    public ResultVO recordListAll(int start, int limit) {
        try {
            List<RecordDTO> records = mybatisRepos.getAllRecords(start, limit);
            Integer amount = mybatisRepos.getAllRecordsCount();
            if (records == null || records.isEmpty()) {
                return Result.failure(PracticeErrorCode.NO_RECORD_FOUND);
            } else if (amount == null || amount < 0) {
                return Result.failure(BaseErrorCode.SERVICE_ERROR);
            }
            return Result.success(
                    Map.of(
                    "records",   records,
                    "amount", amount
                    ));
        } catch (Exception e) {
            return Result.failure(BaseErrorCode.SERVICE_ERROR);
        }
    }
}
