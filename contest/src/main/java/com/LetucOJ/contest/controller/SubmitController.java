package com.LetucOJ.contest.controller;

import com.LetucOJ.common.anno.SubmitLimit;
import com.LetucOJ.common.result.Result;
import com.LetucOJ.common.result.ResultVO;
import com.LetucOJ.common.result.errorcode.BaseErrorCode;
import com.LetucOJ.contest.model.DTO.RecordDTO;
import com.LetucOJ.contest.model.VO.TestTaskVO;
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
    public ResultVO<TestTaskVO> submit(
            @RequestParam("language") String lang,
            @RequestParam("user_name") String userName,
            @RequestParam("problem_name") String problemName,
            @RequestParam("nick_name") String nickName,
            @RequestParam("contest_name") String contestName,
            @RequestBody String code) throws Exception {
        ResultVO<TestTaskVO> result =  practiceService.submit(userName, nickName, problemName, contestName, code, lang, false);
        return saveRecord(lang, userName, problemName, nickName, code, result);
    }

    private ResultVO<TestTaskVO> saveRecord(@RequestParam("language") String lang, @RequestParam("user_name") String userName, @RequestParam("problem_name") String problemName, @RequestParam("nick_name") String nickName, @RequestBody String code, ResultVO<TestTaskVO> result) {
        try {
            Integer res = mybatisRepos.insertRecord(new RecordDTO(userName, nickName, problemName, lang, code, result.getCode() + " $ " + result.getMessage(), 0L, 0L, System.currentTimeMillis()));
            if (res == null || res <= 0) {
                return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
            } else {
                return result;
            }
        } catch (Exception e) {
            return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
        }
    }

    @PostMapping("/submitInRoot")
    @SubmitLimit
    public ResultVO<TestTaskVO> submitRoot(
            @RequestParam("lang") String lang,
            @RequestParam("pname") String pname,
            @RequestParam("qname") String qname,
            @RequestParam("cnname") String cnname,
            @RequestParam("ctname") String ctname,
            @RequestBody String code) throws Exception {
        ResultVO<TestTaskVO> result =  practiceService.submit(pname, cnname, qname, ctname, code, lang, true);
        return saveRecord(lang, pname, qname, cnname, code, result);
    }

}