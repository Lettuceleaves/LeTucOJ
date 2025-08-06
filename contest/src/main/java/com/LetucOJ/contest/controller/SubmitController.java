package com.LetucOJ.contest.controller;

import com.LetucOJ.contest.model.db.RecordDTO;
import com.LetucOJ.contest.model.net.CodeDTO;
import com.LetucOJ.contest.model.net.ResultVO;
import com.LetucOJ.contest.repos.MybatisRepos;
import com.LetucOJ.contest.service.PracticeService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/practice")
@Api(tags = {"contest", "submit"})
public class SubmitController {

    @Autowired
    private PracticeService practiceService;

    @Autowired
    private MybatisRepos mybatisRepos;

    @PostMapping("/submit")
    public ResultVO submit(@RequestBody CodeDTO message, @RequestParam String name, @RequestParam String cnname) throws Exception {
        ResultVO result =  practiceService.submit(message, true, cnname);
        mybatisRepos.insertRecord(new RecordDTO(name, cnname, message.getProblemName(), "C", message.getCode(), result.getStatus() + " $ " + result.getError(), 0L, 0L, System.currentTimeMillis()));
        return result;
    }

}