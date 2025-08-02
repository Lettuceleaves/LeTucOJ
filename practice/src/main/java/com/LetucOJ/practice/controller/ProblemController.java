package com.LetucOJ.practice.controller;

import com.LetucOJ.practice.model.CaseInputDTO;
import com.LetucOJ.practice.model.CasePairDTO;
import com.LetucOJ.practice.model.FullInfoServiceDTO;
import com.LetucOJ.practice.model.ResultVO;
import com.LetucOJ.practice.service.DBService;
import com.LetucOJ.practice.service.PracticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/practice")
public class ProblemController {

    @Autowired
    private PracticeService practiceService;

    @Autowired
    private DBService dbService;

    @PostMapping("/full/get")
    public ResultVO getProblem(@RequestBody FullInfoServiceDTO sql) throws Exception {
        return dbService.getProblem(sql);
    }

    @PostMapping("/fullRoot/get")
    public ResultVO getProblemInRoot(@RequestBody FullInfoServiceDTO sql) throws Exception {
        return dbService.getProblemInRoot(sql);
    }

    @PostMapping("/fullRoot/insert")
    public ResultVO insertProblem(@RequestBody FullInfoServiceDTO sql) throws Exception {
        return dbService.insertProblem(sql);
    }

    @PostMapping("/fullRoot/update")
    public ResultVO updateProblem(@RequestBody FullInfoServiceDTO sql) throws Exception {
        return dbService.updateProblem(sql);
    }

    @PostMapping("/fullRoot/delete")
    public ResultVO deleteProblem(@RequestBody FullInfoServiceDTO sql) throws Exception {
        return dbService.deleteProblem(sql);
    }

    @PostMapping("/getCase")
    public ResultVO getCase(@RequestBody CaseInputDTO caseInputDTO) {
        return dbService.getCase(caseInputDTO);
    }

    @PostMapping("/submitCase")
    public ResultVO submitCase(@RequestBody CasePairDTO casePairDTO) {
        return dbService.submitCase(casePairDTO);
    }

}
