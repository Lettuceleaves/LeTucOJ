package com.LetucOJ.practice.repos;

import com.LetucOJ.practice.model.SqlDataDTO;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

@Repository
@Mapper
public interface MybatisRepos {

    @Select("SELECT caseAmount FROM problem WHERE id = #{problemId}")
    Integer getProblemCaseNum(String problemId);

    @Select("Select* from problem WHERE id = #{problemId}")
    SqlDataDTO getProblemCase(String problemId);

    @Insert("INSERT INTO problem (id, name, caseAmount) VALUES (#{id}, #{caseAmount}, #{description})")
    Integer insertProblem(SqlDataDTO sqlDataDTO);

    @Update("UPDATE problem SET name = #{name}, name = #{name}, caseAmount = #{caseAmount} WHERE id = #{id}")
    Integer updateProblem(SqlDataDTO sqlDataDTO);

    @Delete("DELETE FROM problem WHERE id = #{problemId}")
    Integer deleteProblem(String problemId);
}