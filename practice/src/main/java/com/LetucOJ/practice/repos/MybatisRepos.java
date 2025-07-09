package com.LetucOJ.practice.repos;

import com.LetucOJ.practice.model.BasicInfoDTO;
import com.LetucOJ.practice.model.FullInfoDTO;
import com.LetucOJ.practice.model.FullInfoVO;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

@Repository
@Mapper
public interface MybatisRepos {

    @Select("SELECT caseAmount FROM problem WHERE id = #{problemId}")
    Integer getProblemCaseNum(String problemId);

    @Select("Select id, name, caseAmount from problem WHERE id = #{problemId}")
    BasicInfoDTO getBasicInfoSingleCase(String problemId);

    @Select("SELECT * FROM problem WHERE id = #{problemId}")
    FullInfoDTO getFullInfoSingleCase(String problemId);

    @Insert("INSERT INTO problem (id, name, caseAmount, difficulty, tags, authors, createtime, updateat, content, freq, ispublic, solution, showsolution) VALUES (#{id}, #{name}, #{caseAmount}, #{difficulty}, #{tags}, #{authors}, #{createtime}, #{updateat}, #{content}, #{freq}, #{ispublic}, #{solution}, #{showsolution})")
    Integer insertProblem(FullInfoDTO fullInfoDTO);

    @Update("UPDATE problem SET name = #{name}, caseAmount = #{caseAmount}, difficulty = #{difficulty}, tags = #{tags}, authors = #{authors}, createtime = #{createtime}, updateat = #{updateat}, content = #{content}, freq = #{freq}, ispublic = #{ispublic}, solution = #{solution}, showsolution = #{showsolution} WHERE id = #{id}")
    Integer updateProblem(FullInfoDTO fullInfoDTO);

    @Delete("DELETE FROM problem WHERE id = #{problemId}")
    Integer deleteProblem(String problemId);
}