package com.LetucOJ.run.service;

import com.LetucOJ.common.result.ResultVO;
import com.LetucOJ.run.model.TestTaskDTO;

public interface RunService {
     ResultVO<Integer> run(TestTaskDTO testTaskDTO);
}