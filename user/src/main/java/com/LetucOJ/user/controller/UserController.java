package com.LetucOJ.user.controller;

import com.LetucOJ.common.result.ResultVO;
import com.LetucOJ.user.model.JwtInfoVO;
import com.LetucOJ.user.model.RegisterRequestDTO;
import com.LetucOJ.user.model.UserInfoDTO;
import com.LetucOJ.user.model.UserManagerDTO;
import com.LetucOJ.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResultVO<Void> register(@RequestBody RegisterRequestDTO dto) {
        return userService.register(dto);
    }

    @PostMapping("/login")
    public ResultVO<JwtInfoVO> login(@RequestBody RegisterRequestDTO dto) {
        return userService.login(dto);
    }

    @PutMapping("/activate")
    public ResultVO<Void> activate(@RequestParam("pname") String pname) {
        return userService.activateAccount(pname);
    }

    @PutMapping("/deactivate")
    public ResultVO<Void> deactivate(@RequestParam("pname") String pname) {
        return userService.deactivateAccount(pname);
    }

    @PostMapping("/logout")
    public ResultVO<Void> logout(@RequestParam("pname") String pname) { return userService.logout(pname); }

    @GetMapping("/users")
    public ResultVO<List<UserManagerDTO>> listUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/managers")
    public ResultVO<List<UserManagerDTO>> listManagers() {
        return userService.getAllManagers();
    }

    @PutMapping("/promote")
    public ResultVO<Void> promote(@RequestParam("pname") String pname) {
        return userService.promoteToManager(pname);
    }

    @PutMapping("/demote")
    public ResultVO<Void> demote(@RequestParam("pname") String pname) {
        return userService.demoteToUser(pname);
    }

    @GetMapping("/rank")
    public ResultVO<Object> getRankings() {
        return userService.getUserRankings();
    }

    @GetMapping("/refresh")
    public ResultVO<JwtInfoVO> refreshToken(@RequestParam("pname") String pname) {
        return userService.refreshToken(pname);
    }

    @PutMapping("/info")
    public ResultVO<Void> updateUserInfo(@RequestBody UserInfoDTO dto) { return userService.updateUserFullInfo(dto); }

    @GetMapping("/info")
    public ResultVO<UserInfoDTO> getUserInfo(@RequestParam("pname") String pname) { return userService.getUserFullInfo(pname); }

    @GetMapping("/background")
    public ResultVO<byte[]> getBackground(@RequestParam("pname") String pname) { return userService.getBackground(pname); }

    @PutMapping("/background")
    public ResultVO<Void> updateBackground(@RequestParam("pname") String pname, @RequestBody byte[] data) { return userService.updateBackground(pname, data); }

    @GetMapping("/headPortrait")
    public ResultVO<byte[]> getHeadPortrait(@RequestParam("pname") String pname) { return userService.getHeadPortrait(pname); }

    @PutMapping("/headPortrait")
    public ResultVO<Void> updateHeadPortrait(@RequestParam("pname") String pname, @RequestBody byte[] data) { return userService.updateHeadPortrait(pname, data); }

    @GetMapping("/heatmap")
    public ResultVO<byte[]> getHeatmap(@RequestParam("pname") String pname, @RequestParam("year") int year) { return userService.getHeatmap(pname, year); }

    @GetMapping("/secret_key")
    public ResultVO<String> getSecretKey(@RequestParam("pname") String pname) {
        return userService.getSecretKey(pname);
    }

    @PutMapping("/password")
    public ResultVO<Void> changePassword(@RequestParam("pname") String pname, @RequestParam("secret_key" ) String secretKey, @RequestParam("new_password") String newPassword) {
        return userService.changePassword(pname, secretKey, newPassword);
    }
}