package com.LetucOJ.practice.repos;

import com.LetucOJ.practice.model.BasicInfoDTO;
import com.LetucOJ.practice.model.FullInfoDTO;
import com.LetucOJ.practice.model.FullInfoVO;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface MybatisRepos {

    @Select("SELECT caseAmount FROM problem WHERE name = #{name}")
    Integer getProblemCaseNum(String name);

    @Select("Select name, cnname, caseAmount from problem WHERE name = #{name}")
    BasicInfoDTO getBasicInfoSingleCase(String name);

    @Select("SELECT name, cnname, caseAmount FROM problem LIMIT #{start}, #{limit}")
    List<BasicInfoDTO> getBasicInfoList(long start, long limit);

    @Select("SELECT * FROM problem WHERE name = #{name}")
    FullInfoDTO getFullInfoSingleCase(String name);

    @Insert("INSERT INTO problem (name, cnname, caseAmount, difficulty, tags, authors, createtime, updateat, content, freq, ispublic, solution, showsolution) VALUES (#{name}, #{cnname}, #{caseAmount}, #{difficulty}, #{tags}, #{authors}, #{createtime}, #{updateat}, #{content}, #{freq}, #{ispublic}, #{solution}, #{showsolution})")
    Integer insertProblem(FullInfoDTO fullInfoDTO);

    @Update("UPDATE problem SET cnname = #{cnname}, caseAmount = #{caseAmount}, difficulty = #{difficulty}, tags = #{tags}, authors = #{authors}, createtime = #{createtime}, updateat = #{updateat}, content = #{content}, freq = #{freq}, ispublic = #{ispublic}, solution = #{solution}, showsolution = #{showsolution} WHERE name = #{name}")
    Integer updateProblem(FullInfoDTO fullInfoDTO);

    @Delete("DELETE FROM problem WHERE name = #{name}")
    Integer deleteProblem(String name);
}