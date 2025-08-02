package com.LetucOJ.practice.controller;

import com.LetucOJ.practice.model.CodeDTO;
import com.LetucOJ.practice.model.RecordDTO;
import com.LetucOJ.practice.model.ResultVO;
import com.LetucOJ.practice.repos.MybatisRepos;
import com.LetucOJ.practice.service.PracticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/practice")
public class SubmitController {

    @Autowired
    private PracticeService practiceService;

    @Autowired
    private MybatisRepos mybatisRepos;

    @PostMapping("/submit")
    public ResultVO submit(@RequestBody CodeDTO message, @RequestParam String name, @RequestParam String cnname) throws Exception {
        ResultVO result = practiceService.submit(message, false);
        mybatisRepos.insertRecord(new RecordDTO(name, cnname, message.getName(), "C", message.getCode(), result.getStatus() + " $ " + result.getError(), 0L, 0L, System.currentTimeMillis()));
        return result;
    }

    @PostMapping("/submitInRoot")
    public ResultVO submitInRoot(@RequestBody CodeDTO message, @RequestParam String name, @RequestParam String cnname) throws Exception {
        ResultVO result = practiceService.submit(message, true);
        mybatisRepos.insertRecord(new RecordDTO(name, cnname, message.getName(), "C", message.getCode(), result.getStatus() + " $ " + result.getError(), 0L, 0L, System.currentTimeMillis()));
        return result;
    }

}
