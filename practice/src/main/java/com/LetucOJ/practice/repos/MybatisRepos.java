package com.LetucOJ.practice.repos;

import com.LetucOJ.common.anno.LanguageConfigDO;
import com.LetucOJ.practice.model.DTO.*;
import com.LetucOJ.practice.model.Problem;
import com.LetucOJ.practice.model.ProblemBrief;
import com.LetucOJ.practice.model.ProblemStatus;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
@Mapper
public interface MybatisRepos extends BaseMapper<LanguageConfigDO> {

    // === 题目相关 ===

    /**
     * 通用查询列表（支持：前台/后台 + 搜索/不搜索）
     * 依靠 DTO 中的 onlyPublic 字段区分权限，like 字段区分搜索
     */
    List<ProblemBrief> selectProblemList(ListConditionDTO listConditionDTO);

    /**
     * 通用查询数量
     */
    Integer countProblemList(ListConditionDTO listConditionDTO);

    /**
     * 获取题目详情
     * @param name 题目ID/名称
     * @param onlyPublic true=只查公开(前台), false=查所有(后台)
     */
    Problem selectProblemDetail(@Param("name") String name, @Param("onlyPublic") Boolean onlyPublic);

    ProblemStatus getStatus(String name);

    Integer incrementCaseAmount(String name);

    Integer insertProblem(Problem problem);

    Integer updateProblem(Problem problem);

    void deleteProblem(String name); // 补充了 delete 方法定义

    // === 记录相关 ===

    /**
     * 通用记录查询
     * @param userName 如果为 null 则查所有用户
     */
    List<SubmitRecordDTO> selectRecordList(@Param("userName") String userName, @Param("start") int start, @Param("limit") int limit);

    Integer countRecordList(@Param("userName") String userName);

    Integer insertRecord(SubmitRecordDTO submitRecordDTO);

    // === 正确数相关 ===

    Set<String> getCorrectByName(String userName);

    Integer insertCorrect(@Param("userName") String userName, @Param("problemName") String problemName);
}