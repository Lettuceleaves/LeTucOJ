package com.LetucOJ.contest.controller;

import com.LetucOJ.common.anno.SubmitLimit;
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
    @SubmitLimit
    public ResultVO submit(
            @RequestParam("lang") String lang,
            @RequestParam("pname") String pname,
            @RequestParam("qname") String qname,
            @RequestParam("cnname") String cnname,
            @RequestParam("ctname") String ctname,
            @RequestBody String code) throws Exception {
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
    @SubmitLimit
    public ResultVO submitRoot(
            @RequestParam("lang") String lang,
            @RequestParam("pname") String pname,
            @RequestParam("qname") String qname,
            @RequestParam("cnname") String cnname,
            @RequestParam("ctname") String ctname,
            @RequestBody String code) throws Exception {
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