package com.LetucOJ.contest.controller;

import com.LetucOJ.contest.model.net.ResultVO;
import com.LetucOJ.contest.service.DBService;
import com.LetucOJ.contest.service.PracticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/contest")
public class ListController {

    @Autowired
    private PracticeService practiceService;

    @Autowired
    private DBService dbService;

    @PostMapping("/list/problem")
    public ResultVO getProblemList(@RequestHeader("contestName") String contestName) throws Exception {
        return dbService.getProblemList(contestName);
    }

    @PostMapping("/list/contest")
    public ResultVO getContestList() throws Exception {
        return dbService.getContestList();
    }

    @PostMapping("/list/board")
    public ResultVO getBoardList(@RequestBody String name) throws Exception {
        return dbService.getBoard(name);
    }

}
