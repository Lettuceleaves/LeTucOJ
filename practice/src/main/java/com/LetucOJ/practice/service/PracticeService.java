package com.LetucOJ.practice.service;

import com.LetucOJ.common.result.ResultVO;
import com.LetucOJ.practice.model.VO.TestTaskVO;

public interface PracticeService {
    ResultVO<TestTaskVO> submit(String userName, String problemName, String code, String language, boolean root) throws Exception;
}
