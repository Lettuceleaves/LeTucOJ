package com.LetucOJ.practice.service;

import com.LetucOJ.common.result.ResultVO;

public interface PracticeService {
    ResultVO submit(String pname, String qname, String code, String language, boolean root) throws Exception;
}
