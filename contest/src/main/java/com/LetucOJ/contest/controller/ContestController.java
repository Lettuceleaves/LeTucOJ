package com.LetucOJ.contest.controller;

import com.LetucOJ.common.result.ResultVO;
import com.LetucOJ.contest.model.Contest;
import com.LetucOJ.contest.model.DTO.ContestProblemDTO;
import com.LetucOJ.contest.service.DBService;
import com.LetucOJ.contest.service.PracticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.parsing.Problem;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/contest")
public class ContestController {

    @Autowired
    private PracticeService practiceService;

    @Autowired
    private DBService dbService;

    @GetMapping("/problem")
    public ResultVO<Problem> getProblem(@RequestParam("user_name") String userName,
                                        @RequestParam("contest_name") String contestName,
                                        @RequestParam("problem_name") String problemName,
                                        @RequestParam("role") String role) throws Exception {
        return dbService.getProblem(userName, contestName, problemName, role);
    }

    @GetMapping("/contest")
    public ResultVO<Contest> getContest(@RequestParam("contest_name") String contestName,
                                        @RequestParam("role") String role) throws Exception {
        return dbService.getContest(contestName, role);
    }

    @PostMapping("/contest")
    public ResultVO<Void> insertContest(@RequestBody Contest dto) throws Exception {
        return dbService.insertContest(dto);
    }

    @PutMapping("/contest")
    public ResultVO<Void> updateContest(@RequestBody Contest dto) throws Exception {
        return dbService.updateContest(dto);
    }

    @PostMapping("/problem")
    public ResultVO<Void> insertProblem(@RequestBody ContestProblemDTO dto) throws Exception {
        return dbService.insertProblem(dto);
    }

    @DeleteMapping("/problem")
    public ResultVO<Void> deleteProblem(@RequestBody ContestProblemDTO dto) throws Exception {
        return dbService.deleteProblem(dto);
    }

    @PostMapping("/attend")
    public ResultVO<Void> attendContest(@RequestParam("user_name") String userName, @RequestParam("nick_name") String nickName, @RequestParam("ctname") String contestName) throws Exception {
        return dbService.attend(userName, nickName, contestName);
    }

    @GetMapping("/attended")
    public ResultVO<Void> inContest(@RequestParam("user_name") String userName, @RequestParam("contestName") String contestName) throws Exception {
        return dbService.attended(userName, contestName);
    }
}
