package com.LetucOJ.contest.service;

import com.LetucOJ.common.result.ResultVO;

public interface PracticeService {
    ResultVO submit(String userName, String cnname, String questionName, String contestName, String code, String lang, boolean root) throws Exception;
}
