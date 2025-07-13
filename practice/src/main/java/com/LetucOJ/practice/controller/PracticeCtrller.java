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

    @CrossOrigin(origins = "*") // 指定允许的来源
    @PostMapping("/submitTest")
    public ResultVO submitTest(@RequestBody CodeDTO message) throws Exception {
        return practiceService.submitTest(message);
    }
    @CrossOrigin(origins = "*") // 指定允许的来源
    @PostMapping("/basicinfo")
    public BasicInfoVO getTestCaseAmount(@RequestBody BasicInfoServiceDTO sql) throws Exception {
        return dbService.BasicDBServiceSelector(sql);
    }
    @CrossOrigin(origins = "*") // 指定允许的来源
    @PostMapping("/fullinfo")
    public FullInfoVO getTestCaseAmount(@RequestBody FullInfoServiceDTO sql) throws Exception {
        return dbService.FullDBServiceSelector(sql);
    }

}
