package com.LetucOJ.sys.controller;

import com.LetucOJ.sys.model.RegisterRequestDTO;
import com.LetucOJ.sys.model.ResultVO;
import com.LetucOJ.sys.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;
//
//    @Autowired
//    private ReactiveStringRedisTemplate redisTemplate;

    @PostMapping("/register")
    public ResultVO register(@RequestBody RegisterRequestDTO dto) {
        return userService.register(dto);
    }

    @PostMapping("/login")
    public ResultVO login(@RequestBody RegisterRequestDTO dto) {
        return userService.authenticate(dto);
    }

    @PutMapping("/activate")
    public ResultVO activate(@RequestParam String username) {
        return userService.activateAccount(username);
    }

    @PutMapping("/deactivate")
    public ResultVO deactivate(@RequestParam String username) {
        return userService.deactivateAccount(username);
    }

    @PostMapping("/logout")
    public ResultVO logout(@RequestHeader("Authorization") String authHeader,
                           @RequestParam("ttl") long ttl) {
        String token = authHeader.replace("Bearer ", "");
        return userService.logout(token, ttl);
    }

    @PutMapping("/change-password")
    public ResultVO changePassword(@RequestParam String username,
                                   @RequestParam String oldPassword,
                                   @RequestParam String newPassword) {
        return userService.changePassword(username, oldPassword, newPassword);
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
    public ResultVO promote(@RequestParam String username) {
        return userService.promoteToManager(username);
    }

    @PutMapping("/demote")
    public ResultVO demote(@RequestParam String username) {
        return userService.demoteToUser(username);
    }

//    /** 测试接口：写入一条写死的 Redis 数据 */
//    @PostMapping("/test/redis/write")
//    public ResultVO testRedisWrite() {
//        redisTemplate.opsForValue().set("testKey", "testValue", Duration.ofMinutes(5)).subscribe();
//        return new ResultVO(0, null, null);
//    }
//
//    /** 测试接口：删除上述 Redis 数据 */
//    @PostMapping("/test/redis/delete")
//    public ResultVO testRedisDelete() {
//        redisTemplate.delete("testKey").subscribe();
//        return new ResultVO(0, null, null);
//    }
}