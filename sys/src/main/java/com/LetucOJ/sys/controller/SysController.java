package com.LetucOJ.sys.controller;

import com.LetucOJ.sys.model.ResultVO;
import com.LetucOJ.sys.service.Monitor;
import com.LetucOJ.sys.service.SysService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sys")
public class SysController {

    @Autowired
    private SysService sysService;

    @Autowired
    private Monitor monitor;

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

    @PostMapping("/run")
    public ResultVO run(@RequestBody List<String> files, @RequestParam String lang) {
        return monitor.submit(files, lang);
    }
    @GetMapping("/refresh/sql")
    public ResultVO refreshSql() {
        return sysService.refreshSql();
    }
}