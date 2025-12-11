package com.LetucOJ.contest.controller;

import com.LetucOJ.common.result.ResultVO;
import com.LetucOJ.contest.model.Contest;
import com.LetucOJ.contest.model.DTO.ContestProblemDTO;
import com.LetucOJ.contest.model.Problem;
import com.LetucOJ.contest.service.DBService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/contest")
@AllArgsConstructor
public class ContestController {

    private DBService dbService;

    @GetMapping("/problem")
    public ResultVO<Problem> getProblem(@RequestParam("user_name") String userName,
                                        @RequestParam("contest_name") String contestName,
                                        @RequestParam("problem_name") String problemName,
                                        @RequestParam("role") String role) {
        return dbService.getProblem(userName, contestName, problemName, role);
    }

    @GetMapping("/contest")
    public ResultVO<Contest> getContest(@RequestParam("contest_name") String contestName,
                                        @RequestParam("role") String role) {
        return dbService.getContest(contestName, role);
    }

    @PostMapping("/contest")
    public ResultVO<Void> insertContest(@RequestBody Contest dto) {
        return dbService.insertContest(dto);
    }

    @PutMapping("/contest")
    public ResultVO<Void> updateContest(@RequestBody Contest dto) {
        return dbService.updateContest(dto);
    }

    @PostMapping("/problem")
    public ResultVO<Void> insertProblem(@RequestBody ContestProblemDTO dto) {
        return dbService.insertProblem(dto);
    }

    @DeleteMapping("/problem")
    public ResultVO<Void> deleteProblem(@RequestBody ContestProblemDTO dto) {
        return dbService.deleteProblem(dto);
    }

    @PostMapping("/attend")
    public ResultVO<Void> attendContest(@RequestParam("user_name") String userName,
                                        @RequestParam("nick_name") String nickName,
                                        @RequestParam("contest_name") String contestName,
                                        @RequestParam("password") String password) {
        return dbService.attend(userName, nickName, contestName, password);
    }

    @GetMapping("/attended")
    public ResultVO<Void> inContest(@RequestParam("user_name") String userName,
                                    @RequestParam("contest_name") String contestName) {
        return dbService.attended(userName, contestName);
    }
}
