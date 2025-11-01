package com.LetucOJ.user.controller;

import com.LetucOJ.common.result.Result;
import com.LetucOJ.common.result.ResultVO;
import com.LetucOJ.common.unique.TaskIdContext;
import com.LetucOJ.user.model.RegisterRequestDTO;
import com.LetucOJ.user.model.UserInfoDTO;
import com.LetucOJ.user.service.UserService;
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
        return userService.login(dto);
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
    public ResultVO logout(@RequestParam("pname") String pname) { return userService.logout(pname); }

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

    @GetMapping("/refresh")
    public ResultVO refreshToken(@RequestParam("pname") String pname) {
        return userService.refreshToken(pname);
    }

    @PutMapping("/info/update")
    public ResultVO updateUserInfo(@RequestBody UserInfoDTO dto) { return userService.updateUserFullInfo(dto); }

    @GetMapping("/info/get")
    public ResultVO getUserInfo(@RequestParam("pname") String pname) { return userService.getUserFullInfo(pname); }

    @GetMapping("/background/get")
    public ResultVO getBackground(@RequestParam("pname") String pname) { return userService.getBackground(pname); }

    @PutMapping("/background/update")
    public ResultVO updateBackground(@RequestParam("pname") String pname, @RequestBody byte[] data) { return userService.updateBackground(pname, data); }

    @GetMapping("/headPortrait/get")
    public ResultVO getHeadPortrait(@RequestParam("pname") String pname) { return userService.getHeadPortrait(pname); }

    @PutMapping("/headPortrait/update")
    public ResultVO updateHeadPortrait(@RequestParam("pname") String pname, @RequestBody byte[] data) { return userService.updateHeadPortrait(pname, data); }

    @GetMapping("/heatmap")
    public ResultVO getHeatmap(@RequestParam("pname") String pname, @RequestParam("year") int year) { return userService.getHeatmap(pname, year); }

    @GetMapping("/secret_key")
    public ResultVO getSecretKey(@RequestParam("pname") String pname) {
        return userService.getSecretKey(pname);
    }

    @PostMapping("/change_password")
    public ResultVO changePassword(@RequestParam("pname") String pname, @RequestParam("secret_key" ) String secretKey, @RequestParam("new_password") String newPassword) {
        return userService.changePassword(pname, secretKey, newPassword);
    }

    @PostMapping("/test")
    public ResultVO testEndpoint() {
        return Result.success(TaskIdContext.getTaskId());
    }
}