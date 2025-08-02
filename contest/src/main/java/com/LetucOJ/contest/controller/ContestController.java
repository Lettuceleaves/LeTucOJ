package com.LetucOJ.contest.controller;

import com.LetucOJ.contest.model.net.ContestProblemDTO;
import com.LetucOJ.contest.model.net.ContestServiceDTO;
import com.LetucOJ.contest.model.net.FullInfoServiceDTO;
import com.LetucOJ.contest.model.net.ResultVO;
import com.LetucOJ.contest.service.DBService;
import com.LetucOJ.contest.service.PracticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/contest")
public class ContestController {

    @Autowired
    private PracticeService practiceService;

    @Autowired
    private DBService dbService;

    @PostMapping("/full/getProblem")
    public ResultVO getProblem(@RequestBody FullInfoServiceDTO dto, @RequestHeader("contestName") String contestName) throws Exception {
        return dbService.getProblem(dto, contestName);
    }

    @PostMapping("/full/getContest")
    public ResultVO getContest(@RequestBody ContestServiceDTO dto) throws Exception {
        return dbService.getContest(dto);
    }

    @PostMapping("/insertContest")
    public ResultVO insertContest(@RequestBody ContestServiceDTO dto) throws Exception {
        return dbService.insertContest(dto);
    }

    @PostMapping("/updateContest")
    public ResultVO updateContest(@RequestBody ContestServiceDTO dto) throws Exception {
        return dbService.updateContest(dto);
    }

    @PostMapping("/deleteContest")
    public ResultVO deleteContest(@RequestBody ContestServiceDTO dto) throws Exception {
        return dbService.deleteContest(dto);
    }

    @PostMapping("/insertProblem")
    public ResultVO insertProblem(@RequestBody ContestProblemDTO dto) throws Exception {
        return dbService.insertProblem(dto);
    }

    @PostMapping("/deleteProblem")
    public ResultVO deleteProblem(@RequestBody ContestProblemDTO dto) throws Exception {
        return dbService.deleteProblem(dto);
    }

    @PostMapping("/attend")
    public ResultVO attendContest(@RequestHeader("cnname") String cnname, @RequestHeader("contestName") String contestName) throws Exception {
        return dbService.attend(cnname, contestName);
    }
}
