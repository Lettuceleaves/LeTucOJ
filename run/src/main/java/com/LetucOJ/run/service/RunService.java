package com.LetucOJ.run.service;

import com.LetucOJ.common.result.ResultVO;
import com.LetucOJ.run.model.TestCaseDTO;
import com.LetucOJ.run.model.TestTaskDTO;
import com.LetucOJ.run.model.TestTaskVO;

public interface RunService {
     ResultVO<TestTaskVO> runTestTask(TestTaskDTO testTaskDTO);
     ResultVO<TestTaskVO> runTestCase(TestCaseDTO testCaseDTO);
}