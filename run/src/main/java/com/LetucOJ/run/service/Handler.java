package com.LetucOJ.run.service;


import com.LetucOJ.common.result.ResultVO;
import com.LetucOJ.run.model.TestTaskDTO;
import com.LetucOJ.run.model.TestTaskVO;

public interface Handler {
    void setNextHandler(Handler nextHandler);
    ResultVO<TestTaskVO> handle(TestTaskDTO testTaskDTO, int boxid, byte[] config);
}