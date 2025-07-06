package com.LetucOJ.practice.controller;

import com.LetucOJ.practice.client.AdviceClient;
import com.LetucOJ.practice.model.CodeDTO;
import com.LetucOJ.practice.model.ResultVO;
import com.LetucOJ.practice.service.PracticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/practice")
public class PracticeCtrller {

    @Autowired
    private PracticeService practiceService;

    @Autowired
    private AdviceClient client;

    @PostMapping("/submitTest")
    public ResultVO submitTest(@RequestParam("code") CodeDTO messege) throws Exception {
        return practiceService.submitTest(messege);
    }

    @GetMapping("/testCaseAmount")
    public ResultVO getTestCaseAmount(@RequestParam("problemId") String problemId) {
        return practiceService.getProblemCaseAmount(problemId);
    }

}
