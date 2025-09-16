package com.LetucOJ.practice.service;

import com.LetucOJ.practice.model.*;

public interface DBService {
    ResultVO getList(ListServiceDTO dto, String name);
    ResultVO getListInRoot(ListServiceDTO dto, String name);
    ResultVO searchList(ListServiceDTO dto, String name);
    ResultVO searchListInRoot(ListServiceDTO dto, String name);
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
