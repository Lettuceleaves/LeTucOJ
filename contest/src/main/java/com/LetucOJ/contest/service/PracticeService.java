package com.LetucOJ.contest.service;

import com.LetucOJ.common.result.ResultVO;
import com.LetucOJ.contest.model.VO.TestTaskVO;

public interface PracticeService {
    ResultVO<TestTaskVO> submit(String userName, String nickName, String problemName, String contestName, String code, String language, String role) throws Exception;
}
