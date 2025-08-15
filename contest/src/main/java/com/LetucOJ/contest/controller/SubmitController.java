package com.LetucOJ.contest.controller;

import com.LetucOJ.contest.model.db.RecordDTO;
import com.LetucOJ.contest.model.net.ResultVO;
import com.LetucOJ.contest.repos.MybatisRepos;
import com.LetucOJ.contest.service.PracticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/contest")
public class SubmitController {

    @Autowired
    private PracticeService practiceService;

    @Autowired
    private MybatisRepos mybatisRepos;

    @PostMapping("/submit")
    public ResultVO submit(@RequestParam("pname") String pname, @RequestParam("cnname") String cnname, @RequestParam("qname") String qname, @RequestParam("ctname") String ctname, @RequestParam("lang") String lang, @RequestBody String code) throws Exception {
        ResultVO result =  practiceService.submit(pname, cnname, qname, ctname, code, lang, false);
        try {
            Integer res = mybatisRepos.insertRecord(new RecordDTO(pname, cnname, qname, lang, code, result.getStatus() + " $ " + result.getError(), 0L, 0L, System.currentTimeMillis()));
            if (res == null || res <= 0) {
                return new ResultVO((byte) 5, null, "contest/submit: Failed to insert record into database");
            } else {
                return result;
            }
        } catch (Exception e) {
            return new ResultVO((byte) 5, null, "contest/submit: " + e.getMessage());
        }
    }

    @PostMapping("/submitInRoot")
    public ResultVO submitRoot(@RequestParam("pname") String pname, @RequestParam("cnname") String cnname, @RequestParam("qname") String qname, @RequestParam("ctname") String ctname, @RequestParam("lang") String lang, @RequestBody String code) throws Exception {
        ResultVO result =  practiceService.submit(pname, cnname, qname, ctname, code, lang, true);
        try {
            Integer res = mybatisRepos.insertRecord(new RecordDTO(pname, cnname, qname, lang, code, result.getStatus() + " $ " + result.getError(), 0L, 0L, System.currentTimeMillis()));
            if (res == null || res <= 0) {
                return new ResultVO((byte) 5, null, "contest/submit: Failed to insert record into database");
            } else {
                return result;
            }
        } catch (Exception e) {
            return new ResultVO((byte) 5, null, "contest/submit: " + e.getMessage());
        }
    }

}