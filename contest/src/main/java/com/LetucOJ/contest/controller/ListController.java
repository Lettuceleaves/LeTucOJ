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

    @GetMapping("/list/problem")
    public ResultVO getProblemList(@RequestParam("ctname") String ctname) throws Exception {
        return dbService.getProblemList(ctname);
    }

    @GetMapping("/list/contest")
    public ResultVO getContestList() throws Exception {
        return dbService.getContestList();
    }

    @GetMapping("/list/board")
    public ResultVO getBoardList(@RequestParam("ctname") String ctname) throws Exception {
        return dbService.getBoard(ctname);
    }

}
