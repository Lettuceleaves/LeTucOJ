package com.LetucOJ.contest.repos;

import com.LetucOJ.contest.model.db.*;
import com.LetucOJ.contest.model.net.ContestProblemDTO;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface MybatisRepos {

    // --- Problem 相关 ---
    @Select("SELECT ispublic, showsolution, caseAmount FROM problem WHERE name = #{name}")
    ProblemStatusDTO getStatus(String name);

    @Select("SELECT * FROM problem WHERE name = #{name}")
    FullInfoDTO getProblem(String name);

    // --- Contest 相关 ---
    @Select("SELECT * FROM contest")
    List<ContestInfoDTO> getContestList();

    @Select("SELECT * FROM contest WHERE name = #{name}")
    ContestInfoDTO getContest(String name);

    @Insert("INSERT INTO contest " +
            "(name, cnname, mode, start, end, ispublic, note) " +
            "VALUES " +
            "(#{name}, #{cnname}, #{mode}, #{start}, #{end}, #{ispublic}, #{note})")
    Integer insertContest(ContestInfoDTO contestInfoDTO);

    @Update("UPDATE contest SET " +
            "cnname   = #{cnname}, " +
            "mode     = #{mode}, " +
            "start    = #{start}, " +
            "end      = #{end}, " +
            "ispublic = #{ispublic}, " +
            "note     = #{note} " +
            "WHERE name = #{name}")
    Integer updateContest(ContestInfoDTO contestInfoDTO);

    // --- Contest–Problem 关联（题库中题目在某场比赛的权重） ---
    @Select("SELECT * FROM contest_problem WHERE contest_name = #{contestName}")
    List<ContestProblemDTO> getProblemList(@Param("contestName") String contestName);

    @Insert("INSERT INTO contest_problem " +
            "(contest_name, problem_name, score) " +
            "VALUES " +
            "(#{contestName}, #{problemName}, #{score})")
    Integer insertProblem(ContestProblemDTO contestProblemDTO);

    @Delete("DELETE FROM contest_problem " +
            "WHERE contest_name = #{contestName} " +
            "  AND problem_name = #{problemName}")
    Integer deleteProblem(@Param("contestName") String contestName,
                          @Param("problemName") String problemName);

    @Select("SELECT score FROM contest_problem " +
            "WHERE contest_name = #{contestName} " +
            "  AND problem_name = #{problemName}")
    Integer getScoreByContestAndProblem(@Param("contestName") String contestName,
                                        @Param("problemName") String problemName);

    // --- 添加参赛用户 ---
    @Insert("INSERT INTO contest_user (contest_name, user_name, cnname) " +
            "VALUES (#{contestName}, #{userName}, #{cnname})")
    Integer insertContestUser(@Param("contestName") String contestName,
                              @Param("userName")    String userName,
                              @Param("cnname")    String cnname);


    // --- 排行榜（使用视图，动态组合所有用户×题目） ---
    @Select("SELECT * FROM contest_board_view " +
            "WHERE contest_name = #{contestName} " +
            "  AND user_name    = #{userName}    " +
            "  AND problem_name = #{problemName}")
    BoardDTO getContestBoardByUserAndProblem(@Param("contestName") String contestName,
                                             @Param("userName")    String userName,
                                             @Param("problemName") String problemName);

    @Select("SELECT * FROM contest_board_view " +
            "WHERE contest_name = #{contestName}")
    List<BoardDTO> getBoard(@Param("contestName") String contestName);

    @Insert("INSERT INTO contest_board " +
            "(contest_name, user_name, problem_name, score, attempts) " +
            "VALUES " +
            "(#{contestName}, #{userName}, #{problemName}, #{score}, #{times})")
    Integer insertContestBoard(BoardDTO boardDTO);

    @Update("UPDATE contest_board SET " +
            "score    = #{score}, " +
            "attempts = #{times}, " +
            "last_submit = CURRENT_TIMESTAMP " +
            "WHERE contest_name = #{contestName} " +
            "  AND user_name    = #{userName}    " +
            "  AND problem_name = #{problemName}")
    Integer updateContestBoard(BoardDTO boardDTO);

    // --- 提交记录 ---
    @Insert("INSERT INTO record " +
            "(userName, cnname, problemName, language, code, result, timeUsed, memoryUsed, submitTime) " +
            "VALUES " +
            "(#{userName}, #{cnname}, #{problemName}, #{language}, #{code}, #{result}, #{timeUsed}, #{memoryUsed}, #{submitTime})")
    Integer insertRecord(RecordDTO recordDTO);
}
