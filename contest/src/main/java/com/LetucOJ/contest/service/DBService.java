package com.LetucOJ.contest.service;

import com.LetucOJ.common.result.ResultVO;
import com.LetucOJ.contest.model.db.ContestInfoDTO;
import com.LetucOJ.contest.model.net.*;

public interface DBService {
    ResultVO getProblemList(String contestName);
    ResultVO getProblemListInRoot(String contestName);
    ResultVO getContestList();
    ResultVO getProblem(String name, String contestName);
    ResultVO getProblemInRoot(String name, String contestName);
    ResultVO getBoard(String contestName);
    ResultVO getBoardInRoot(String contestName);
    ResultVO getContest(String ctname);
    ResultVO getContestInRoot(String ctname);
    ResultVO insertContest(ContestInfoDTO dto);
    ResultVO updateContest(ContestInfoDTO dto);
    ResultVO deleteContest(String ctname);
    ResultVO insertProblem(ContestProblemDTO dto);
    ResultVO deleteProblem(ContestProblemDTO dto);
    ResultVO attend(String name, String cnname, String contestName);
    ResultVO getUserStatus(String name, String contestName);
}
