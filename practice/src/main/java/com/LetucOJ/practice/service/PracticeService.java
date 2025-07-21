package com.LetucOJ.practice.service;

import com.LetucOJ.practice.model.CaseInputDTO;
import com.LetucOJ.practice.model.CasePairDTO;
import com.LetucOJ.practice.model.CodeDTO;
import com.LetucOJ.practice.model.ResultVO;

public interface PracticeService {
    ResultVO submitTest(CodeDTO message) throws Exception;
    ResultVO getProblemCaseAmount(String problemId);
    ResultVO getCase(CaseInputDTO caseInputDTO);
    ResultVO submitCase(CasePairDTO casePairDTO);
}
