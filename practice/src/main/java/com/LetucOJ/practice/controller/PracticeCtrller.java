package com.LetucOJ.practice.controller;

import com.LetucOJ.practice.client.AdviceClient;
import com.LetucOJ.practice.model.*;
import com.LetucOJ.practice.service.DBService;
import com.LetucOJ.practice.service.PracticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/practice")
public class PracticeCtrller {

    @Autowired
    private PracticeService practiceService;

    @Autowired
    private DBService dbService;

    @Autowired
    private AdviceClient client;

    @PostMapping("/submitTest")
    public ResultVO submitTest(@RequestParam("code") CodeDTO message) throws Exception {
        return practiceService.submitTest(message);
    }

    @PostMapping("/basicinfo")
    public BasicInfoVO getTestCaseAmount(@RequestParam("sql") BasicInfoServiceDTO sql) throws Exception {
        return dbService.BasicDBServiceSelector(sql);
    }

    @PostMapping("/fullinfo")
    public FullInfoVO getTestCaseAmount(@RequestParam("sql") FullInfoServiceDTO sql) throws Exception {
        return dbService.FullDBServiceSelector(sql);
    }

}
