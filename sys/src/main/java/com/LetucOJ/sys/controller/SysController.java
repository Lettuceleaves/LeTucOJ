package com.LetucOJ.sys.controller;

import com.LetucOJ.common.result.ResultVO;
import com.LetucOJ.sys.model.Log;
import com.LetucOJ.sys.model.LogListVO;
import com.LetucOJ.sys.service.SysService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sys")
@AllArgsConstructor
public class SysController {

    private SysService sysService;

    @GetMapping("/log/list")
    public ResultVO<LogListVO> logList() {
        return null;
    }

    @PutMapping("/doc/update")
    public ResultVO<Void> updateDoc(@RequestBody byte[] doc) {
        return sysService.updateDoc(doc);
    }

    @GetMapping("/doc/get")
    public ResultVO<byte[]> getDoc() {
        return sysService.getDoc();
    }

    @GetMapping("/refresh/sql")
    public ResultVO<Void> refreshSql() {
        return sysService.refreshSql();
    }
}