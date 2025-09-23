package com.LetucOJ.sys.service.impl;

import com.LetucOJ.common.result.ResultVO;
import com.LetucOJ.sys.model.JwtInfoVO;
import com.LetucOJ.sys.model.RegisterRequestDTO;
import com.LetucOJ.sys.model.UserDTO;
import com.LetucOJ.sys.repos.UserMybatisRepos;
import com.LetucOJ.sys.service.UserService;
import com.LetucOJ.sys.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        UserDTO userDTO = new UserDTO(username, cnname, encodedPwd, "USER", false);
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
            if (list == null || list.isEmpty()) {
                return new ResultVO(1, null, "No users found");
            }
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
            if (list == null || list.isEmpty()) {
                return new ResultVO(1, null, "No managers found");
            }
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

    @Override
    public ResultVO getUserRankings() {
        /* ---------- 1. 取数据 ---------- */
        List<Map<String, Object>> rankings = userMybatisRepos.listCorrect();
        List<UserDTO> users = userMybatisRepos.getUsersByRole("USER");

        if (rankings == null || rankings.isEmpty()) {
            return new ResultVO(1, null, "No rankings found");
        }
        if (users == null || users.isEmpty()) {
            return new ResultVO(1, null, "No users found");
        }

        /* ---------- 2. 题目 -> 分数 ---------- */
        Map<String, Integer> scoreMap = userMybatisRepos.points()
                .stream()
                .collect(Collectors.toMap(
                        m -> m.get("name").toString().trim(),
                        m -> Integer.parseInt(m.get("difficulty").toString())));

        /* ---------- 3. 用户 -> UserDTO ---------- */
        Map<String, UserDTO> userMap = users.stream()
                .collect(Collectors.toMap(UserDTO::getUserName, Function.identity()));

        /* ---------- 4. 小顶堆：总分升序，同分则 userName 降序 ---------- */
        PriorityQueue<Map<String, Object>> heap = new PriorityQueue<>((a, b) -> {
            int sa = (int) a.get("totalScore");
            int sb = (int) b.get("totalScore");
            if (sa != sb) return Integer.compare(sa, sb);
            String ua = (String) a.get("userName");
            String ub = (String) b.get("userName");
            return ub.compareTo(ua);
        });

        /* ---------- 5. 同时统计题数 & 总分 ---------- */
        Map<String, Integer> userCount  = new HashMap<>();   // 用户 -> 题数
        Map<String, Integer> userScore  = new HashMap<>();   // 用户 -> 总分
        for (Map<String, Object> rec : rankings) {
            String userName = Optional.ofNullable(
                            rec.get("userName") != null ? rec.get("userName") : rec.get("user_name"))
                    .map(Object::toString).map(String::trim).orElse(null);
            String problemName = Optional.ofNullable(rec.get("name"))
                    .map(Object::toString).map(String::trim).orElse(null);
            if (userName == null || problemName == null) continue;

            int score = scoreMap.getOrDefault(problemName, 0);
            userCount.merge(userName, 1, Integer::sum);        // 题数 +1
            userScore.merge(userName, score, Integer::sum);    // 分数 +score
        }

        /* ---------- 6. 填充堆，只留前 20 ---------- */
        userScore.forEach((userName, totalScore) -> {
            UserDTO u = userMap.get(userName);
            if (u == null) return;

            Map<String, Object> row = new HashMap<>();
            row.put("cnname", u.getCnname());
            row.put("userName", userName);
            row.put("count", userCount.get(userName));   // 完成题数
            row.put("totalScore", totalScore);           // 总分

            heap.offer(row);
            if (heap.size() > 20) heap.poll();
        });

        /* ---------- 7. 堆 -> 列表 -> 反转得降序 ---------- */
        List<Map<String, Object>> top20 = new ArrayList<>();
        while (!heap.isEmpty()) top20.add(heap.poll());
        Collections.reverse(top20);

        return new ResultVO(0, top20, null);
    }

}
