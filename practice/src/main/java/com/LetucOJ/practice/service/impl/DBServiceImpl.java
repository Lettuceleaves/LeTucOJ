package com.LetucOJ.practice.service.impl;

import com.LetucOJ.practice.client.RunClient;
import com.LetucOJ.practice.model.*;
import com.LetucOJ.practice.repos.MybatisRepos;
import com.LetucOJ.practice.repos.MinioRepos;
import com.LetucOJ.practice.service.DBService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        Integer amount = mybatisRepos.getAmount();

        if (amount == null || amount < 0) {
            return new ResultVO((byte)5, null, "practice/getAmount: Amount is null or negative");
        }

        return new ResultVO((byte)0, amount, null);
    }

    public ResultVO getList(ListServiceDTO dto) {

        if (dto.getStart() == null || dto.getLimit() == null) {
            return new ResultVO(5, null, "practice/getList: Start or limit is null");
        }

        List<ListDTO> list = mybatisRepos.getList(dto);

        if (list == null || list.isEmpty()) {
            return new ResultVO((byte)5, null, "practice/getList: No problems found in Mybatis");
        }

        return new ResultVO((byte)0, list, null);
    }

    public ResultVO getListInRoot(ListServiceDTO dto) {

        System.out.println("DTO: " + dto);

        if (dto.getStart() == null || dto.getLimit() == null) {
            return new ResultVO((byte)5, null, "practice/getListInRoot: Start or limit is null");
        }

        List<ListDTO> list = mybatisRepos.getListInRoot(dto);

        if (list == null || list.isEmpty()) {
            return new ResultVO((byte)5, null, "practice/getListInRoot: No problems found in Mybatis");
        }

        return new ResultVO((byte)0, list, null);
    }

    @Override
    public ResultVO searchList(ListServiceDTO dto) {
        if (dto.getStart() == null || dto.getLimit() == null) {
            return new ResultVO((byte)5, null, "practice/searchList: Start or limit is null");
        }

        List<ListDTO> list = mybatisRepos.searchList(dto);

        if (list == null || list.isEmpty()) {
            return new ResultVO((byte)5, null, "practice/searchList: No problems found in Mybatis");
        }

        return new ResultVO((byte)0, list, null);
    }

    @Override
    public ResultVO searchListInRoot(ListServiceDTO dto) {
        if (dto.getStart() == null || dto.getLimit() == null) {
            return new ResultVO((byte)5, null, "practice/searchListInRoot: Start or limit is null");
        }

        List<ListDTO> list = mybatisRepos.searchListInRoot(dto);

        if (list == null || list.isEmpty()) {
            return new ResultVO((byte)5, null, "practice/searchListInRoot: No problems found in Mybatis");
        }

        return new ResultVO((byte)0, list, null);
    }

    public ResultVO getProblem(FullInfoServiceDTO dto) {

        FullInfoDTO dbDto = mybatisRepos.getProblem(dto.getName());

        if (dbDto == null) {
            return new ResultVO((byte)5, null, "practice/getProblem: Problem not found in Mybatis");
        } else if (dbDto.getShowsolution() == true){
            return new ResultVO((byte)0, dbDto, null);
        } else {
            dbDto.setSolution("题解已隐藏");
            return new ResultVO((byte)0, dbDto, null);
        }
    }

    public ResultVO getProblemInRoot(FullInfoServiceDTO dto) {

        FullInfoDTO dbDto = mybatisRepos.getProblemInRoot(dto.getName());

        if (dbDto == null) {
            return new ResultVO((byte)5, null, "practice/getProblemInRoot: Problem not found in Mybatis");
        } else {
            return new ResultVO((byte)0, dbDto, null);
        }
    }
    public ResultVO insertProblem(FullInfoServiceDTO dto) {
        if (dto.getData() == null) {
            return new ResultVO();
        }
        dto.getData().setCreatetime(new Date(System.currentTimeMillis()));

        ResultVO response;

        try {
            Integer rows = mybatisRepos.insertProblem(dto.getData());
            if (rows != null && rows > 0) {
                response = new ResultVO(0, null, null);
            } else {
                response = new ResultVO(5, null, "practice/insertProblem: Mybatis return not >=0 or is null");
            }
        } catch (Exception e) {
            return new ResultVO(5, null, "practice/insertProblem: " + e.getMessage());
        }
        return response;
    }

    public ResultVO updateProblem(FullInfoServiceDTO dto) {
        if (dto.getData() == null) {
            return new ResultVO();
        }
        ResultVO response;
        try {
            Integer rows = mybatisRepos.updateProblem(dto.getData());

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

    public ResultVO deleteProblem(FullInfoServiceDTO dto) {
//        ResultVO response;
//        try {
//            Integer rows = mybatisRepos.deleteProblem(dto.getName());
//
//            if (rows != null && rows > 0) {
//                minioRepos.deleteFile(dto.getName());
//                response = new ResultVO((byte)0, null, null);
//            } else {
//                response = new ResultVO((byte)5, null, "practice/deleteProblem: Mybatis return not >=0 or is null");
//            }
//        } catch (Exception e) {
//            return new ResultVO((byte)0, null, e.getMessage());
//        }
//        return response;
        return new ResultVO((byte)5, null, "practice/deleteProblem: Delete operation is not supported yet");
    }

    public ResultVO getCase(CaseInputDTO caseInputDTO) {
        List<String> inputs = new ArrayList<>();
        inputs.add(caseInputDTO.getCode());
        inputs.add(caseInputDTO.getInput());
        return runClient.run(inputs);
    }

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
            String inputFile = minioRepos.addFilePair(name, result, input, output);
            if (inputFile != null) {
                return new ResultVO((byte) 5, null, "practice/submitCase: Error adding file pair" + inputFile);
            }
            return new ResultVO((byte) 0, null, null);
        } catch (Exception e) {
            return new ResultVO((byte) 5, null, "practice/submitCase: Error submitting test case: " + e.getMessage());
        }
    }

    @Override
    public ResultVO recordListByName(String cnname) {
        try {
            List<RecordDTO> records = mybatisRepos.getRecordsByName(cnname);
            if (records == null || records.isEmpty()) {
                return new ResultVO((byte)5, null, "practice/recordListByName: No records found for user " + cnname);
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
                return new ResultVO((byte)5, null, "practice/recordListAll: No records found");
            }
            return new ResultVO((byte)0, records, null);
        } catch (Exception e) {
            return new ResultVO((byte)5, null, "practice/recordListAll: Error retrieving all records: " + e.getMessage());
        }
    }
}
