package com.LetucOJ.run.service;


import com.LetucOJ.common.result.ResultVO;

import java.util.List;

public interface Handler {
    void setNextHandler(Handler nextHandler);
    ResultVO handle(List<String> inputFiles, int boxid, String language, String qname, byte[] config);
}