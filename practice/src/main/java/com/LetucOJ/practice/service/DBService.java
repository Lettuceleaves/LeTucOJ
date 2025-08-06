package com.LetucOJ.practice.service;

import com.LetucOJ.practice.model.*;

public interface DBService {
    ResultVO getAmount();
    ResultVO getList(ListServiceDTO dto);
    ResultVO getListInRoot(ListServiceDTO dto);
    ResultVO searchList(ListServiceDTO dto);
    ResultVO searchListInRoot(ListServiceDTO dto);
    ResultVO getProblem(FullInfoServiceDTO dto);
    ResultVO getProblemInRoot(FullInfoServiceDTO dto);
    ResultVO insertProblem(FullInfoServiceDTO dto);
    ResultVO updateProblem(FullInfoServiceDTO dto);
    ResultVO deleteProblem(FullInfoServiceDTO dto);
    ResultVO getCase(CaseInputDTO dto);
    ResultVO submitCase(CasePairDTO dto);
    ResultVO recordListByName(String cnname);
    ResultVO recordListAll();
}
