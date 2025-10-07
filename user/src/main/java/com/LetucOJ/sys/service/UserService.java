package com.LetucOJ.sys.service;

import com.LetucOJ.common.result.ResultVO;
import com.LetucOJ.sys.model.RegisterRequestDTO;
import com.LetucOJ.sys.model.UserInfoDTO;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
    ResultVO register(RegisterRequestDTO registerRequestDTO);
    ResultVO login(RegisterRequestDTO registerRequestDTO);
    ResultVO refreshToken(String userName);
    ResultVO activateAccount(String userName);
    ResultVO deactivateAccount(String userName);
    ResultVO logout(String username);
    ResultVO getAllUsers();
    ResultVO getAllManagers();
    ResultVO promoteToManager(String userName);
    ResultVO demoteToUser(String userName);
    ResultVO getUserRankings();
    ResultVO updateUserFullInfo(UserInfoDTO userInfoDTO);
    ResultVO getBackground(String username);
    ResultVO getUserFullInfo(String username);
    ResultVO updateBackground(String username, byte[] data);
    ResultVO getHeadPortrait(String username);
    ResultVO updateHeadPortrait(String username, byte[] data);
    ResultVO getHeatmap(String username, int year);
}