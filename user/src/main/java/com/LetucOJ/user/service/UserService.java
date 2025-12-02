package com.LetucOJ.user.service;

import com.LetucOJ.common.result.ResultVO;
import com.LetucOJ.user.model.JwtInfoVO;
import com.LetucOJ.user.model.RegisterRequestDTO;
import com.LetucOJ.user.model.UserInfoDTO;
import com.LetucOJ.user.model.UserManagerDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserService {
    ResultVO<Void> register(RegisterRequestDTO registerRequestDTO);
    ResultVO<JwtInfoVO> login(RegisterRequestDTO registerRequestDTO);
    ResultVO<JwtInfoVO> refreshToken(String userName);
    ResultVO<Void> activateAccount(String userName);
    ResultVO<Void> deactivateAccount(String userName);
    ResultVO<Void> logout(String username);
    ResultVO<List<UserManagerDTO>> getAllUsers();
    ResultVO<List<UserManagerDTO>> getAllManagers();
    ResultVO<Void> promoteToManager(String userName);
    ResultVO<Void> demoteToUser(String userName);
    ResultVO<Object> getUserRankings();
    ResultVO<Void> updateUserFullInfo(UserInfoDTO userInfoDTO);
    ResultVO<byte[]> getBackground(String username);
    ResultVO<UserInfoDTO> getUserFullInfo(String username);
    ResultVO<Void> updateBackground(String username, byte[] data);
    ResultVO<byte[]> getHeadPortrait(String username);
    ResultVO<Void> updateHeadPortrait(String username, byte[] data);
    ResultVO<byte[]> getHeatmap(String username, int year);
    ResultVO<String> getSecretKey(String username);
    ResultVO<Void> changePassword(String username, String secretKey, String newPassword);
}