package com.LetucOJ.contest.repos;

import com.LetucOJ.contest.model.*;
import com.LetucOJ.contest.model.DTO.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface MybatisRepos {

    ProblemStatusDTO getStatus(String name);
    Problem getProblem(String name);
    List<ContestBrief> getContestList();
    Contest getContest(String name);
    Integer insertContest(Contest contest);
    Integer updateContest(Contest contest);
    List<ProblemBrief> getProblemList(@Param("contestName") String contestName);
    Integer insertProblem(ContestProblemDTO contestProblemDTO);
    Integer deleteProblem(@Param("contestName") String contestName,
                          @Param("problemName") String problemName);
    Integer getScoreByContestAndProblem(@Param("contestName") String contestName,
                                        @Param("problemName") String problemName);
    Integer insertContestUser(@Param("contestName") String contestName,
                              @Param("userName")    String userName,
                              @Param("cnname")      String cnname);
    Integer getUserStatus(@Param("contestName") String contestName,
                          @Param("userName")    String userName);
    BoardDTO getContestBoardByUserAndProblem(@Param("contestName") String contestName,
                                             @Param("userName")    String userName,
                                             @Param("problemName") String problemName);
    List<BoardDTO> getBoard(@Param("contestName") String contestName);
    Integer insertContestBoard(BoardDTO boardDTO);
    Integer updateContestBoard(BoardDTO boardDTO);
    Integer insertRecord(SubmitRecordDTO recordDTO);
    Integer problemExist(@Param("problemName") String problemName);
}