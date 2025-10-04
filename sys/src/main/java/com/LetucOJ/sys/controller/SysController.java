package com.LetucOJ.sys.controller;

import com.LetucOJ.common.result.ResultVO;
import com.LetucOJ.sys.service.SysService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sys")
public class SysController {

    @Autowired
    private SysService sysService;

    @GetMapping("/log/list")
    public ResultVO logList() {
        return null;
    }

    @PutMapping("/doc/update")
    public ResultVO updateDoc(@RequestBody byte[] doc) {
        return sysService.updateDoc(doc);
    }

    @GetMapping("/doc/get")
    public ResultVO getDoc() {
        return sysService.getDoc();
    }

    @GetMapping("/refresh/sql")
    public ResultVO refreshSql() {
        return sysService.refreshSql();
    }
}