package com.LetucOJ.practice.controller;

import com.LetucOJ.practice.model.RecordDTO;
import com.LetucOJ.practice.model.ResultVO;
import com.LetucOJ.practice.repos.MybatisRepos;
import com.LetucOJ.practice.service.PracticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/practice")
public class SubmitController {

    @Autowired
    private PracticeService practiceService;

    @Autowired
    private MybatisRepos mybatisRepos;

    @PostMapping("/submit")
    public ResultVO submit(@RequestParam("pname") String pname, @RequestParam("cnname") String cnname, @RequestParam("qname") String qname, @RequestParam("lang") String lang, @RequestBody String code) throws Exception {
        ResultVO result = practiceService.submit(pname, qname, code, false);
        Integer res = mybatisRepos.insertRecord(new RecordDTO(pname, cnname, qname, lang, code, result.getStatus() + " $ " + result.getError(), 0L, 0L, System.currentTimeMillis()));
        if (res == null || res <= 0) {
            return new ResultVO((byte) 5, null, "practice/submit: Failed to insert record into database");
        }
        return result;
    }

    @PostMapping("/submitInRoot")
    public ResultVO submitInRoot(@RequestParam("pname") String pname, @RequestParam("cnname") String cnname, @RequestParam("qname") String qname, @RequestParam("lang") String lang, @RequestBody String code) throws Exception {
        ResultVO result = practiceService.submit(pname, qname, code, true);
        try {
            Integer res = mybatisRepos.insertRecord(new RecordDTO(pname, cnname, qname, lang, code, result.getStatus() + " $ " + result.getError(), 0L, 0L, System.currentTimeMillis()));
            if (res == null || res <= 0) {
                return new ResultVO((byte) 5, null, "practice/submitInRoot: Failed to insert record into database");
            }
            return result;
        } catch (Exception e) {
            return new ResultVO((byte) 5, null, "practice/submitInRoot: " + e.getMessage());
        }
    }
}
