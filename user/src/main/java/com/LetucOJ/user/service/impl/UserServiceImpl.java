package com.LetucOJ.user.service.impl;

import com.LetucOJ.common.cache.Redis;
import com.LetucOJ.common.encode.Password;
import com.LetucOJ.common.log.LogLevel;
import com.LetucOJ.common.log.Logger;
import com.LetucOJ.common.log.Type;
import com.LetucOJ.common.oss.MinioRepos;
import com.LetucOJ.common.result.Result;
import com.LetucOJ.common.result.ResultVO;
import com.LetucOJ.common.result.errorcode.BaseErrorCode;
import com.LetucOJ.common.result.errorcode.ErrorCode;
import com.LetucOJ.common.result.errorcode.UserErrorCode;
import com.LetucOJ.user.model.JwtInfoVO;
import com.LetucOJ.user.model.RegisterRequestDTO;
import com.LetucOJ.user.model.UserInfoDTO;
import com.LetucOJ.user.model.UserManagerDTO;
import com.LetucOJ.user.repos.UserMybatisRepos;
import com.LetucOJ.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMybatisRepos userMybatisRepos;
    private final MinioRepos minioRepos;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // 常量定义
    private static final String BUCKET_NAME = "letucoj";
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z]{2,10}\\d{12}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^[A-Za-z0-9]{6,20}$");
    private static final String BACKGROUND_FILE = "background.txt";
    private static final String HEAD_PORTRAIT_FILE = "headPortrait.txt";

    // ================= 核心业务接口实现 =================

    @Override
    public ResultVO<Void> register(RegisterRequestDTO dto) {
        return executeSafe(() -> {
            // 1. 参数校验
            if (!isValidRegisterParams(dto)) {
                return Result.failure(UserErrorCode.PARAM_FORMAT_ERROR);
            }

            // 2. 查重
            if (userMybatisRepos.getUserFullInfo(dto.getUserName()) != null) {
                return Result.failure(UserErrorCode.USERNAME_ALREADY_EXISTS);
            }

            // 3. 执行注册
            String encodedPwd = Password.encrypt(dto.getPassword());
            UserManagerDTO newUser = new UserManagerDTO(dto.getUserName(), dto.getUserNickName(), encodedPwd, "USER", 1);

            return checkDbRows(userMybatisRepos.saveUserInfo(newUser), UserErrorCode.REGISTER_FAILED);
        });
    }

    @Override
    public ResultVO<JwtInfoVO> login(RegisterRequestDTO dto) {
        return executeSafe(() -> authenticate(dto.getUserName(), dto.getPassword(), true));
    }

    @Override
    public ResultVO<JwtInfoVO> refreshToken(String userName) {
        return executeSafely(() -> authenticate(userName, null, false));
    }

    @Override
    public ResultVO<Void> activateAccount(String userName) {
        return handleStatusChange(userName, userMybatisRepos::activateUser, false);
    }

    @Override
    public ResultVO<Void> deactivateAccount(String userName) {
        return handleStatusChange(userName, userMybatisRepos::deactivateUser, true);
    }

    @Override
    public ResultVO<Void> logout(String userName) {
        return executeSafe(() -> {
            Redis.mapPutDuration("black:" + userName, "0", 7 * 24 * 60 * 60);
            return Result.success();
        });
    }

    @Override
    public ResultVO<List<UserManagerDTO>> getAllUsers() {
        return getUsersByRole("USER", UserErrorCode.NO_USER);
    }

    @Override
    public ResultVO<List<UserManagerDTO>> getAllManagers() {
        return getUsersByRole("MANAGER", UserErrorCode.NO_MANAGER);
    }

    @Override
    public ResultVO<Void> promoteToManager(String userName) {
        return handleStatusChange(userName, userMybatisRepos::setUserToManager, false);
    }

    @Override
    public ResultVO<Void> demoteToUser(String userName) {
        return handleStatusChange(userName, userMybatisRepos::setManagerToUser, true);
    }

    @Override
    public ResultVO<UserInfoDTO> getUserFullInfo(String userName) {
        return executeSafe(() -> {
            if (userName == null) return Result.failure(UserErrorCode.EMPTY_PARAMETER, null);
            UserInfoDTO info = userMybatisRepos.getUserFullInfo(userName);
            return info != null ? Result.success(info) : Result.failure(UserErrorCode.NO_USER, null);
        });
    }

    @Override
    public ResultVO<Void> updateUserFullInfo(UserInfoDTO userInfoDTO) {
        return executeSafe(() -> {
            if (userInfoDTO == null || userInfoDTO.getUserName() == null) {
                return Result.failure(UserErrorCode.EMPTY_PARAMETER);
            }
            return checkDbRows(userMybatisRepos.updateUserInfo(userInfoDTO), BaseErrorCode.SERVICE_ERROR);
        });
    }

    // --- MinIO 相关 ---

    @Override
    public ResultVO<byte[]> getBackground(String userName) {
        return getFileSafe(userName, BACKGROUND_FILE, UserErrorCode.NO_BACKGROUND);
    }

    @Override
    public ResultVO<Void> updateBackground(String userName, byte[] data) {
        return updateFileSafe(userName, BACKGROUND_FILE, data);
    }

    @Override
    public ResultVO<byte[]> getHeadPortrait(String userName) {
        return getFileSafe(userName, HEAD_PORTRAIT_FILE, UserErrorCode.NO_HEADPORTRAIT);
    }

    @Override
    public ResultVO<Void> updateHeadPortrait(String userName, byte[] data) {
        return updateFileSafe(userName, HEAD_PORTRAIT_FILE, data);
    }

    @Override
    public ResultVO<byte[]> getHeatmap(String userName, int year) {
        return executeSafe(() -> {
            String objectName = "user/" + userName + "/heatmap/" + year + ".json";
            if (!minioRepos.isObjectExist(BUCKET_NAME, objectName)) {
                return Result.failure(UserErrorCode.NO_HEATMAP, null);
            }
            return Result.success(minioRepos.getFile(BUCKET_NAME, objectName));
        });
    }

    // --- 邮件与密码 ---

    @Override
    public ResultVO<String> getSecretKey(String userName) {
        return executeSafe(() -> {
            UserInfoDTO user = userMybatisRepos.getUserFullInfo(userName);
            if (user == null) return Result.failure(UserErrorCode.NO_USER, null);
            if (isEmpty(user.getEmail())) return Result.failure(UserErrorCode.NO_EMAIL, null);

            String secretKey = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
            sendSecretKeyEmail(user.getEmail(), userName, secretKey);

            Redis.mapPutDuration(userName, secretKey, 120);
            return Result.success("密钥已成功发送至您的邮箱: " + user.getEmail());
        });
    }

    @Override
    public ResultVO<Void> changePassword(String userName, String secretKey, String newPassword) {
        return executeSafe(() -> {
            String cachedKey = Redis.mapGet(userName);
            if (!Objects.equals(cachedKey, secretKey)) {
                Logger.log(Type.SERVER, LogLevel.WARN, "Password change failed: invalid key for " + userName);
                return Result.failure(UserErrorCode.SECRET_KEY_INVALID);
            }
            return checkDbRows(
                    userMybatisRepos.updatePassword(userName, Password.encrypt(newPassword)),
                    BaseErrorCode.SERVICE_ERROR
            );
        });
    }

    // --- 排行榜逻辑 ---

    @Override
    public ResultVO<Object> getUserRankings() {
        return executeSafe(() -> {
            List<Map<String, Object>> correctSubmissions = userMybatisRepos.listCorrect();
            List<UserManagerDTO> activeUsers = userMybatisRepos.getUserListByRole("USER");

            if (isEmpty(correctSubmissions)) return Result.failure(UserErrorCode.NO_RANK, null);
            if (isEmpty(activeUsers)) return Result.failure(UserErrorCode.NO_USER, null);

            // 1. 构建题目分数映射：problem_name -> difficulty
            Map<String, Integer> problemScoreMap = userMybatisRepos.points().stream()
                    .filter(m -> m.get("problem_name") != null && m.get("difficulty") != null)
                    .collect(Collectors.toMap(
                            m -> m.get("problem_name").toString().trim(),
                            m -> Integer.parseInt(m.get("difficulty").toString()),
                            (existing, replacement) -> existing // 处理可能的重复题目名，保留第一个
                    ));

            // 2. 计算用户聚合数据：user_name -> (通过题目数, 总分)
            Map<String, UserStats> userStatsMap = new HashMap<>();

            for (Map<String, Object> submission : correctSubmissions) {
                String userName = safeString(submission.get("user_name"));
                String problemName = safeString(submission.get("problem_name"));
                
                if (userName == null || problemName == null) continue;

                UserStats stats = userStatsMap.computeIfAbsent(userName, k -> new UserStats());
                stats.passCount++;
                stats.totalScore += problemScoreMap.getOrDefault(problemName, 0);
            }

            // 3. 构建用户映射：user_name -> UserManagerDTO
            Map<String, UserManagerDTO> userInfoMap = activeUsers.stream()
                    .collect(Collectors.toMap(UserManagerDTO::getUserName, Function.identity()));

            // 4. 构建并排序排行榜
            List<Map<String, Object>> topRankings = userStatsMap.entrySet().stream()
                    .filter(entry -> userInfoMap.containsKey(entry.getKey()))
                    .map(entry -> {
                        String userName = entry.getKey();
                        UserStats stats = entry.getValue();
                        UserManagerDTO user = userInfoMap.get(userName);
                        
                        Map<String, Object> rankItem = new HashMap<>();
                        rankItem.put("userName", userName);
                        rankItem.put("userNickName", user.getUserNickName());
                        rankItem.put("count", stats.passCount);
                        rankItem.put("totalScore", stats.totalScore);
                        
                        return rankItem;
                    })
                    .sorted((a, b) -> {
                        // 首先按总分降序排序
                        int scoreCompare = Integer.compare(
                                (Integer) b.getOrDefault("totalScore", 0),
                                (Integer) a.getOrDefault("totalScore", 0)
                        );
                        if (scoreCompare != 0) return scoreCompare;
                        
                        // 总分相同时按用户名升序排序
                        return ((String) a.getOrDefault("userName", "")).compareTo(
                                (String) b.getOrDefault("userName", "")
                        );
                    })
                    .limit(20)
                    .collect(Collectors.toList());

            return Result.success(topRankings);
        });
    }
    
    // 辅助类：用于存储用户统计数据
    private static class UserStats {
        int passCount = 0;
        int totalScore = 0;
    }


    // ================= 私有辅助方法 (核心重构部分) =================

    /**
     * 核心环绕执行方法：统一异常处理
     */
    private <T> ResultVO<T> executeSafe(Supplier<ResultVO<T>> action) {
        try {
            return action.get();
        } catch (Exception e) {
            Logger.log(Type.SERVER, LogLevel.ERROR, e.getMessage() != null ? e.getMessage() : "Unknown Error");
            return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
        }
    }

    // 为了兼容你原来的代码调用习惯，保留 executeSafely 这个名字作为别名，或者你可以把上面的改成 executeSafely
    private <T> ResultVO<T> executeSafely(Supplier<ResultVO<T>> action) {
        return executeSafe(action);
    }

    /**
     * 统一数据库受影响行数检查
     */
    private ResultVO<Void> checkDbRows(Integer rows, ErrorCode failCode) {
        if (rows != null && rows > 0) {
            return Result.success();
        }
        return Result.failure(failCode);
    }

    /**
     * 认证逻辑抽取
     */
    private ResultVO<JwtInfoVO> authenticate(String userName, String rawPassword, boolean checkPassword) {
        UserManagerDTO user = userMybatisRepos.getPasswordByUserName(userName);
        if (user == null) {
            return Result.failure(UserErrorCode.NAME_OR_CODE_WRONG, null);
        }
        if (checkPassword && !Password.matches(rawPassword, user.getPassword())) {
            return Result.failure(UserErrorCode.NAME_OR_CODE_WRONG, null);
        }
        if (user.getStatus() == 0) {
            return Result.failure(UserErrorCode.NOT_ENABLE, null);
        }
        JwtInfoVO jwt = new JwtInfoVO(userName, user.getUserNickName(), user.getRole(), System.currentTimeMillis());
        return Result.success(jwt);
    }

    /**
     * 状态变更模板方法
     */
    private ResultVO<Void> handleStatusChange(String userName, Function<String, Integer> operation, boolean needLogout) {
        return executeSafe(() -> {
            Integer rows = operation.apply(userName);
            if (rows != null && rows == 1) {
                if (needLogout) {
                    logout(userName); // 强制登出
                }
                return Result.success();
            }
            return Result.failure(BaseErrorCode.SERVICE_ERROR);
        });
    }

    /**
     * 根据角色获取用户列表
     */
    private ResultVO<List<UserManagerDTO>> getUsersByRole(String role, ErrorCode emptyError) {
        return executeSafe(() -> {
            List<UserManagerDTO> list = userMybatisRepos.getUserListByRole(role);
            if (isEmpty(list)) {
                return Result.failure(emptyError, null);
            }
            list.forEach(u -> u.setPassword(null));
            return Result.success(list);
        });
    }

    /**
     * 安全获取文件
     */
    private ResultVO<byte[]> getFileSafe(String userName, String fileName, ErrorCode failCode) {
        return executeSafe(() -> {
            try {
                String objectName = "user/" + userName + "/" + fileName;
                return Result.success(minioRepos.getFile(BUCKET_NAME, objectName));
            } catch (Exception e) {
                return Result.failure(failCode, null);
            }
        });
    }

    /**
     * 安全上传文件
     */
    private ResultVO<Void> updateFileSafe(String userName, String fileName, byte[] data) {
        return executeSafe(() -> {
            String objectName = "user/" + userName + "/" + fileName;
            minioRepos.addFile(BUCKET_NAME, objectName, data);
            return Result.success();
        });
    }

    // --- 业务辅助工具 ---

    private boolean isValidRegisterParams(RegisterRequestDTO dto) {
        return USERNAME_PATTERN.matcher(dto.getUserName()).matches() &&
                PASSWORD_PATTERN.matcher(dto.getPassword()).matches() &&
                dto.getUserNickName() != null && !dto.getUserNickName().isEmpty() && dto.getUserNickName().length() <= 20;
    }

    private void sendSecretKeyEmail(String email, String userName, String secretKey) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(email);
        message.setSubject("您在 LetucOJ 的密钥");
        message.setText(String.format("尊敬的 %s,\n\n您请求的密钥如下:\n\n密钥: %s\n\n请使用此密钥继续操作。", userName, secretKey));
        mailSender.send(message);
    }

    private Map<String, Object> buildRankItem(Map.Entry<String, Integer> entry, UserManagerDTO user, Map<String, Integer> passCounts) {
        Map<String, Object> map = new HashMap<>();
        map.put("userName", entry.getKey());
        map.put("cnname", user.getUserNickName());
        map.put("count", passCounts.get(entry.getKey()));
        map.put("totalScore", entry.getValue());
        return map;
    }

    private int compareRank(Map<String, Object> a, Map<String, Object> b) {
        Integer scoreA = (Integer) a.getOrDefault("totalScore", 0);
        Integer scoreB = (Integer) b.getOrDefault("totalScore", 0);
        int scoreCompare = Integer.compare(scoreB, scoreA); // 分数降序
        if (scoreCompare != 0) return scoreCompare;
        String userNameA = (String) a.getOrDefault("userName", "");
        String userNameB = (String) b.getOrDefault("userName", "");
        return userNameA.compareTo(userNameB); // 名字升序
    }

    private boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    private boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    private String safeString(Object... objs) {
        for (Object o : objs) {
            if (o != null) return o.toString().trim();
        }
        return null;
    }
}