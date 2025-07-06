package com.LetucOJ.auth.controller;

import com.LetucOJ.auth.model.user;
import com.LetucOJ.auth.service.authService;
import com.LetucOJ.auth.tool.jwt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class authCtrller {
    @Autowired
    private authService authService;

    @Autowired
    private jwt jwt;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestParam String username, @RequestParam String password) {
        authService.register(username, password);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam String username, @RequestParam String password) {
        user user = authService.authenticate(username, password);
        if (user != null) {
            String token = jwt.generateToken(user.getUserName(), user.getRole());
            return ResponseEntity.ok(token);
        } else {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }
}
