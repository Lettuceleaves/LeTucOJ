package com.LetucOJ.practice.controller;

import com.LetucOJ.common.result.Result;
import com.LetucOJ.common.result.ResultVO;
import com.LetucOJ.common.result.errorcode.BaseErrorCode;
import com.LetucOJ.practice.model.RecordDTO;
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
    public ResultVO submit(@RequestParam("pname") String pname, @RequestParam("cnname") String cnname, @RequestParam("qname") String qname, @RequestParam("language") String language, @RequestBody String code) throws Exception {
        ResultVO result = practiceService.submit(pname, qname, code, language, false);
        Integer res = mybatisRepos.insertRecord(new RecordDTO(pname, cnname, qname, language, code, result.getCode() + " $ " + result.getMessage(), 0L, 0L, System.currentTimeMillis()));
        if (res == null || res <= 0) {
            return Result.failure(BaseErrorCode.SERVICE_ERROR);
        }
        return result;
    }

    @PostMapping("/submitInRoot")
    public ResultVO submitInRoot(@RequestParam("pname") String pname, @RequestParam("cnname") String cnname, @RequestParam("qname") String qname, @RequestParam("language") String language, @RequestBody String code) throws Exception {
        ResultVO result = practiceService.submit(pname, qname, code, language, true);
        try {
            Integer res = mybatisRepos.insertRecord(new RecordDTO(pname, cnname, qname, language, code, result.getCode() + " $ " + result.getMessage(), 0L, 0L, System.currentTimeMillis()));
            if (res == null || res <= 0) {
                return Result.failure(BaseErrorCode.SERVICE_ERROR);
            }
            return result;
        } catch (Exception e) {
            return Result.failure(BaseErrorCode.SERVICE_ERROR);
        }
    }
}
