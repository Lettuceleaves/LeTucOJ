package com.LetucOJ.practice.controller;

import com.LetucOJ.practice.model.CaseInputDTO;
import com.LetucOJ.practice.model.CasePairDTO;
import com.LetucOJ.practice.model.FullInfoDTO;
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

    @GetMapping("/full/get")
    public ResultVO getProblem(@RequestParam("qname") String qname) throws Exception {
        return dbService.getProblem(qname);
    }

    @GetMapping("/fullRoot/get")
    public ResultVO getProblemInRoot(@RequestParam("qname") String qname) throws Exception {
        return dbService.getProblemInRoot(qname);
    }

    @PostMapping("/fullRoot/insert")
    public ResultVO insertProblem(@RequestBody FullInfoDTO dto) throws Exception {
        return dbService.insertProblem(dto);
    }

    @PutMapping("/fullRoot/update")
    public ResultVO updateProblem(@RequestBody FullInfoDTO dto) throws Exception {
        return dbService.updateProblem(dto);
    }

    @DeleteMapping("/fullRoot/delete")
    public ResultVO deleteProblem(@RequestParam("qname") String qname) throws Exception {
        return dbService.deleteProblem(qname);
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
