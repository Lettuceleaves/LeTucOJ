package com.LetucOJ.user.controller;

import com.LetucOJ.common.result.ResultVO;
import com.LetucOJ.user.model.JwtInfoVO;
import com.LetucOJ.user.model.RegisterRequestDTO;
import com.LetucOJ.user.model.UserInfoDTO;
import com.LetucOJ.user.model.UserManagerDTO;
import com.LetucOJ.user.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@AllArgsConstructor
public class UserController {

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
    public ResultVO<Void> activate(@RequestParam("user_name") String userName) {
        return userService.activateAccount(userName);
    }

    @PutMapping("/deactivate")
    public ResultVO<Void> deactivate(@RequestParam("user_name") String userName) {
        return userService.deactivateAccount(userName);
    }

    @PostMapping("/logout")
    public ResultVO<Void> logout(@RequestParam("user_name") String userName) { return userService.logout(userName); }

    @GetMapping("/users")
    public ResultVO<List<UserManagerDTO>> listUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/managers")
    public ResultVO<List<UserManagerDTO>> listManagers() {
        return userService.getAllManagers();
    }

    @PutMapping("/promote")
    public ResultVO<Void> promote(@RequestParam("user_name") String userName) {
        return userService.promoteToManager(userName);
    }

    @PutMapping("/demote")
    public ResultVO<Void> demote(@RequestParam("user_name") String userName) {
        return userService.demoteToUser(userName);
    }

    @GetMapping("/rank")
    public ResultVO<Object> getRankings() {
        return userService.getUserRankings();
    }

    @GetMapping("/refresh")
    public ResultVO<JwtInfoVO> refreshToken(@RequestParam("user_name") String userName) {
        return userService.refreshToken(userName);
    }

    @PutMapping("/info")
    public ResultVO<Void> updateUserInfo(@RequestBody UserInfoDTO dto) { return userService.updateUserFullInfo(dto); }

    @GetMapping("/info")
    public ResultVO<UserInfoDTO> getUserInfo(@RequestParam("user_name") String userName) { return userService.getUserFullInfo(userName); }

    @GetMapping("/background")
    public ResultVO<byte[]> getBackground(@RequestParam("user_name") String userName) { return userService.getBackground(userName); }

    @PutMapping("/background")
    public ResultVO<Void> updateBackground(@RequestParam("user_name") String userName, 
                                           @RequestBody byte[] data) { return userService.updateBackground(userName, data); }

    @GetMapping("/headPortrait")
    public ResultVO<byte[]> getHeadPortrait(@RequestParam("user_name") String userName) { return userService.getHeadPortrait(userName); }

    @PutMapping("/headPortrait")
    public ResultVO<Void> updateHeadPortrait(@RequestParam("user_name") String userName, 
                                             @RequestBody byte[] data) { return userService.updateHeadPortrait(userName, data); }

    @GetMapping("/heatmap")
    public ResultVO<byte[]> getHeatmap(@RequestParam("user_name") String userName, 
                                       @RequestParam("year") int year) { return userService.getHeatmap(userName, year); }

    @GetMapping("/secret_key")
    public ResultVO<String> getSecretKey(@RequestParam("user_name") String userName) {
        return userService.getSecretKey(userName);
    }

    @PutMapping("/password")
    public ResultVO<Void> changePassword(@RequestParam("userName") String userName, 
                                         @RequestParam("secret_key" ) String secretKey, 
                                         @RequestParam("new_password") String newPassword) {
        return userService.changePassword(userName, secretKey, newPassword);
    }
}