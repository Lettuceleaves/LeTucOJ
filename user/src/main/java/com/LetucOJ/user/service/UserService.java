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
    ResultVO<Void> logout(String userName);
    ResultVO<List<UserManagerDTO>> getAllUsers();
    ResultVO<List<UserManagerDTO>> getAllManagers();
    ResultVO<Void> promoteToManager(String userName);
    ResultVO<Void> demoteToUser(String userName);
    ResultVO<Object> getUserRankings();
    ResultVO<Void> updateUserFullInfo(UserInfoDTO userInfoDTO);
    ResultVO<byte[]> getBackground(String userName);
    ResultVO<UserInfoDTO> getUserFullInfo(String userName);
    ResultVO<Void> updateBackground(String userName, byte[] data);
    ResultVO<byte[]> getHeadPortrait(String userName);
    ResultVO<Void> updateHeadPortrait(String userName, byte[] data);
    ResultVO<byte[]> getHeatmap(String userName, int year);
    ResultVO<String> getSecretKey(String userName);
    ResultVO<Void> changePassword(String userName, String secretKey, String newPassword);
}