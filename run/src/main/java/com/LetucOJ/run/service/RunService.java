package com.LetucOJ.run.service;

import com.LetucOJ.run.model.vo.ResultVO;

import java.util.List;

public interface RunService {
     ResultVO run(List<String> inputFiles);
}
