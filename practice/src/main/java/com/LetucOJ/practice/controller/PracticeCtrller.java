package com.LetucOJ.practice.controller;

import com.LetucOJ.practice.client.AdviceClient;
import com.LetucOJ.practice.model.*;
import com.LetucOJ.practice.service.DBService;
import com.LetucOJ.practice.service.PracticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/practice")
@CrossOrigin(origins = "*")
public class PracticeCtrller {

    @Autowired
    private PracticeService practiceService;

    @Autowired
    private DBService dbService;

    @Autowired
    private AdviceClient client;

    @PostMapping("/hello")
    public String hello() {
        return "Hello, this is the Run Service!";
    }

    @PostMapping("/submitTest")
    public ResultVO submitTest(@RequestBody CodeDTO message) throws Exception {
        return practiceService.submitTest(message);
    }

    @PostMapping("/basicinfo")
    public BasicInfoVO getTestCaseAmount(@RequestBody BasicInfoServiceDTO sql) throws Exception {
        return dbService.BasicDBServiceSelector(sql);
    }

    @PostMapping("/fullinfo")
    public FullInfoVO getTestCaseAmount(@RequestBody FullInfoServiceDTO sql) throws Exception {
        return dbService.FullDBServiceSelector(sql);
    }

    @PostMapping("/getCase")
    public ResultVO getCase(@RequestBody CaseInputDTO caseInputDTO) {
        return practiceService.getCase(caseInputDTO);
    }

    @PostMapping("/submitCase")
    public ResultVO submitCase(@RequestBody CasePairDTO casePairDTO) {
        return practiceService.submitCase(casePairDTO);
    }

}
