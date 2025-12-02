package com.LetucOJ.practice.repos;

import com.LetucOJ.common.anno.LanguageConfigDO;
import com.LetucOJ.practice.model.DTO.*;
import com.LetucOJ.practice.model.Problem;
import com.LetucOJ.practice.model.ProblemBrief;
import com.LetucOJ.practice.model.ProblemStatus;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
@Mapper
public interface MybatisRepos extends BaseMapper<LanguageConfigDO> {

    @Select("SELECT public > 0 AS publicProblem, showsolution > 0 AS showSolution, caseAmount FROM problem WHERE name = #{name}")
    ProblemStatus getStatus(String name);

    @Update("UPDATE problem SET caseAmount = caseAmount + 1 WHERE name = #{name}")
    Integer incrementCaseAmount(String name);

    @Select("SELECT name, cnname, tags, difficulty, 0 AS accepted  FROM problem WHERE public = 1 ORDER BY ${order} LIMIT #{start}, #{limit}")
    List<ProblemBrief> getList(ListConditionDTO listConditionDTO);

    @Select("SELECT COUNT(*) FROM problem WHERE public = 1")
    Integer getAmount(ListConditionDTO listConditionDTO);

    @Select("SELECT name, cnname, tags, difficulty, 0 AS accepted FROM problem WHERE public = 1 AND (cnname LIKE CONCAT('%', #{like}, '%') OR tags LIKE CONCAT('%', #{like}, '%') OR content LIKE CONCAT('%', #{like}, '%')) ORDER BY ${order} LIMIT #{start}, #{limit}")
    List<ProblemBrief> searchList(ListConditionDTO listConditionDTO);

    @Select("SELECT COUNT(*) FROM problem WHERE public = 1 AND (cnname LIKE CONCAT('%', #{like}, '%') OR tags LIKE CONCAT('%', #{like}, '%') OR content LIKE CONCAT('%', #{like}, '%'))")
    Integer getSearchAmount(ListConditionDTO listConditionDTO);

    @Select("SELECT name, cnname, tags, difficulty, 0 AS accepted FROM problem ORDER BY ${order} LIMIT #{start}, #{limit}")
    List<ProblemBrief> getListInRoot(ListConditionDTO listConditionDTO);

    @Select("SELECT COUNT(*) FROM problem")
    Integer getAmountInRoot(ListConditionDTO listConditionDTO);

    @Select("SELECT name, cnname, tags, difficulty, 0 AS accepted FROM problem WHERE cnname LIKE CONCAT('%', #{like}, '%') OR tags LIKE CONCAT('%', #{like}, '%') OR content LIKE CONCAT('%', #{like}, '%') ORDER BY ${order} LIMIT #{start}, #{limit}")
    List<ProblemBrief> searchListInRoot(ListConditionDTO listConditionDTO);

    @Select("SELECT COUNT(*) FROM problem WHERE cnname LIKE CONCAT('%', #{like}, '%') OR tags LIKE CONCAT('%', #{like}, '%') OR content LIKE CONCAT('%', #{like}, '%')")
    Integer getSearchAmountInRoot(ListConditionDTO listConditionDTO);

    @Select("SELECT name, cnname, caseAmount, difficulty, tags, authors, createtime, updateat, content, freq, public > 0 AS publicProblem, solution, showsolution " +
            "FROM problem " +
            "WHERE name = #{name} AND public = 1")
    Problem getProblem(String name);

    @Select("SELECT name, cnname, caseAmount, difficulty, tags, authors, createtime, updateat, content, freq, public > 0 AS publicProblem, solution, showsolution " +
            "FROM problem " +
            "WHERE name = #{name}")
    Problem getProblemInRoot(String name);

    @Insert("INSERT INTO problem (name, cnname, caseAmount, difficulty, tags, authors, createtime, updateat, content, freq, public, solution, showsolution) VALUES (#{name}, #{cnname}, #{caseAmount}, #{difficulty}, #{tags}, #{authors}, #{createtime}, #{updateat}, #{content}, #{freq}, #{publicProblem}, #{solution}, #{showsolution})")
    Integer insertProblem(Problem problem);

    @Update("UPDATE problem SET cnname = #{cnname}, caseAmount = #{caseAmount}, difficulty = #{difficulty}, tags = #{tags}, authors = #{authors}, updateat = #{updateat}, content = #{content}, freq = #{freq}, public = #{publicProblem}, solution = #{solution}, showsolution = #{showsolution} WHERE name = #{name}")
    Integer updateProblem(Problem problem);

    @Delete("DELETE FROM problem WHERE name = #{name}")
    Integer deleteProblem(String name);

    @Select("SELECT * FROM record ORDER BY submitTime DESC LIMIT #{start}, #{limit}")
    List<SubmitRecord> getAllRecords(int start, int limit);

    @Select("SELECT COUNT(*) FROM record")
    Integer getAllRecordsCount();

    @Select("SELECT * FROM record WHERE userName = #{userName} ORDER BY submitTime DESC LIMIT #{start}, #{limit}")
    List<SubmitRecord> getRecordsByName(String userName, int start, int limit);

    @Select("SELECT COUNT(*) FROM record WHERE userName = #{userName}")
    Integer getRecordsByNameCount(String userName);

    @Insert("INSERT INTO record (userName, cnname, problemName, language, code, result, timeUsed, memoryUsed, submitTime) VALUES (#{userName}, #{cnname}, #{problemName}, #{language}, #{code}, #{result}, #{timeUsed}, #{memoryUsed}, #{submitTime})")
    Integer insertRecord(SubmitRecord submitRecord);

    @Select("SELECT problem_name FROM correct WHERE user_name = #{userName}")
    Set<String> getCorrectByName(String userName);

    @Insert("INSERT INTO correct (user_name, problem_name) VALUES (#{userName}, #{problemName})")
    Integer insertCorrect(String userName, String problemName);

    @Select("SELECT COUNT(*) FROM correct WHERE user_name = #{userName} AND problem_name = #{problemName}")
    Integer checkCorrect(String userName, String problemName);

}