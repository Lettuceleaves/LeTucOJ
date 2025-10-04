package com.LetucOJ.sys.service.impl;

import com.LetucOJ.common.result.Result;
import com.LetucOJ.common.result.ResultVO;
import com.LetucOJ.common.result.errorcode.BaseErrorCode;
import com.LetucOJ.common.result.errorcode.UserErrorCode;
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
                return Result.failure(UserErrorCode.REGISTER_FAILED);
            }
            return Result.success();
        } catch (Exception e) {
            return Result.failure(UserErrorCode.REGISTER_FAILED);
        }
    }

    @Override
    public ResultVO login(RegisterRequestDTO dto) {
        String username = dto.getUsername();
        String rawPwd = dto.getPassword();
        try {
            long version = System.currentTimeMillis();
            UserDTO userDTO = userMybatisRepos.getPasswordByUserName(username);
            if (userDTO == null || !PasswordUtil.matches(rawPwd, userDTO.getPassword())) {
                return Result.failure(UserErrorCode.NAME_OR_CODE_WRONG);
            } else if (!userDTO.isEnabled()) {
                return Result.failure(UserErrorCode.NOT_ENABLE);
            }
            String cnname = userDTO.getCnname();
            System.out.println(cnname);
            JwtInfoVO jwtInfoVO = new JwtInfoVO(username, cnname, userDTO.getRole(), version);
            return Result.success(jwtInfoVO);
        } catch (Exception e) {
            return Result.failure(BaseErrorCode.SERVICE_ERROR);
        }
    }

    @Override
    public ResultVO refreshToken(String userName) {
        try {
            long version = System.currentTimeMillis();
            UserDTO userDTO = userMybatisRepos.getPasswordByUserName(userName);
            if (userDTO == null ) {
                return Result.failure(UserErrorCode.NAME_OR_CODE_WRONG);
            } else if (!userDTO.isEnabled()) {
                return Result.failure(UserErrorCode.NOT_ENABLE);
            }
            String cnname = userDTO.getCnname();
            System.out.println(cnname);
            JwtInfoVO jwtInfoVO = new JwtInfoVO(userName, cnname, userDTO.getRole(), version);
            return Result.success(jwtInfoVO);
        } catch (Exception e) {
            return Result.failure(BaseErrorCode.SERVICE_ERROR);
        }
    }

    @Override
    public ResultVO activateAccount(String userName) {
        try {
            Integer rows = userMybatisRepos.activateUser(userName);
            if (rows != null && rows == 1) {
                return Result.success();
            } else {
                return Result.failure(BaseErrorCode.SERVICE_ERROR);
            }
        } catch (Exception e) {
            return Result.failure(BaseErrorCode.SERVICE_ERROR);
        }
    }

    @Override
    public ResultVO deactivateAccount(String userName) {
        try {
            Integer rows = userMybatisRepos.deactivateUser(userName);
            if (rows != null && rows == 1) {
                return Result.success();
            } else {
                return Result.failure(BaseErrorCode.SERVICE_ERROR);
            }
        } catch (Exception e) {
            return Result.failure(BaseErrorCode.SERVICE_ERROR);
        }
    }

    @Override
    public ResultVO logout(String token, long ttl) {
        try {
            if (ttl > 0) {
                redisTemplate.opsForValue().set(BLACKLIST_PREFIX + token, "1", Duration.ofSeconds(ttl));
            }
            return Result.success();
        } catch (Exception e) {
            return Result.failure(BaseErrorCode.SERVICE_ERROR);
        }
    }

    @Override
    public ResultVO getAllUsers() {
        try {
            List<UserDTO> list = userMybatisRepos.getUsersByRole("USER");
            if (list == null || list.isEmpty()) {
                return Result.failure(UserErrorCode.NO_USER);
            }
            for (UserDTO user : list) {
                user.setPassword(null); // Clear password for security
            }
            return Result.success(list);
        } catch (Exception e) {
            return Result.failure(BaseErrorCode.SERVICE_ERROR);
        }
    }

    @Override
    public ResultVO getAllManagers() {
        try {
            List<UserDTO> list = userMybatisRepos.getUsersByRole("MANAGER");
            if (list == null || list.isEmpty()) {
                return Result.failure(UserErrorCode.NO_MANAGER);
            }
            for (UserDTO user : list) {
                user.setPassword(null); // Clear password for security
            }
            return Result.success(list);
        } catch (Exception e) {
            return Result.failure(BaseErrorCode.SERVICE_ERROR);
        }
    }

    @Override
    public ResultVO promoteToManager(String userName) {
        try {
            Integer rows = userMybatisRepos.setUserToManager(userName);
            if (rows != null && rows == 1) {
                return Result.success();
            } else {
                return Result.failure(BaseErrorCode.SERVICE_ERROR);
            }
        } catch (Exception e) {
            return Result.failure(BaseErrorCode.SERVICE_ERROR);
        }
    }

    @Override
    public ResultVO demoteToUser(String userName) {
        try {
            Integer rows = userMybatisRepos.setManagerToUser(userName);
            if (rows != null && rows == 1) {
                return Result.success();
            } else {
                return Result.failure(BaseErrorCode.SERVICE_ERROR);
            }
        } catch (Exception e) {
            return Result.failure(BaseErrorCode.SERVICE_ERROR);
        }
    }

    @Override
    public ResultVO getUserRankings() {
        /* ---------- 1. 取数据 ---------- */
        List<Map<String, Object>> corrects = userMybatisRepos.listCorrect();
        List<UserDTO> users = userMybatisRepos.getUsersByRole("USER");

        if (corrects == null || corrects.isEmpty()) {
            return Result.failure(UserErrorCode.NO_RANK);
        }
        if (users == null || users.isEmpty()) {
            return Result.failure(UserErrorCode.NO_USER);
        }

        Map<String, Integer> scoreMap = userMybatisRepos.points()
                .stream()
                .collect(Collectors.toMap(
                        m -> m.get("name").toString().trim(),
                        m -> Integer.parseInt(m.get("difficulty").toString())));

        Map<String, UserDTO> userMap = users.stream()
                .collect(Collectors.toMap(UserDTO::getUserName, Function.identity()));

        PriorityQueue<Map<String, Object>> heap = new PriorityQueue<>((a, b) -> {
            int sa = (int) a.get("totalScore");
            int sb = (int) b.get("totalScore");
            if (sa != sb) return Integer.compare(sa, sb);
            String ua = (String) a.get("userName");
            String ub = (String) b.get("userName");
            return ub.compareTo(ua);
        });

        Map<String, Integer> userCount  = new HashMap<>();
        Map<String, Integer> userScore  = new HashMap<>();
        for (Map<String, Object> rec : corrects) {
            String userName = Optional.ofNullable(
                            rec.get("userName") != null ? rec.get("userName") : rec.get("user_name"))
                    .map(Object::toString).map(String::trim).orElse(null);
            String problemName = Optional.ofNullable(rec.get("name"))
                    .map(Object::toString).map(String::trim).orElse(null);
            if (userName == null || problemName == null) continue;

            int score = scoreMap.getOrDefault(problemName, 0);
            userCount.merge(userName, 1, Integer::sum);
            userScore.merge(userName, score, Integer::sum);
        }

        userScore.forEach((userName, totalScore) -> {
            UserDTO u = userMap.get(userName);
            if (u == null) return;

            Map<String, Object> row = new HashMap<>();
            row.put("cnname", u.getCnname());
            row.put("userName", userName);
            row.put("count", userCount.get(userName));
            row.put("totalScore", totalScore);

            heap.offer(row);
            if (heap.size() > 20) heap.poll();
        });

        List<Map<String, Object>> top20 = new ArrayList<>();
        while (!heap.isEmpty()) top20.add(heap.poll());
        Collections.reverse(top20);

        return Result.success(top20);
    }

}
