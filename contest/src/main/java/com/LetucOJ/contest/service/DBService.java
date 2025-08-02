package com.LetucOJ.contest.service;

import com.LetucOJ.contest.model.net.*;

public interface DBService {
    ResultVO getProblemList(String contestName);
    ResultVO getContestList();
    ResultVO getProblem(FullInfoServiceDTO dto, String contestName);
    ResultVO getBoard(String contestName);
    ResultVO getContest(ContestServiceDTO dto);
    ResultVO insertContest(ContestServiceDTO dto);
    ResultVO updateContest(ContestServiceDTO dto);
    ResultVO deleteContest(ContestServiceDTO dto);
    ResultVO insertProblem(ContestProblemDTO dto);
    ResultVO deleteProblem(ContestProblemDTO dto);
    ResultVO attend(String cnname, String contestName);
}
