package com.LetucOJ.run.controller;

import com.LetucOJ.run.model.vo.ResultVO;
import com.LetucOJ.run.service.RunService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class RunCtrller {

    @Autowired
    RunService runService;

    @PostMapping("/runFeign")
    public ResultVO run(@RequestBody List<String> files) {
        return runService.run(files);
    }

    @PostMapping("/runTestFeign")
    public ResultVO runTest(@RequestBody List<String> files) {
        return runService.run(files);
    }
}
