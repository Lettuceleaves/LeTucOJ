package com.LetucOJ.sys.service.impl;

import com.LetucOJ.sys.model.JwtInfoVO;
import com.LetucOJ.sys.model.RegisterRequestDTO;
import com.LetucOJ.sys.model.ResultVO;
import com.LetucOJ.sys.model.UserDTO;
import com.LetucOJ.sys.repos.UserMybatisRepos;
import com.LetucOJ.sys.service.UserService;
import com.LetucOJ.sys.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMybatisRepos userMybatisRepos;

    @Autowired
    private ReactiveStringRedisTemplate redisTemplate;

    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";

    @Override
    public ResultVO register(RegisterRequestDTO dto) {
        String username = dto.getUsername();
        String cnname = dto.getCnname();
        String rawPwd = dto.getPassword();
        String encodedPwd = PasswordUtil.encrypt(rawPwd);
        UserDTO userDTO = new UserDTO(username, cnname, encodedPwd);
        userDTO.setRole("USER");
        userDTO.setEnabled(true);
        Integer result = userMybatisRepos.saveUserInfo(userDTO);
        if (!result.equals(1)) {
            return new ResultVO(1, null, "User registration failed");
        }
        return new ResultVO(0, null, null);
    }

    @Override
    public ResultVO authenticate(RegisterRequestDTO dto) {
        String username = dto.getUsername();
        String cnname = dto.getCnname();
        String rawPwd = dto.getPassword();
        UserDTO userDTO = userMybatisRepos.getPasswordByUserName(username);
        if (userDTO != null && userDTO.isEnabled() && PasswordUtil.matches(rawPwd, userDTO.getPassword())) {
            return new ResultVO(0, new JwtInfoVO(username, userDTO.getRole(), cnname, System.currentTimeMillis()), null);
        }
        return new ResultVO(1, null, "Invalid credentials or account disabled");
    }

    @Override
    public ResultVO activateAccount(String userName) {
        Integer rows = userMybatisRepos.activateUser(userName);
        return rows != null && rows == 1
                ? new ResultVO(0, null, null)
                : new ResultVO(1, null, "Activation failed");
    }

    @Override
    public ResultVO deactivateAccount(String userName) {
        Integer rows = userMybatisRepos.deactivateUser(userName);
        return rows != null && rows == 1
                ? new ResultVO(0, null, null)
                : new ResultVO(1, null, "Deactivation failed");
    }

    @Override
    public ResultVO logout(String token, long ttl) {
        if (ttl > 0) {
            redisTemplate.opsForValue().set(BLACKLIST_PREFIX + token, "1", Duration.ofSeconds(ttl));
        }
        return new ResultVO(0, null, null);
    }

    @Override
    public ResultVO changePassword(String userName, String oldPassword, String newPassword) {
        UserDTO userDTO = userMybatisRepos.getPasswordByUserName(userName);
        if (userDTO == null || !PasswordUtil.matches(oldPassword, userDTO.getPassword())) {
            return new ResultVO(1, null, "Old password incorrect");
        }
        String encodedNew = PasswordUtil.encrypt(newPassword);
        Integer rows = userMybatisRepos.updatePasswordAndLock(userName, encodedNew);
        return rows != null && rows == 1
                ? new ResultVO(0, null, null)
                : new ResultVO(1, null, "Password change failed");
    }

    @Override
    public ResultVO getAllUsers() {
        List<UserDTO> list = userMybatisRepos.getUsersByRole("USER");
        for (UserDTO user : list) {
            user.setPassword(null); // Clear password for security
        }
        return new ResultVO(0, list, null);
    }

    @Override
    public ResultVO getAllManagers() {
        List<UserDTO> list = userMybatisRepos.getUsersByRole("MANAGER");
        for (UserDTO user : list) {
            user.setPassword(null); // Clear password for security
        }
        return new ResultVO(0, list, null);
    }

    @Override
    public ResultVO promoteToManager(String userName) {
        Integer rows = userMybatisRepos.setUserToManager(userName);
        return rows != null && rows == 1
                ? new ResultVO(0, null, null)
                : new ResultVO(1, null, "Promotion failed");
    }

    @Override
    public ResultVO demoteToUser(String userName) {
        Integer rows = userMybatisRepos.setManagerToUser(userName);
        return rows != null && rows == 1
                ? new ResultVO(0, null, null)
                : new ResultVO(1, null, "Demotion failed");
    }
}
