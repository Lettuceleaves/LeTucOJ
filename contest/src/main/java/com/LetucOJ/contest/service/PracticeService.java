package com.LetucOJ.contest.service;

import com.LetucOJ.common.result.ResultVO;

public interface PracticeService {
    ResultVO<TestTaskVO> submit(String userName, String cnname, String problemName, String contestName, String code, String lang, boolean root) throws Exception;
}
