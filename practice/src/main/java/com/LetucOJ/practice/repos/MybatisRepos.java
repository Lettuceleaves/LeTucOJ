package com.LetucOJ.practice.repos;

import com.LetucOJ.practice.model.*;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface MybatisRepos {

    @Select("SELECT COUNT(*) FROM problem")
    Integer getAmount();

    @Select("SELECT ispublic, showsolution, caseAmount FROM problem WHERE name = #{name}")
    ProblemStatusDTO getStatus(String name);

    @Update("UPDATE problem SET caseAmount = caseAmount + 1 WHERE name = #{name}")
    Integer incrementCaseAmount(String name);

    @Select("SELECT name, cnname FROM problem WHERE ispublic = 1 ORDER BY #{order} LIMIT #{start}, #{limit}")
    List<ListDTO> getList(ListServiceDTO listServiceDTO);

    @Select("SELECT name, cnname FROM problem WHERE ispublic = 1 AND (cnname LIKE CONCAT('%', #{like}, '%') OR tags LIKE CONCAT('%', #{like}, '%') OR content LIKE CONCAT('%', #{like}, '%')) ORDER BY #{order} LIMIT #{start}, #{limit}")
    List<ListDTO> searchList(ListServiceDTO listServiceDTO);

    @Select("SELECT name, cnname FROM problem ORDER BY #{order} LIMIT #{start}, #{limit}")
    List<ListDTO> getListInRoot(ListServiceDTO listServiceDTO);

    @Select("SELECT name, cnname FROM problem WHERE cnname LIKE CONCAT('%', #{like}, '%') OR tags LIKE CONCAT('%', #{like}, '%') OR content LIKE CONCAT('%', #{like}, '%') ORDER BY #{order} LIMIT #{start}, #{limit}")
    List<ListDTO> searchListInRoot(ListServiceDTO listServiceDTO);

    @Select("SELECT * FROM problem WHERE name = #{name} and ispublic = 1")
    FullInfoDTO getProblem(String name);

    @Select("SELECT * FROM problem WHERE name = #{name}")
    FullInfoDTO getProblemInRoot(String name);

    @Insert("INSERT INTO problem (name, cnname, caseAmount, difficulty, tags, authors, createtime, updateat, content, freq, ispublic, solution, showsolution) VALUES (#{name}, #{cnname}, #{caseAmount}, #{difficulty}, #{tags}, #{authors}, #{createtime}, #{updateat}, #{content}, #{freq}, #{ispublic}, #{solution}, #{showsolution})")
    Integer insertProblem(FullInfoDTO fullInfoDTO);

    @Update("UPDATE problem SET cnname = #{cnname}, caseAmount = #{caseAmount}, difficulty = #{difficulty}, tags = #{tags}, authors = #{authors}, createtime = #{createtime}, updateat = #{updateat}, content = #{content}, freq = #{freq}, ispublic = #{ispublic}, solution = #{solution}, showsolution = #{showsolution} WHERE name = #{name}")
    Integer updateProblem(FullInfoDTO fullInfoDTO);

    @Delete("DELETE FROM problem WHERE name = #{name}")
    Integer deleteProblem(String name);

    @Select("SELECT * FROM record")
    List<RecordDTO> getAllRecords();

    @Select("SELECT * FROM record WHERE userName = #{userName}")
    List<RecordDTO> getRecordsByName(String userName);

    @Insert("INSERT INTO record (userName, cnname, problemName, language, code, result, timeUsed, memoryUsed, submitTime) VALUES (#{userName}, #{cnname}, #{problemName}, #{language}, #{code}, #{result}, #{timeUsed}, #{memoryUsed}, #{submitTime})")
    Integer insertRecord(RecordDTO recordDTO);
}