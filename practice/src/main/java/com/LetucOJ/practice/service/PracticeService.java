package com.LetucOJ.practice.service;

import com.LetucOJ.practice.model.CodeDTO;
import com.LetucOJ.practice.model.ResultVO;

public interface PracticeService {
    ResultVO submitTest(CodeDTO messege) throws Exception;
    ResultVO getProblemCaseAmount(String problemId);
}
