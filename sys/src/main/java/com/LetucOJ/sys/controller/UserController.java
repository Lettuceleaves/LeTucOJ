package com.LetucOJ.sys.controller;

import com.LetucOJ.sys.model.ResultVO;
import com.LetucOJ.sys.service.SysService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sys")
public class UserController {

    @Autowired
    private SysService sysService;

    @GetMapping("/log/list")
    public ResultVO logList() {
        return null;
    }

    @GetMapping("/doc/get")
    public ResultVO updateDoc(@RequestBody byte[] doc) {
        return sysService.updateDoc(doc);
    }

    @PutMapping("/doc/update")
    public ResultVO getDoc() {
        return sysService.getDoc();
    }
}