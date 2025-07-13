package com.LetucOJ.practice.service.impl;

import com.LetucOJ.practice.model.*;
import com.LetucOJ.practice.repos.MybatisRepos;
import com.LetucOJ.practice.service.DBService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DBServiceImpl implements DBService { // TODO

    @Autowired
    private MybatisRepos mybatisRepos;

    private BasicInfoVO getBasicInfoSingleCase(BasicInfoServiceDTO dto) {
        try {
            BasicInfoDTO mybatisResult = mybatisRepos.getBasicInfoSingleCase(dto.getName());
            if (mybatisResult == null) {
                return new BasicInfoVO((byte) 0, null, "Problem not found in Mybatis");
            } else {
                return new BasicInfoVO((byte) 1, List.of(new BasicInfoDTO[]{mybatisResult}), null);
            }
        } catch (Exception e) {
            return new BasicInfoVO((byte) 1, null, "Mybatis Error: " + e.getMessage());
        }
    }

    private FullInfoVO getFullInfoSingleCase(FullInfoServiceDTO dto) {
        try {
            FullInfoDTO mybatisResult = mybatisRepos.getFullInfoSingleCase(dto.getName());
            if (mybatisResult == null) {
                return new FullInfoVO((byte) 0, null, "Problem not found in Mybatis");
            } else {
                return new FullInfoVO((byte) 1, mybatisResult, null);
            }
        } catch (Exception e) {
            return new FullInfoVO((byte) 0, null, "Mybatis Error: " + e.getMessage());
        }
    }

    private FullInfoVO insertProblem(FullInfoServiceDTO dto) {
        try {
            if (dto.getData() == null) {
                return new FullInfoVO();
            }
            Integer mybatisResult = mybatisRepos.insertProblem(dto.getData());
            if (mybatisResult == null || mybatisResult != 1) {
                return new FullInfoVO((byte) 0, null, "Mybatis return not 1 or is null");
            } else {
                return new FullInfoVO((byte) 2, null, null);
            }
        } catch (Exception e) {
            return new FullInfoVO((byte) 0, null, "Mybatis Error: " + e.getMessage());
        }
    }

    private FullInfoVO updateProblem(FullInfoServiceDTO dto) {
        try {
            if (dto.getData() == null) {
                return new FullInfoVO();
            }
            Integer mybatisResult = mybatisRepos.updateProblem(dto.getData());
            if (mybatisResult == null || mybatisResult < 0) {
                return new FullInfoVO((byte) 0, null, "Mybatis return not 1 or is null");
            } else {
                return new FullInfoVO((byte) 3, null, null);
            }
        } catch (Exception e) {
            return new FullInfoVO((byte) 0, null, "Mybatis Error: " + e.getMessage());
        }
    }

    private BasicInfoVO deleteProblem(BasicInfoServiceDTO dto) {
        try {
            Integer mybatisResult = mybatisRepos.deleteProblem(dto.getName());
            if (mybatisResult == null || mybatisResult < 0) {
                return new BasicInfoVO((byte) 0, null, "Mybatis return not 1 or is null");
            } else {
                return new BasicInfoVO((byte) 4, null, null);
            }
        } catch (Exception e) {
            return new BasicInfoVO((byte) 0, null, "Mybatis Error: " + e.getMessage());
        }
    }

    private BasicInfoVO getBasicInfoList(BasicInfoServiceDTO dto) {
        try {
            if (dto.getStart() == null || dto.getLimit() == null) {
                return new BasicInfoVO((byte) 0, null, "Start or limit is null");
            }
            List<BasicInfoDTO> mybatisResult = mybatisRepos.getBasicInfoList(dto.getStart(), dto.getLimit());
            if (mybatisResult == null || mybatisResult.isEmpty()) {
                return new BasicInfoVO((byte) 0, null, "No problems found in Mybatis");
            } else {
                return new BasicInfoVO((byte) 1, mybatisResult, null);
            }
        } catch (Exception e) {
            return new BasicInfoVO((byte) 0, null, "Mybatis Error: " + e.getMessage());
        }
    }

    @Override
    public BasicInfoVO BasicDBServiceSelector(BasicInfoServiceDTO dto) {
        BasicInfoVO sqlResult = null;
        switch (dto.getType()) {
            case "SELECT" -> {
                if (dto.getSubType().equals("SINGLE_LINE")) {
                    return getBasicInfoSingleCase(dto);
                } else if (dto.getSubType().equals("LIST")) {
                    return getBasicInfoList(dto);
                } else {
                    return new BasicInfoVO((byte) 0, null, "Unsupported command");
                }
            }
//            case "INSERT" -> {
//                if (dto.getSubType().equals("SINGLE_LINE")) {
//                    return insertProblem(dto);
//                } else {
//                    return new BasicInfoVO((byte) 0, null, "Unsupported command");
//                }
//            }
            case "DELETE" -> {
                if (dto.getSubType().equals("SINGLE_LINE")) {
                    return deleteProblem(dto);
                } else {
                    return new BasicInfoVO((byte) 0, null, "Unsupported command");
                }
            }
//            case "UPDATE" -> {
//                if (dto.getSubType().equals("SINGLE_LINE")) {
//                    return updateProblem(dto);
//                } else {
//                    return new BasicInfoVO((byte) 0, null, "Unsupported command");
//                }
//            }
            default -> {
                return new BasicInfoVO((byte) 0, null, "Unsupported command");
            }
        }
    }

    @Override
    public FullInfoVO FullDBServiceSelector(FullInfoServiceDTO dto) {
        FullInfoVO sqlResult = null;
        switch (dto.getType()) {
            case "SELECT" -> {
                if (dto.getSubType().equals("SINGLE_LINE")) {
                    return getFullInfoSingleCase(dto);
                } else {
                    return new FullInfoVO((byte) 0, null, "Unsupported command");
                }
            }
            case "INSERT" -> {
                if (dto.getSubType().equals("SINGLE_LINE")) {
                    return insertProblem(dto);
                } else {
                    return new FullInfoVO((byte) 0, null, "Unsupported command");
                }
            }
            case "UPDATE" -> {
                if (dto.getSubType().equals("SINGLE_LINE")) {
                    return updateProblem(dto);
                } else {
                    return new FullInfoVO((byte) 0, null, "Unsupported command");
                }
            }
            default -> {
                return new FullInfoVO((byte) 0, null, "Unsupported command");
            }
        }
    }
}
