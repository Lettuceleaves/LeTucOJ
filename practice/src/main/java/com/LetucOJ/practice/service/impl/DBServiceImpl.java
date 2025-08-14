package com.LetucOJ.practice.service.impl;

import com.LetucOJ.practice.client.RunClient;
import com.LetucOJ.practice.model.*;
import com.LetucOJ.practice.repos.MybatisRepos;
import com.LetucOJ.practice.repos.MinioRepos;
import com.LetucOJ.practice.service.DBService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@Service
public class DBServiceImpl implements DBService {

    @Autowired
    private MybatisRepos mybatisRepos;

    @Autowired
    private MinioRepos minioRepos;

    @Autowired
    private RunClient runClient;

    public ResultVO getAmount() {
        try {
            Integer amount = mybatisRepos.getAmount();

            if (amount == null || amount < 0) {
                return new ResultVO((byte) 5, null, "practice/getAmount: Amount is null or negative");
            }

            return new ResultVO((byte) 0, amount, null);
        } catch (Exception e) {
            return new ResultVO((byte) 5, null, "practice/getAmount: " + e.getMessage());
        }
    }

    public ResultVO getList(ListServiceDTO dto) {

        try {

            if (dto.getStart() == null || dto.getLimit() == null) {
                return new ResultVO(5, null, "practice/getList: Start or limit is null");
            }

            List<ListDTO> list = mybatisRepos.getList(dto);

            if (list == null || list.isEmpty()) {
                return new ResultVO((byte) 1, null, "practice/getList: No problems found in Mybatis");
            }

            return new ResultVO((byte) 0, list, null);
        } catch (Exception e) {
            return new ResultVO((byte) 5, null, "practice/getList: " + e.getMessage());
        }
    }

    public ResultVO getListInRoot(ListServiceDTO dto) {

        try {

            System.out.println("DTO: " + dto);

            if (dto.getStart() == null || dto.getLimit() == null) {
                return new ResultVO((byte) 5, null, "practice/getListInRoot: Start or limit is null");
            }

            List<ListDTO> list = mybatisRepos.getListInRoot(dto);

            if (list == null || list.isEmpty()) {
                return new ResultVO((byte) 1, null, "practice/getListInRoot: No problems found in Mybatis");
            }

            return new ResultVO((byte) 0, list, null);
        } catch (Exception e) {
            return new ResultVO((byte) 5, null, "practice/getListInRoot: " + e.getMessage());
        }
    }

    @Override
    public ResultVO searchList(ListServiceDTO dto) {
        try {
            if (dto.getStart() == null || dto.getLimit() == null) {
                return new ResultVO((byte) 5, null, "practice/searchList: Start or limit is null");
            }

            List<ListDTO> list = mybatisRepos.searchList(dto);

            if (list == null || list.isEmpty()) {
                return new ResultVO((byte) 1, null, "practice/searchList: No problems found in Mybatis");
            }

            return new ResultVO((byte) 0, list, null);
        } catch (Exception e) {
            return new ResultVO((byte) 5, null, "practice/searchList: " + e.getMessage());
        }
    }

    @Override
    public ResultVO searchListInRoot(ListServiceDTO dto) {
        try {
            if (dto.getStart() == null || dto.getLimit() == null) {
                return new ResultVO((byte) 5, null, "practice/searchListInRoot: Start or limit is null");
            }

            List<ListDTO> list = mybatisRepos.searchListInRoot(dto);

            if (list == null || list.isEmpty()) {
                return new ResultVO((byte) 1, null, "practice/searchListInRoot: No problems found in Mybatis");
            }

            return new ResultVO((byte) 0, list, null);
        } catch (Exception e) {
            return new ResultVO((byte) 5, null, "practice/searchListInRoot: " + e.getMessage());
        }
    }

    public ResultVO getProblem(String name) {

        try {

            FullInfoDTO dbDto = mybatisRepos.getProblem(name);

            if (dbDto == null) {
                return new ResultVO((byte) 5, null, "practice/getProblem: Problem not found in Mybatis");
            } else if (dbDto.getShowsolution() == true) {
                return new ResultVO((byte) 0, dbDto, null);
            } else {
                dbDto.setSolution("题解已隐藏");
                return new ResultVO((byte) 0, dbDto, null);
            }
        } catch (Exception e) {
            return new ResultVO((byte) 5, null, "practice/getProblem: " + e.getMessage());
        }
    }

