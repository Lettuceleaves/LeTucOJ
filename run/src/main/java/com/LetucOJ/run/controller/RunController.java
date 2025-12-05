package com.LetucOJ.run.controller;

import com.LetucOJ.common.result.ResultVO;
import com.LetucOJ.run.model.TestCaseDTO;
import com.LetucOJ.run.model.TestTaskDTO;
import com.LetucOJ.run.model.TestTaskVO;
import com.LetucOJ.run.service.RunService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
public class RunController {

    RunService runService;

    @PostMapping("/task")
    public ResultVO<TestTaskVO> runTestTask(@RequestBody TestTaskDTO testTaskDTO) {
        return runService.runTestTask(testTaskDTO);
    }

    @PostMapping("/case")
    public ResultVO<TestTaskVO> runTestCase(@RequestBody TestCaseDTO testCaseDTO) {
        return runService.runTestCase(testCaseDTO);
    }
}
