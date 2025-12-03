package com.LetucOJ.contest.controller;

import com.LetucOJ.common.result.ResultVO;
import com.LetucOJ.contest.model.VO.BoardVO;
import com.LetucOJ.contest.model.VO.ContestListVO;
import com.LetucOJ.contest.model.VO.ContestProblemListVO;
import com.LetucOJ.contest.service.DBService;
import com.LetucOJ.contest.service.PracticeService;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/contest")
@Data
@AllArgsConstructor
public class ListController {

    private PracticeService practiceService;

    private DBService dbService;

    @GetMapping("/problems")
    public ResultVO<ContestProblemListVO> getProblemList(@RequestParam("contest_name") String ctname,
                                                         @RequestParam("role") String role) throws Exception {
        return dbService.getProblemList(ctname, role);
    }

    @GetMapping("/contests")
    public ResultVO<ContestListVO> getContestList() throws Exception {
        return dbService.getContestList();
    }

    @GetMapping("/board")
    public ResultVO<BoardVO> getBoardList(@RequestParam("contest_name") String ctname,
                                          @RequestParam("role") String role) throws Exception {
        return dbService.getBoard(ctname, role);
    }

}
