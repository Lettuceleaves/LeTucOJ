package com.LetucOJ.practice.service.impl;

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

    private SqlVO selectSingle(SqlServiceDTO dto) {
        try {
            Integer mybatisResult = mybatisRepos.getProblemCaseNum(dto.getId());
            if (mybatisResult == null || mybatisResult < 0) {
                return new SqlVO();
            } else {
                return new SqlVO();
            }
        } catch (Exception e) {
            return new SqlVO();
        }
    }

    @Override
    public SqlVO DBServiceSelector(SqlServiceDTO dto) {
        switch (dto.getType()) {
            case 0 -> {
                return selectSingle(dto);
            }
            case 1 -> {
                return selectSingle(dto);
            }
            case 2 -> {
                return selectSingle(dto);
            }
            case 3 -> {
                return selectSingle(dto);
            }
            default -> {
                return new SqlVO();
            }
        }
    }
}
