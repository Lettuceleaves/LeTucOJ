package com.LetucOJ.contest.controller;

import com.LetucOJ.common.result.Result;
import com.LetucOJ.common.result.ResultVO;
import com.LetucOJ.common.result.errorcode.BaseErrorCode;
import com.LetucOJ.contest.model.RecordDTO;
import com.LetucOJ.contest.repos.MybatisRepos;
import com.LetucOJ.contest.service.PracticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
            Integer res = mybatisRepos.insertRecord(new RecordDTO(pname, cnname, qname, lang, code, result.getCode() + " $ " + result.getMessage(), 0L, 0L, System.currentTimeMillis()));
            if (res == null || res <= 0) {
                return Result.failure(BaseErrorCode.SERVICE_ERROR);
            } else {
                return result;
            }
        } catch (Exception e) {
            return Result.failure(BaseErrorCode.SERVICE_ERROR);
        }
    }

    @PostMapping("/submitInRoot")
    public ResultVO submitRoot(@RequestParam("pname") String pname, @RequestParam("cnname") String cnname, @RequestParam("qname") String qname, @RequestParam("ctname") String ctname, @RequestParam("lang") String lang, @RequestBody String code) throws Exception {
        ResultVO result =  practiceService.submit(pname, cnname, qname, ctname, code, lang, true);
        try {
            Integer res = mybatisRepos.insertRecord(new RecordDTO(pname, cnname, qname, lang, code, result.getCode() + " $ " + result.getMessage(), 0L, 0L, System.currentTimeMillis()));
            if (res == null || res <= 0) {
                return Result.failure(BaseErrorCode.SERVICE_ERROR);
            } else {
                return result;
            }
        } catch (Exception e) {
            return Result.failure(BaseErrorCode.SERVICE_ERROR);
        }
    }

}