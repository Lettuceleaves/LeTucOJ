package com.LetucOJ.contest.service;

import com.LetucOJ.contest.model.net.CodeDTO;
import com.LetucOJ.contest.model.net.ResultVO;

public interface PracticeService {
    ResultVO submit(CodeDTO message, boolean root, String cnname) throws Exception;
}
