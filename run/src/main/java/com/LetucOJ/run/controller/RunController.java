package com.LetucOJ.run.controller;

import com.LetucOJ.common.result.ResultVO;
import com.LetucOJ.run.model.TestCaseDTO;
import com.LetucOJ.run.model.TestCaseVO;
import com.LetucOJ.run.service.RunService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class RunController {

    @Autowired
    RunService runService;

    @PostMapping("/run")
    public ResultVO<TestCaseVO> run(@RequestBody TestCaseDTO testCaseDTO) {
        return runService.run(testCaseDTO);
    }
}
