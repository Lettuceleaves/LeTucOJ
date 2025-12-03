package com.LetucOJ.contest.service;

import com.LetucOJ.common.result.ResultVO;
import com.LetucOJ.contest.model.Contest;
import com.LetucOJ.contest.model.DTO.ContestProblemDTO;
import com.LetucOJ.contest.model.Problem;
import com.LetucOJ.contest.model.VO.BoardVO;
import com.LetucOJ.contest.model.VO.ContestListVO;
import com.LetucOJ.contest.model.VO.ContestProblemListVO;

public interface DBService {
    ResultVO<ContestProblemListVO> getProblemList(String contestName, String role);
    ResultVO<ContestListVO> getContestList();
    ResultVO<Problem> getProblem(String name, String contestName, String userName, String role);
    ResultVO<Void> deleteProblem(ContestProblemDTO dto);
    ResultVO<BoardVO> getBoard(String contestName, String role);
    ResultVO<Contest> getContest(String ctname, String role);
    ResultVO<Void> insertContest(Contest dto);
    ResultVO<Void> updateContest(Contest dto);
    ResultVO<Void> insertProblem(ContestProblemDTO dto);
    ResultVO<Void> attend(String name, String cnname, String contestName);
    ResultVO<Void> attended(String name, String contestName);
}
