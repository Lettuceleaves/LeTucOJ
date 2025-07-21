package com.LetucOJ.auth.service.impl;

import com.LetucOJ.auth.model.JwtInfoVO;
import com.LetucOJ.auth.model.RegisterRequestDTO;
import com.LetucOJ.auth.model.ResultVO;
import com.LetucOJ.auth.model.UserDTO;
import com.LetucOJ.auth.repos.UserMybatisRepos;
import com.LetucOJ.auth.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMybatisRepos userMybatisRepos;

    public ResultVO register(RegisterRequestDTO registerRequestDTO) {
        String username = registerRequestDTO.getUsername();
        String password = registerRequestDTO.getPassword();
        UserDTO userDTO = new UserDTO(username, password);
        Integer result = userMybatisRepos.saveUserInfo(userDTO);
        if (result != 1) {
            return new ResultVO(1, null, "User registration failed");
        } else {
            return new ResultVO(0, new JwtInfoVO(username, "USER", System.currentTimeMillis()), null);
        }
    }

    public ResultVO authenticate(RegisterRequestDTO registerRequestDTO) {
        String username = registerRequestDTO.getUsername();
        String password = registerRequestDTO.getPassword();
        UserDTO userDTO = userMybatisRepos.getPasswordByUserName(username);
        if (userDTO != null && userDTO.getPassword().equals(password)) {
            return new ResultVO(0, new JwtInfoVO(username, userDTO.getRole(), System.currentTimeMillis()), null);
        } else {
            return new ResultVO(1, null, "Invalid credentials");
        }
    }
}
