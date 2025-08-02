package com.LetucOJ.contest.repos;

import com.LetucOJ.contest.model.db.*;
import com.LetucOJ.contest.model.net.ContestProblemDTO;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface MybatisRepos {

    @Select("SELECT ispublic, showsolution, caseAmount FROM problem WHERE name = #{name}")
    ProblemStatusDTO getStatus(String name);

    @Select("SELECT * FROM contest_problems WHERE contest_name = #{contestName}")
    List<ContestProblemDTO> getProblemList(String contestName);

    @Select("SELECT * FROM contest")
    List<ContestInfoDTO> getContestList();

    @Select("SELECT * FROM problem WHERE name = #{name}")
    FullInfoDTO getProblem(String name);

    @Select("SELECT * FROM contest WHERE name = #{name}")
    ContestInfoDTO getContest(String name);

    @Insert("INSERT INTO contest (name, cnname, mode, start, end, ispublic) VALUES (#{name}, #{mode}, #{cnname}, #{start}, #{end}, #{ispublic})")
    Integer insertContest(ContestInfoDTO contestInfoDTO);

    @Update("UPDATE contest SET cnname = #{cnname}, mode = #{mode}, start = #{start}, end = #{end}, ispublic = #{ispublic} WHERE name = #{name}")
    Integer updateContest(ContestInfoDTO contestInfoDTO);

    @Delete("DELETE FROM contest_problems WHERE contest_name = #{contestName} AND problem_name = #{problemName}")
    Integer deleteProblem(ContestProblemDTO contestProblemDTO);

    @Insert("INSERT INTO contest_problems (contest_name, problem_name, score) VALUES (#{contestName}, #{problemName}, #{score})")
    Integer insertProblem(ContestProblemDTO contestProblemDTO);

    @Select("SELECT score FROM contest_problems WHERE contest_name = #{contestName} AND problem_name = #{problemName}")
    Integer getScoreByContestAndProblem(@Param("contestName") String contestName, @Param("problemName") String problemName);

    @Select("SELECT * FROM contest_board WHERE contest_name = #{contestName} AND user_name = #{userName} AND problem_name = #{problemName}")
    BoardDTO getContestBoardByUserAndProblem(@Param("contestName") String contestName, @Param("userName") String userName, @Param("problemName") String problemName);

    @Select("SELECT * FROM contest_board WHERE contest_name = #{contestName}")
    List<BoardDTO> getBoard(String contestName);

    @Insert("INSERT INTO contest_board (contest_name, user_name, problem_name, score, times) VALUES (#{contestName}, #{userName}, #{problemName}, #{score}, #{times})")
    Integer insertContestBoard(BoardDTO boardDTO);

    @Update("UPDATE contest_board SET score = #{score}, times = #{times} WHERE contest_name = #{contestName} AND user_name = #{userName} AND problem_name = #{problemName}")
    Integer updateContestBoard(BoardDTO boardDTO);

    @Insert("INSERT INTO record (name, userName, result, time, memory, language, code, createtime) VALUES (#{name}, #{userName}, #{result}, #{time}, #{memory}, #{language}, #{code}, #{createtime})")
    Integer insertRecord(RecordDTO recordDTO);

}