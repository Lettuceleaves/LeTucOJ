package com.LetucOJ.run.service;

import com.LetucOJ.run.model.vo.ResultVO;

import java.util.List;

public interface RunService {
     // String[] run(MultipartFile userFile, String problemName, String language) throws Exception;

     ResultVO run(List<String> inputFiles, boolean test) throws Exception;

     ResultVO runTest(List<String> inputFiles, boolean test) throws Exception;
}
