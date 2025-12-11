package com.LetucOJ.practice.controller;

import com.LetucOJ.common.result.Result;
import com.LetucOJ.common.result.ResultVO;
import com.LetucOJ.common.trace.TraceContext;
import com.LetucOJ.practice.model.DTO.ListConditionDTO;
import com.LetucOJ.practice.model.VO.ProblemListVO;
import com.LetucOJ.practice.model.VO.SubmitRecordListVO;
import com.LetucOJ.practice.service.DBService;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/practice")
@Data
@AllArgsConstructor
public class ListController {

    private DBService dbService;

    @GetMapping("/list")
    public ResultVO<ProblemListVO> getList(@ModelAttribute ListConditionDTO listConditionDTO, @RequestParam("user_name") String problemName, @RequestParam("role") String role) {
        listConditionDTO.setLike(recursiveDecode(listConditionDTO.getLike()));
        return dbService.getList(listConditionDTO, problemName, role);
    }

    @GetMapping("/list_search")
    public ResultVO<ProblemListVO> searchList(@ModelAttribute ListConditionDTO listConditionDTO, @RequestParam("user_name") String problemName, @RequestParam("role") String role) {
        listConditionDTO.setLike(recursiveDecode(listConditionDTO.getLike()));
        return dbService.searchList(listConditionDTO, problemName, role);
    }
    @GetMapping("/list_record/self")
    public ResultVO<SubmitRecordListVO> submitRecordListSelf(@RequestParam("user_name") String problemName, @RequestParam("start") int start, @RequestParam("limit") int limit) {
        return dbService.submitRecordListByName(problemName, start, limit);
    }

    @GetMapping("/list_record/any")
    public ResultVO<SubmitRecordListVO> submitRecordListAny(@RequestParam("user_name") String problemName, @RequestParam("start") int start, @RequestParam("limit") int limit) {
        return dbService.submitRecordListByName(problemName, start, limit);
    }

    @GetMapping("/list_record/all")
    public ResultVO<SubmitRecordListVO> submitRecordListAll(@RequestParam("start") int start, @RequestParam("limit") int limit) {
        return dbService.submitRecordListAll(start, limit);
    }

    // base64多次编码问题 辅助解析方法
    private String recursiveDecode(String s) {
        if (s == null) return null;
        String tmp;
        try {
            while (!(tmp = URLDecoder.decode(s, StandardCharsets.UTF_8)).equals(s)) s = tmp;
        } catch (Exception ignore) {}
        return s;
    }
}
