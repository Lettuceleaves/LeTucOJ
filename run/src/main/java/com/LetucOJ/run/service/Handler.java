package com.LetucOJ.run.service;


import com.LetucOJ.common.result.ResultVO;
import com.LetucOJ.run.model.TestCaseDTO;
import com.LetucOJ.run.model.TestCaseVO;

import java.util.List;

public interface Handler {
    void setNextHandler(Handler nextHandler);
    ResultVO<TestCaseVO> handle(TestCaseDTO testCaseDTO, int boxid, byte[] config);
}