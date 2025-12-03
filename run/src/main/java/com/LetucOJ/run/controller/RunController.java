package com.LetucOJ.run.controller;

import com.LetucOJ.common.result.ResultVO;
import com.LetucOJ.run.model.TestTaskDTO;
import com.LetucOJ.run.service.RunService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
public class RunController {

    RunService runService;

    @PostMapping("/run")
    public ResultVO<Integer> run(@RequestBody TestTaskDTO testTaskDTO) {
        return runService.run(testTaskDTO);
    }
}
