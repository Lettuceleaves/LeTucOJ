package com.LetucOJ.practice.service;

import com.LetucOJ.common.result.ResultVO;

public interface PracticeService {
    ResultVO<Integer> submit(String userName, String problemName, String code, String language, boolean root) throws Exception;
}
