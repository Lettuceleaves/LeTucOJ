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
    @Select("SELECT public > 0 as ispublic, showsolution, caseAmount FROM problem WHERE name = #{name}")
    ProblemStatusDTO getStatus(String name);

    @Select("SELECT name, cnname, caseAmount, difficulty, tags, authors, createtime, updateat, content, freq, public > 0 AS publicProblem, solution, showsolution " +
            "FROM problem " +
            "WHERE name = #{name}")
    FullInfoDTO getProblem(String name);

    // --- Contest 相关 ---
    @Select("SELECT * FROM contest")
    List<ContestInfoDTO> getContestList();

    @Select("SELECT name, cnname, mode, start, end, public > 0 AS publicContest, note FROM contest WHERE name = #{name}")
    ContestInfoDTO getContest(String name);

    @Insert("INSERT INTO contest " +
            "(name, cnname, mode, start, end, public, note) " +
            "VALUES " +
            "(#{name}, #{cnname}, #{mode}, #{start}, #{end}, #{publicContest}, #{note})")
    Integer insertContest(ContestInfoDTO contestInfoDTO);

    @Update("UPDATE contest SET " +
            "cnname   = #{cnname}, " +
            "mode     = #{mode}, " +
            "start    = #{start}, " +
            "end      = #{end}, " +
            "public   = #{publicContest}, " +
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

    @Select("SELECT COUNT(*) FROM contest_user WHERE contest_name = #{contestName} AND user_name = #{userName}")
    Integer getUserStatus(@Param("contestName") String contestName,
                                  @Param("userName")    String userName);


    // --- 排行榜（使用视图，动态组合所有用户×题目） ---
    @Select("SELECT contest_name AS contestName, " +
            "       user_name AS userName, " +
            "       problem_name AS problemName, " +
            "       score ," +
            "       attempts AS times," +
            "       last_submit as lastSubmit FROM contest_board " +
            "WHERE contest_name = #{contestName} " +
            "  AND user_name    = #{userName}    " +
            "  AND problem_name = #{problemName}")
    BoardDTO getContestBoardByUserAndProblem(@Param("contestName") String contestName,
                                             @Param("userName")    String userName,
                                             @Param("problemName") String problemName);

    @Select("SELECT u.user_name AS userName, u.cnname AS userCnname, p.problem_name AS problemName, " +
            "       COALESCE(b.score, 0) AS score, " +
            "       COALESCE(b.attempts, 0) AS times, " +
            "       b.last_submit AS lastSubmit " +
            "FROM contest_user u " +
            "JOIN contest_problem p ON p.contest_name = u.contest_name " +
            "LEFT JOIN contest_board b ON b.contest_name = u.contest_name " +
            "                          AND b.user_name    = u.user_name " +
            "                          AND b.problem_name = p.problem_name " +
            "WHERE u.contest_name = #{contestName} " +
            "ORDER BY u.user_name, p.problem_name")
    List<BoardDTO> getBoard(@Param("contestName") String contestName);

    @Insert("INSERT INTO contest_board " +
            "(contest_name, user_name, problem_name, score, attempts, last_submit) " +
            "VALUES " +
            "(#{contestName}, #{userName}, #{problemName}, #{score}, #{times}, #{lastSubmit})")
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

    @Select("SELECT COUNT(*) FROM problem WHERE name = #{name}")
    Integer problemExist(@Param("name") String name);
}
