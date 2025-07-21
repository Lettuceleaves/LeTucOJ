package com.LetucOJ.auth.controller;

import com.LetucOJ.auth.model.RegisterRequestDTO;
import com.LetucOJ.auth.model.ResultVO;
import com.LetucOJ.auth.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResultVO register(@RequestBody RegisterRequestDTO registerRequestDTO) {
        return userService.register(registerRequestDTO);
    }

    @PostMapping("/login")
    public ResultVO login(@RequestBody RegisterRequestDTO registerRequestDTO) {
        return userService.authenticate(registerRequestDTO);
    }
}
