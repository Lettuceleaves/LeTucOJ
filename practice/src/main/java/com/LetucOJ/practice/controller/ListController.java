package com.LetucOJ.practice.controller;

import com.LetucOJ.practice.model.ListServiceDTO;
import com.LetucOJ.practice.model.ResultVO;
import com.LetucOJ.practice.service.DBService;
import com.LetucOJ.practice.service.PracticeService;
import io.swagger.annotations.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/practice")
public class ListController {

    @Autowired
    private PracticeService practiceService;

    @Autowired
    private DBService dbService;

    @GetMapping("/count")
    public ResultVO getCount() throws Exception {
        return dbService.getAmount();
    }

    @GetMapping("/list")
    public ResultVO getList(@ModelAttribute ListServiceDTO sql) throws Exception {
        return dbService.getList(sql);
    }

    @GetMapping("/listRoot")
    public ResultVO getListInRoot(@ModelAttribute ListServiceDTO sql) throws Exception {
        return dbService.getListInRoot(sql);
    }

    @GetMapping("/searchList")
    public ResultVO searchList(@ModelAttribute ListServiceDTO sql) throws Exception {
        return dbService.searchList(sql);
    }

    @GetMapping("/searchListInRoot")
    public ResultVO searchListInRoot(@ModelAttribute ListServiceDTO sql) throws Exception {
        return dbService.searchListInRoot(sql);
    }

    @GetMapping("/recordList/self")
    public ResultVO recordListSelf(@RequestParam("pname") String pname) throws Exception {
        return dbService.recordListByName(pname);
    }

    @GetMapping("/recordList/any")
    public ResultVO recordListAny(@RequestParam("pname") String pname) throws Exception {
        return dbService.recordListByName(pname);
    }

    @GetMapping("/recordList/all")
    public ResultVO recordListAll() throws Exception {
        return dbService.recordListAll();
    }

}
