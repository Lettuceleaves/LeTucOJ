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

    ProblemStatus getStatus(String name);

    Integer incrementCaseAmount(String name);

    List<ProblemBrief> getList(ListConditionDTO listConditionDTO);

    Integer getAmount(ListConditionDTO listConditionDTO);

    List<ProblemBrief> searchList(ListConditionDTO listConditionDTO);

    Integer getSearchAmount(ListConditionDTO listConditionDTO);

    List<ProblemBrief> getListInRoot(ListConditionDTO listConditionDTO);

    Integer getAmountInRoot(ListConditionDTO listConditionDTO);

    List<ProblemBrief> searchListInRoot(ListConditionDTO listConditionDTO);

    Integer getSearchAmountInRoot(ListConditionDTO listConditionDTO);

    Problem getProblem(String name);

    Integer insertProblem(Problem problem);

    Integer updateProblem(Problem problem);

    // 多参数需要 @Param 才能在 XML 中通过名字引用，否则只能用 #{arg0}, #{arg1}
    List<SubmitRecordDTO> getAllRecords(@Param("start") int start, @Param("limit") int limit);

    Integer getAllRecordsCount();

    List<SubmitRecordDTO> getRecordsByName(@Param("userName") String userName, @Param("start") int start, @Param("limit") int limit);

    Integer getRecordsByNameCount(String userName);

    Integer insertRecord(SubmitRecordDTO SubmitRecordDTO);

    Set<String> getCorrectByName(String userName);

    Integer insertCorrect(@Param("userName") String userName, @Param("problemName") String problemName);
}