    public ResultVO getProblemInRoot(String name) {

        try {

            FullInfoDTO dbDto = mybatisRepos.getProblemInRoot(name);

            if (dbDto == null) {
                return new ResultVO((byte) 5, null, "practice/getProblemInRoot: Problem not found in Mybatis");
            } else {
                return new ResultVO((byte) 0, dbDto, null);
            }
        } catch (Exception e) {
            return new ResultVO((byte) 5, null, "practice/getProblemInRoot: " + e.getMessage());
        }
    }
    public ResultVO insertProblem(FullInfoDTO dto) {
        dto.setCreatetime(new Date(System.currentTimeMillis()));

        try {
            Integer rows = mybatisRepos.insertProblem(dto);
            if (rows != null && rows > 0) {
                return new ResultVO(0, null, null);
            } else {
                return new ResultVO(5, null, "practice/insertProblem: Mybatis return not >=0 or is null");
            }
        } catch (Exception e) {
            return new ResultVO(5, null, "practice/insertProblem: " + e.getMessage());
        }
    }

    public ResultVO updateProblem(FullInfoDTO dto) {
        ResultVO response;
        try {
            Integer rows = mybatisRepos.updateProblem(dto);

            if (rows != null && rows > 0) {
                response = new ResultVO((byte)0, null, null);
            } else {
                response = new ResultVO((byte)5, null, "practice/updateProblem: Mybatis return not >=0 or is null");
            }
        } catch (Exception e) {
            return new ResultVO((byte)5, null, "practice/updateProblem: " + e.getMessage());
        }
        return response;
    }

    public ResultVO deleteProblem(String name) {
        return new ResultVO((byte)5, null, "practice/deleteProblem: Delete operation is not supported yet");
    }

    public ResultVO getCase(CaseInputDTO caseInputDTO) {
        List<String> inputs = new ArrayList<>();
        inputs.add(caseInputDTO.getCode());
        inputs.add(caseInputDTO.getInput());
        return runClient.run(inputs, "C");
    }

    @Transactional
    public ResultVO submitCase(CasePairDTO casePairDTO) {
        String name = casePairDTO.getName();
        String input = casePairDTO.getInput();
        String output = casePairDTO.getOutput();
        try {
            // 检查输入输出是否存在
            if (input == null || output == null) {
                return new ResultVO((byte) 5, null, "practice/submitCase: Input or output cannot be null");
            }
            Integer result = mybatisRepos.incrementCaseAmount(name);
            if (result == null || result <= 0) {
                return new ResultVO((byte) 5, null, "practice/submitCase: Error incrementing case amount");
            }
            ProblemStatusDTO problemStatus = mybatisRepos.getStatus(name);
            if (problemStatus == null) {
                return new ResultVO((byte) 5, null, "practice/submitCase: Problem status not found for " + name);
            }
            String inputFile = minioRepos.addFilePair(name, problemStatus.getCaseAmount(), input, output);
            if (inputFile != null) {
                return new ResultVO((byte) 5, null, "practice/submitCase: Error adding file pair" + inputFile);
            }
            return new ResultVO((byte) 0, null, null);
        } catch (Exception e) {
            return new ResultVO((byte) 5, null, "practice/submitCase: Error submitting test case: " + e.getMessage());
        }
    }

    @Override
    public ResultVO recordListByName(String pname) {
        try {
            List<RecordDTO> records = mybatisRepos.getRecordsByName(pname);
            if (records == null || records.isEmpty()) {
                return new ResultVO((byte)1, null, "practice/recordListByName: No records found for user " + pname);
            }
            return new ResultVO((byte)0, records, null);
        } catch (Exception e) {
            return new ResultVO((byte)5, null, "practice/recordListByName: Error retrieving records: " + e.getMessage());
        }
    }

    @Override
    public ResultVO recordListAll() {
        try {
            List<RecordDTO> records = mybatisRepos.getAllRecords();
            if (records == null || records.isEmpty()) {
                return new ResultVO((byte)1, null, "practice/recordListAll: No records found");
            }
            return new ResultVO((byte)0, records, null);
        } catch (Exception e) {
            return new ResultVO((byte)5, null, "practice/recordListAll: Error retrieving all records: " + e.getMessage());
        }
    }
}
