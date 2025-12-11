package com.LetucOJ.sys.controller;

import com.LetucOJ.common.result.ResultVO;
import com.LetucOJ.sys.model.LogListVO;
import com.LetucOJ.sys.service.SysService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sys")
@AllArgsConstructor
public class SysController {

    private SysService sysService;

    @GetMapping("/log/list")
    public ResultVO<LogListVO> logList() {
        return null;
    }

    @PutMapping("/doc")
    public ResultVO<Void> updateDoc(@RequestBody byte[] doc) {
        return sysService.updateDoc(doc);
    }

    @GetMapping("/doc")
    public ResultVO<byte[]> getDoc() {
        return sysService.getDoc();
    }

    @GetMapping("/mysqldump")
    public ResultVO<Void> refreshSql() {
        return sysService.refreshSql();
    }
}