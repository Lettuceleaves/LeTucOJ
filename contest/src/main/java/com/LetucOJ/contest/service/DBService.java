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
    ResultVO<Problem> getProblem(String problemName, String contestName, String userName, String role);
    ResultVO<Void> deleteProblem(ContestProblemDTO contestProblemDTO);
    ResultVO<BoardVO> getBoard(String contestName, String role);
    ResultVO<Contest> getContest(String contestName, String role);
    ResultVO<Void> insertContest(Contest contest);
    ResultVO<Void> updateContest(Contest contest);
    ResultVO<Void> insertProblem(ContestProblemDTO contestProblemDTO);
    ResultVO<Void> attend(String userName, String userNickName, String contestName, String password);
    ResultVO<Void> attended(String userName, String contestName);
}
