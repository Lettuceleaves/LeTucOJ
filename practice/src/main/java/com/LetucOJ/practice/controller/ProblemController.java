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
    public ResultVO<Problem> getProblem(@RequestParam("problem_name") String problemName, String role) throws Exception {
        return dbService.getProblem(problemName, role);
    }

    @PostMapping("/problem")
    public ResultVO<Void> insertProblem(@RequestBody Problem problem) throws Exception {
        return dbService.insertProblem(problem);
    }

    @PutMapping("/problem")
    public ResultVO<Void> updateProblem(@RequestBody Problem problem) throws Exception {
        return dbService.updateProblem(problem);
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
    public ResultVO<CaseFile> getCase(@RequestParam("problem_name") String problemName, @RequestParam("id") Integer id) {
        return dbService.getCase(problemName, id);
    }

    @GetMapping("/config_file")
    public ResultVO<byte[]> getConfigFile(@RequestParam("problem_name") String problemName) {
        return dbService.getConfigFile(problemName);
    }

    @PostMapping("/save_case")
    public ResultVO<Void> saveCase(@RequestBody CaseFile CaseFile) {
        return dbService.saveCase(CaseFile);
    }
}
