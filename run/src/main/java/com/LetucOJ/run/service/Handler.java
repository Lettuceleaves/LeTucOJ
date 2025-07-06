package com.LetucOJ.run.service;


import com.LetucOJ.run.model.vo.ResultVO;
import com.LetucOJ.run.tool.path;

import java.util.List;

public interface Handler {
    void setNextHandler(Handler nextHandler);
    ResultVO handle(List<String> inputFiles, path path);
}
