package com.LetucOJ.contest.service;

import com.LetucOJ.common.result.ResultVO;
import com.LetucOJ.contest.model.ContestProblemDTO;
import com.LetucOJ.contest.model.ContestInfoDTO;

public interface DBService {
    ResultVO getProblemList(String contestName);
    ResultVO getProblemListInRoot(String contestName);
    ResultVO getContestList();
    ResultVO getProblem(String name, String contestName);
    ResultVO getProblemInRoot(String name, String contestName);
    ResultVO deleteProblem(ContestProblemDTO dto);
    ResultVO getBoard(String contestName);
    ResultVO getBoardInRoot(String contestName);
    ResultVO getContest(String ctname);
    ResultVO getContestInRoot(String ctname);
    ResultVO insertContest(ContestInfoDTO dto);
    ResultVO updateContest(ContestInfoDTO dto);
    ResultVO insertProblem(ContestProblemDTO dto);
    ResultVO attend(String name, String cnname, String contestName);
    ResultVO getUserStatus(String name, String contestName);
}
