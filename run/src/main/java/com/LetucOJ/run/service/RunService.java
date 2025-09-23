package com.LetucOJ.run.service;

import com.LetucOJ.common.result.ResultVO;

import java.util.List;

public interface RunService {
     ResultVO run(List<String> inputFiles, String language);
}