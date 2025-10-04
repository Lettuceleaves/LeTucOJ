package com.LetucOJ.sys.service;

import com.LetucOJ.common.result.ResultVO;
import com.LetucOJ.sys.model.RegisterRequestDTO;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
    ResultVO register(RegisterRequestDTO registerRequestDTO);
    ResultVO login(RegisterRequestDTO registerRequestDTO);
    ResultVO refreshToken(String userName);
    ResultVO activateAccount(String userName);
    ResultVO deactivateAccount(String userName);
    ResultVO logout(String token, long ttl);
    ResultVO getAllUsers();
    ResultVO getAllManagers();
    ResultVO promoteToManager(String userName);
    ResultVO demoteToUser(String userName);
    ResultVO getUserRankings();
}