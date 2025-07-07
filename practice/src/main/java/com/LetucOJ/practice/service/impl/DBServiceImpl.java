package com.LetucOJ.practice.service.impl;

import com.LetucOJ.practice.model.SqlDataDTO;
import com.LetucOJ.practice.model.SqlServiceDTO;
import com.LetucOJ.practice.model.SqlVO;
import com.LetucOJ.practice.repos.MybatisRepos;
import com.LetucOJ.practice.service.DBService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DBServiceImpl implements DBService { // TODO

    @Autowired
    private MybatisRepos mybatisRepos;

    private SqlVO getProblemCase(SqlServiceDTO dto) {
        try {
            SqlDataDTO mybatisResult = mybatisRepos.getProblemCase(dto.getId());
            if (mybatisResult == null) {
                return new SqlVO((byte) 0, null, "Problem not found in Mybatis");
            } else {
                return new SqlVO((byte) 1, mybatisResult, null);
            }
        } catch (Exception e) {
            return new SqlVO((byte) 1, null, "Mybatis Error: " + e.getMessage());
        }
    }

    private SqlVO insertProblem(SqlServiceDTO dto) {
        try {
            if (dto.getData() == null) {
                return new SqlVO();
            }
            Integer mybatisResult = mybatisRepos.insertProblem(dto.getData());
            if (mybatisResult == null || mybatisResult != 1) {
                return new SqlVO((byte) 0, null, "Mybatis return not 1 or is null");
            } else {
                return new SqlVO((byte) 2, null, null);
            }
        } catch (Exception e) {
            return new SqlVO((byte) 0, null, "Mybatis Error: " + e.getMessage());
        }
    }

    private SqlVO updateProblem(SqlServiceDTO dto) {
        try {
            if (dto.getData() == null) {
                return new SqlVO();
            }
            Integer mybatisResult = mybatisRepos.updateProblem(dto.getData());
            if (mybatisResult == null || mybatisResult < 0) {
                return new SqlVO((byte) 0, null, "Mybatis return not 1 or is null");
            } else {
                return new SqlVO((byte) 3, null, null);
            }
        } catch (Exception e) {
            return new SqlVO((byte) 0, null, "Mybatis Error: " + e.getMessage());
        }
    }

    private SqlVO deleteProblem(SqlServiceDTO dto) {
        try {
            Integer mybatisResult = mybatisRepos.deleteProblem(dto.getId());
            if (mybatisResult == null || mybatisResult < 0) {
                return new SqlVO((byte) 0, null, "Mybatis return not 1 or is null");
            } else {
                return new SqlVO((byte) 4, null, null);
            }
        } catch (Exception e) {
            return new SqlVO((byte) 0, null, "Mybatis Error: " + e.getMessage());
        }
    }

    @Override
    public SqlVO DBServiceSelector(SqlServiceDTO dto) {
        SqlVO sqlResult = null;
        switch (dto.getType()) {
            case "SELECT" -> {
                if (dto.getSubType().equals("SINGLE_LINE")) {
                    return getProblemCase(dto);
                } else {
                    return new SqlVO((byte) 0, null, "Unsupported command");
                }
            }
            case "INSERT" -> {
                if (dto.getSubType().equals("SINGLE_LINE")) {
                    return insertProblem(dto);
                } else {
                    return new SqlVO((byte) 0, null, "Unsupported command");
                }
            }
            case "DELETE" -> {
                if (dto.getSubType().equals("SINGLE_LINE")) {
                    return deleteProblem(dto);
                } else {
                    return new SqlVO((byte) 0, null, "Unsupported command");
                }
            }
            case "UPDATE" -> {
                if (dto.getSubType().equals("SINGLE_LINE")) {
                    return updateProblem(dto);
                } else {
                    return new SqlVO((byte) 0, null, "Unsupported command");
                }
            }
            default -> {
                return new SqlVO((byte) 0, null, "Unsupported command");
            }
        }
    }
}
