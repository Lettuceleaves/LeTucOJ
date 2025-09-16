package com.LetucOJ.sys.service;

import com.LetucOJ.sys.model.RegisterRequestDTO;
import com.LetucOJ.sys.model.ResultVO;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
    ResultVO register(RegisterRequestDTO registerRequestDTO);
    ResultVO authenticate(RegisterRequestDTO registerRequestDTO);
    ResultVO activateAccount(String userName);
    ResultVO deactivateAccount(String userName);
    ResultVO logout(String token, long ttl);
    ResultVO changePassword(String userName, String oldPassword, String newPassword);
    ResultVO getAllUsers();
    ResultVO getAllManagers();
    ResultVO promoteToManager(String userName);
    ResultVO demoteToUser(String userName);
    ResultVO getUserRankings();
}