package com.LetucOJ.run.controller;

import com.LetucOJ.common.result.ResultVO;
import com.LetucOJ.run.service.RunService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class RunController {

    @Autowired
    RunService runService;

    @PostMapping("/run")
    public ResultVO run(@RequestBody List<String> files, @RequestParam String language) {
        return runService.run(files, language);
    }
}
