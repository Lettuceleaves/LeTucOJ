package com.LetucOJ.sys.controller;

import com.LetucOJ.common.result.ResultVO;
import com.LetucOJ.sys.model.RegisterRequestDTO;
import com.LetucOJ.sys.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResultVO register(@RequestBody RegisterRequestDTO dto) {
        return userService.register(dto);
    }

    @PostMapping("/login")
    public ResultVO login(@RequestBody RegisterRequestDTO dto) {
        return userService.authenticate(dto);
    }

    @PutMapping("/activate")
    public ResultVO activate(@RequestParam("pname") String pname) {
        return userService.activateAccount(pname);
    }

    @PutMapping("/deactivate")
    public ResultVO deactivate(@RequestParam("pname") String pname) {
        return userService.deactivateAccount(pname);
    }

    @PostMapping("/logout")
    public ResultVO logout(@RequestHeader("Authorization") String authHeader,
                           @RequestParam("ttl") long ttl) {
        String token = authHeader.replace("Bearer ", "");
        return userService.logout(token, ttl);
    }

    @PutMapping("/change-password")
    public ResultVO changePassword(@RequestParam("pname") String pname,
                                   @RequestParam String oldPassword,
                                   @RequestParam String newPassword) {
        return userService.changePassword(pname, oldPassword, newPassword);
    }

    @GetMapping("/users")
    public ResultVO listUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/managers")
    public ResultVO listManagers() {
        return userService.getAllManagers();
    }

    @PutMapping("/promote")
    public ResultVO promote(@RequestParam("pname") String pname) {
        return userService.promoteToManager(pname);
    }

    @PutMapping("/demote")
    public ResultVO demote(@RequestParam("pname") String pname) {
        return userService.demoteToUser(pname);
    }

    @GetMapping("/rank")
    public ResultVO getRankings() {
        return userService.getUserRankings();
    }
}