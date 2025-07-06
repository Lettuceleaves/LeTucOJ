package com.LetucOJ.auth.service.impl;

import com.LetucOJ.auth.model.user;
import com.LetucOJ.auth.repos.userMybatisRepos;
import com.LetucOJ.auth.service.authService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class authServiceImpl implements authService {

    @Autowired
    private userMybatisRepos userMybatisRepos;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public user register(String username, String password) {
        user user = new user();
        user.setUserName(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("USER");
        if (userMybatisRepos.save(user) != 1) {
            throw new RuntimeException("User registration failed");
        } else {
            System.out.println("User registered successfully");
            return user;
        }
    }

    public user authenticate(String username, String password) {
        user user = userMybatisRepos.findByUsername(username);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            System.out.println(user.getRole());
            return user;
        } else {
            System.out.println("Invalid credentials");
            return null;
        }
    }
}
