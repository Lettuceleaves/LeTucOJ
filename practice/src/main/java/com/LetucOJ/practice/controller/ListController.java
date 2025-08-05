package com.LetucOJ.practice.controller;

import com.LetucOJ.practice.model.ListServiceDTO;
import com.LetucOJ.practice.model.ResultVO;
import com.LetucOJ.practice.service.DBService;
import com.LetucOJ.practice.service.PracticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/practice")
public class ListController {

    @Autowired
    private PracticeService practiceService;

    @Autowired
    private DBService dbService;

    @GetMapping("/list")
    public ResultVO getList(@RequestBody ListServiceDTO sql) throws Exception {
        return dbService.getList(sql);
    }

    @GetMapping("/listRoot")
    public ResultVO getListInRoot(@RequestBody ListServiceDTO sql) throws Exception {
        return dbService.getListInRoot(sql);
    }

    @GetMapping("/searchList")
    public ResultVO searchList(@RequestBody ListServiceDTO sql) throws Exception {
        return dbService.searchList(sql);
    }

    @GetMapping("/searchListInRoot")
    public ResultVO searchListInRoot(@RequestBody ListServiceDTO sql) throws Exception {
        return dbService.searchListInRoot(sql);
    }

    @GetMapping("/recordList/self")
    public ResultVO recordListSelf(@RequestParam("cnname") String cnname) throws Exception {
        return dbService.recordListByName(cnname);
    }

    @GetMapping("/recordList/any")
    public ResultVO recordListAny(@RequestParam("cnname") String cnname) throws Exception {
        return dbService.recordListByName(cnname);
    }

    @GetMapping("/recordList/all")
    public ResultVO recordListAll() throws Exception {
        return dbService.recordListAll();
    }

}
