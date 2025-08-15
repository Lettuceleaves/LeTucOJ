package com.LetucOJ.practice.service;

import com.LetucOJ.practice.model.*;

public interface DBService {
    ResultVO getAmount();
    ResultVO getList(ListServiceDTO dto);
    ResultVO getListInRoot(ListServiceDTO dto);
    ResultVO searchList(ListServiceDTO dto);
    ResultVO searchListInRoot(ListServiceDTO dto);
    ResultVO getProblem(String name);
    ResultVO getProblemInRoot(String name);
    ResultVO insertProblem(FullInfoDTO dto);
    ResultVO updateProblem(FullInfoDTO dto);
    ResultVO deleteProblem(String name);
    ResultVO getCase(CaseInputDTO dto);
    ResultVO submitCase(CasePairDTO dto);
    ResultVO recordListByName(String pname);
    ResultVO recordListAll();
}
