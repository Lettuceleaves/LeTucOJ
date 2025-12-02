package com.LetucOJ.practice.service;

import com.LetucOJ.common.result.ResultVO;
import com.LetucOJ.practice.model.Case;
import com.LetucOJ.practice.model.Problem;
import com.LetucOJ.practice.model.DTO.ListConditionDTO;

public interface DBService {
    ResultVO getList(ListConditionDTO dto, String name);
    ResultVO getListInRoot(ListConditionDTO dto, String name);
    ResultVO searchList(ListConditionDTO dto, String name);
    ResultVO searchListInRoot(ListConditionDTO dto, String name);
    ResultVO getProblem(String name);
    ResultVO getProblemInRoot(String name);
    ResultVO insertProblem(Problem dto);
    ResultVO updateProblem(Problem dto);
    ResultVO deleteProblem(String name);
    ResultVO getCase(CaseInputDTO dto);
    ResultVO submitCase(Case dto);
    ResultVO recordListByName(String pname, int start, int limit);
    ResultVO recordListAll(int start, int limit);
    ResultVO getExistCase(String qname, Integer id);
    ResultVO getConfigFile(String qname);
}
