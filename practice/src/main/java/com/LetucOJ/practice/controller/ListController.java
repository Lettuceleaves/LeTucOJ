package com.LetucOJ.practice.controller;

import com.LetucOJ.practice.model.ListServiceDTO;
import com.LetucOJ.practice.model.ResultVO;
import com.LetucOJ.practice.service.DBService;
import com.LetucOJ.practice.service.PracticeService;
import io.swagger.annotations.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/practice")
public class ListController {

    @Autowired
    private PracticeService practiceService;

    @Autowired
    private DBService dbService;

    @GetMapping("/list")
    public ResultVO getList(@ModelAttribute ListServiceDTO sql, @RequestParam("pname") String pname) throws Exception {
        sql.setLike(recursiveDecode(sql.getLike()));
        return dbService.getList(sql, pname);
    }

    @GetMapping("/listRoot")
    public ResultVO getListInRoot(@ModelAttribute ListServiceDTO sql, @RequestParam("pname") String pname) throws Exception {
        sql.setLike(recursiveDecode(sql.getLike()));
        return dbService.getListInRoot(sql, pname);
    }

    @GetMapping("/searchList")
    public ResultVO searchList(@ModelAttribute ListServiceDTO sql, @RequestParam("pname") String pname) throws Exception {
        sql.setLike(recursiveDecode(sql.getLike()));
        return dbService.searchList(sql, pname);
    }

    @GetMapping("/searchListInRoot")
    public ResultVO searchListInRoot(@ModelAttribute ListServiceDTO sql, @RequestParam("pname") String pname) throws Exception {
        sql.setLike(recursiveDecode(sql.getLike()));
        return dbService.searchListInRoot(sql, pname);
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

    private String recursiveDecode(String s) {
        if (s == null) return null;
        String tmp;
        try {
            while (!(tmp = URLDecoder.decode(s, StandardCharsets.UTF_8)).equals(s)) s = tmp;
        } catch (Exception ignore) {}
        return s;
    }


}
