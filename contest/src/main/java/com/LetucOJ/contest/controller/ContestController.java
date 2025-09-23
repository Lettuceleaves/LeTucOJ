package com.LetucOJ.contest.controller;

import com.LetucOJ.common.result.ResultVO;
import com.LetucOJ.contest.model.db.ContestInfoDTO;
import com.LetucOJ.contest.model.net.ContestProblemDTO;
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

    @GetMapping("/full/getProblem")
    public ResultVO getProblem(@RequestParam("qname") String qname, @RequestParam("ctname") String contestName) throws Exception {
        return dbService.getProblem(qname, contestName);
    }

    @GetMapping("/full/getProblemInRoot")
    public ResultVO getProblemInRoot(@RequestParam("qname") String qname, @RequestParam("ctname") String contestName) throws Exception {
        return dbService.getProblemInRoot(qname, contestName);
    }

    @GetMapping("/full/getContest")
    public ResultVO getContest(@RequestParam("ctname") String ctname) throws Exception {
        return dbService.getContest(ctname);
    }

    @GetMapping("/full/getContestInRoot")
    public ResultVO getContestInRoot(@RequestParam("ctname") String ctname) throws Exception {
        return dbService.getContestInRoot(ctname);
    }

    @PostMapping("/insertContest")
    public ResultVO insertContest(@RequestBody ContestInfoDTO dto) throws Exception {
        return dbService.insertContest(dto);
    }

    @PutMapping("/updateContest")
    public ResultVO updateContest(@RequestBody ContestInfoDTO dto) throws Exception {
        return dbService.updateContest(dto);
    }

    @DeleteMapping("/deleteContest")
    public ResultVO deleteContest(@RequestParam("ctname") String ctname) throws Exception {
        return dbService.deleteContest(ctname);
    }

    @PostMapping("/insertProblem")
    public ResultVO insertProblem(@RequestBody ContestProblemDTO dto) throws Exception {
        return dbService.insertProblem(dto);
    }

    @DeleteMapping("/deleteProblem")
    public ResultVO deleteProblem(@RequestBody ContestProblemDTO dto) throws Exception {
        return dbService.deleteProblem(dto);
    }

    @PostMapping("/attend")
    public ResultVO attendContest(@RequestParam("pname") String pname, @RequestParam("cnname") String cnname, @RequestParam("ctname") String contestName) throws Exception {
        return dbService.attend(pname, cnname, contestName);
    }

    @GetMapping("/inContest")
    public ResultVO inContest(@RequestParam("pname") String pname, @RequestParam("ctname") String contestName) throws Exception {
        return dbService.getUserStatus(pname, contestName);
    }
}
