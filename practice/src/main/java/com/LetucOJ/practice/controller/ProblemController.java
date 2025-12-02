package com.LetucOJ.practice.controller;

import com.LetucOJ.common.result.ResultVO;
import com.LetucOJ.practice.model.CaseFile;
import com.LetucOJ.practice.model.DTO.TestCaseDTO;
import com.LetucOJ.practice.model.Problem;
import com.LetucOJ.practice.model.VO.TestTaskVO;
import com.LetucOJ.practice.service.DBService;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/practice")
@Data
@AllArgsConstructor
public class ProblemController {

    private DBService dbService;

    @GetMapping("/problem")
    public ResultVO<Problem> getProblem(@RequestParam("problem_name") String problemName) throws Exception {
        return dbService.getProblem(problemName);
    }

    @GetMapping("/problem_root")
    public ResultVO<Problem> getProblemInRoot(@RequestParam("problem_name") String problemName) throws Exception {
        return dbService.getProblemInRoot(problemName);
    }

    @PostMapping("/problem")
    public ResultVO<Void> insertProblem(@RequestBody Problem dto) throws Exception {
        return dbService.insertProblem(dto);
    }

    @PutMapping("/problem")
    public ResultVO<Void> updateProblem(@RequestBody Problem dto) throws Exception {
        return dbService.updateProblem(dto);
    }

    @DeleteMapping("/problem")
    public ResultVO<Void> deleteProblem(@RequestParam("problem_name") String problemName) throws Exception {
        return dbService.deleteProblem(problemName);
    }

    @PostMapping("/test_case")
    public ResultVO<TestTaskVO> testCase(@RequestParam String language, @RequestBody TestCaseDTO testCaseDTO) {
        return dbService.testCase(testCaseDTO, language);
    }

    @GetMapping("/get_case")
    public ResultVO<CaseFile> getCase(@RequestParam("qname") String qname, @RequestParam("id") Integer id) {
        return dbService.getCase(qname, id);
    }

    @GetMapping("/configfile")
    public ResultVO<byte[]> getConfigFile(@RequestParam("qname") String qname) {
        return dbService.getConfigFile(qname);
    }

    @PostMapping("/save_case")
    public ResultVO<Void> saveCase(@RequestBody CaseFile CaseFile) {
        return dbService.saveCase(CaseFile);
    }

}
