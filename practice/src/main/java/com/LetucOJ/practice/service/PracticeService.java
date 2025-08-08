package com.LetucOJ.practice.service;

import com.LetucOJ.practice.model.ResultVO;

public interface PracticeService {
    ResultVO submit(String qname, String code, boolean root) throws Exception;
}
