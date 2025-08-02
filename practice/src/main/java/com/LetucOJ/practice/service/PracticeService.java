package com.LetucOJ.practice.service;

import com.LetucOJ.practice.model.CodeDTO;
import com.LetucOJ.practice.model.ResultVO;

public interface PracticeService {
    ResultVO submit(CodeDTO message, boolean root) throws Exception;
}
