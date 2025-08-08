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
        try {
            Integer result = userMybatisRepos.saveUserInfo(userDTO);
            if (!result.equals(1)) {
                return new ResultVO(1, null, "User registration failed");
            }
            return new ResultVO(0, null, null);
        } catch (Exception e) {
            return new ResultVO(1, null, "User registration failed: " + e.getMessage());
        }
    }

    @Override
    public ResultVO authenticate(RegisterRequestDTO dto) {
        String username = dto.getUsername();
        String rawPwd = dto.getPassword();
        try {
            UserDTO userDTO = userMybatisRepos.getPasswordByUserName(username);
            if (userDTO != null && userDTO.isEnabled() && PasswordUtil.matches(rawPwd, userDTO.getPassword())) {
                String cnname = userDTO.getCnname();
                System.out.println(cnname);
                return new ResultVO(0, new JwtInfoVO(username, cnname, userDTO.getRole(), System.currentTimeMillis()), null);
            }
            return new ResultVO(1, null, "Invalid credentials or account disabled" +
                    (userDTO == null ? " - User not found" : (userDTO.isEnabled() ? "" : " - Account is disabled")));
        } catch (Exception e) {
            return new ResultVO(1, null, "Authentication failed: " + e.getMessage());
        }
    }

    @Override
    public ResultVO activateAccount(String userName) {
        try {
            Integer rows = userMybatisRepos.activateUser(userName);
            return rows != null && rows == 1
                    ? new ResultVO(0, null, null)
                    : new ResultVO(1, null, "Activation failed");
        } catch (Exception e) {
            return new ResultVO(1, null, "Activation failed: " + e.getMessage());
        }
    }

    @Override
    public ResultVO deactivateAccount(String userName) {
        try {
            Integer rows = userMybatisRepos.deactivateUser(userName);
            return rows != null && rows == 1
                    ? new ResultVO(0, null, null)
                    : new ResultVO(1, null, "Deactivation failed");
        } catch (Exception e) {
            return new ResultVO(1, null, "Deactivation failed: " + e.getMessage());
        }
    }

    @Override
    public ResultVO logout(String token, long ttl) {
        try {
            if (ttl > 0) {
                redisTemplate.opsForValue().set(BLACKLIST_PREFIX + token, "1", Duration.ofSeconds(ttl));
            }
            return new ResultVO(0, null, null);
        } catch (Exception e) {
            return new ResultVO(1, null, "Logout failed: " + e.getMessage());
        }
    }

    @Override
    public ResultVO changePassword(String userName, String oldPassword, String newPassword) {
        try {
            UserDTO userDTO = userMybatisRepos.getPasswordByUserName(userName);
            if (userDTO == null || !PasswordUtil.matches(oldPassword, userDTO.getPassword())) {
                return new ResultVO(1, null, "Old password incorrect");
            }
            String encodedNew = PasswordUtil.encrypt(newPassword);
            Integer rows = userMybatisRepos.updatePasswordAndLock(userName, encodedNew);
            return rows != null && rows == 1
                    ? new ResultVO(0, null, null)
                    : new ResultVO(1, null, "Password change failed");
        } catch (Exception e) {
            return new ResultVO(1, null, "Password change failed: " + e.getMessage());
        }
    }

    @Override
    public ResultVO getAllUsers() {
        try {
            List<UserDTO> list = userMybatisRepos.getUsersByRole("USER");
            for (UserDTO user : list) {
                user.setPassword(null); // Clear password for security
            }
            return new ResultVO(0, list, null);
        } catch (Exception e) {
            return new ResultVO(1, null, "Failed to retrieve users: " + e.getMessage());
        }
    }

    @Override
    public ResultVO getAllManagers() {
        try {
            List<UserDTO> list = userMybatisRepos.getUsersByRole("MANAGER");
            for (UserDTO user : list) {
                user.setPassword(null); // Clear password for security
            }
            return new ResultVO(0, list, null);
        } catch (Exception e) {
            return new ResultVO(1, null, "Failed to retrieve managers: " + e.getMessage());
        }
    }

    @Override
    public ResultVO promoteToManager(String userName) {
        try {
            Integer rows = userMybatisRepos.setUserToManager(userName);
            return rows != null && rows == 1
                    ? new ResultVO(0, null, null)
                    : new ResultVO(1, null, "Promotion failed");
        } catch (Exception e) {
            return new ResultVO(1, null, "Promotion failed: " + e.getMessage());
        }
    }

    @Override
    public ResultVO demoteToUser(String userName) {
        try {
            Integer rows = userMybatisRepos.setManagerToUser(userName);
            return rows != null && rows == 1
                    ? new ResultVO(0, null, null)
                    : new ResultVO(1, null, "Demotion failed");
        } catch (Exception e) {
            return new ResultVO(1, null, "Demotion failed: " + e.getMessage());
        }
    }
}
