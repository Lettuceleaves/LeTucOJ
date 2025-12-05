package com.LetucOJ.user.service.impl;

import com.LetucOJ.common.cache.Redis;
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
import com.LetucOJ.user.util.PasswordUtil;
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
            if (userMybatisRepos.getUserFullInfo(dto.getUsername()) != null) {
                return Result.failure(UserErrorCode.USERNAME_ALREADY_EXISTS);
            }

            // 3. 执行注册
            String encodedPwd = PasswordUtil.encrypt(dto.getPassword());
            UserManagerDTO newUser = new UserManagerDTO(dto.getUsername(), dto.getCnname(), encodedPwd, "USER", 1);

            return checkDbRows(userMybatisRepos.saveUserInfo(newUser), UserErrorCode.REGISTER_FAILED);
        });
    }

    @Override
    public ResultVO<JwtInfoVO> login(RegisterRequestDTO dto) {
        return executeSafe(() -> authenticate(dto.getUsername(), dto.getPassword(), true));
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
    public ResultVO<Void> logout(String username) {
        return executeSafe(() -> {
            Redis.mapPutDuration("black:" + username, "0", 7 * 24 * 60 * 60);
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
    public ResultVO<UserInfoDTO> getUserFullInfo(String username) {
        return executeSafe(() -> {
            if (username == null) return Result.failure(UserErrorCode.EMPTY_PARAMETER, null);
            UserInfoDTO info = userMybatisRepos.getUserFullInfo(username);
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
    public ResultVO<byte[]> getBackground(String username) {
        return getFileSafe(username, BACKGROUND_FILE, UserErrorCode.NO_BACKGROUND);
    }

    @Override
    public ResultVO<Void> updateBackground(String username, byte[] data) {
        return updateFileSafe(username, BACKGROUND_FILE, data);
    }

    @Override
    public ResultVO<byte[]> getHeadPortrait(String username) {
        return getFileSafe(username, HEAD_PORTRAIT_FILE, UserErrorCode.NO_HEADPORTRAIT);
    }

    @Override
    public ResultVO<Void> updateHeadPortrait(String username, byte[] data) {
        return updateFileSafe(username, HEAD_PORTRAIT_FILE, data);
    }

    @Override
    public ResultVO<byte[]> getHeatmap(String username, int year) {
        return executeSafe(() -> {
            String objectName = "user/" + username + "/heatmap/" + year + ".json";
            if (!minioRepos.isObjectExist(BUCKET_NAME, objectName)) {
                return Result.failure(UserErrorCode.NO_HEATMAP, null);
            }
            return Result.success(minioRepos.getFile(BUCKET_NAME, objectName));
        });
    }

    // --- 邮件与密码 ---

    @Override
    public ResultVO<String> getSecretKey(String username) {
        return executeSafe(() -> {
            UserInfoDTO user = userMybatisRepos.getUserFullInfo(username);
            if (user == null) return Result.failure(UserErrorCode.NO_USER, null);
            if (isEmpty(user.getEmail())) return Result.failure(UserErrorCode.NO_EMAIL, null);

            String secretKey = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
            sendSecretKeyEmail(user.getEmail(), username, secretKey);

            Redis.mapPutDuration(username, secretKey, 120);
            return Result.success("密钥已成功发送至您的邮箱: " + user.getEmail());
        });
    }

    @Override
    public ResultVO<Void> changePassword(String username, String secretKey, String newPassword) {
        return executeSafe(() -> {
            String cachedKey = Redis.mapGet(username);
            if (!Objects.equals(cachedKey, secretKey)) {
                Logger.log(Type.SERVER, LogLevel.WARN, "Password change failed: invalid key for " + username);
                return Result.failure(UserErrorCode.SECRET_KEY_INVALID);
            }
            return checkDbRows(
                    userMybatisRepos.updatePassword(username, PasswordUtil.encrypt(newPassword)),
                    BaseErrorCode.SERVICE_ERROR
            );
        });
    }

    // --- 排行榜逻辑 ---

    @Override
    public ResultVO<Object> getUserRankings() {
        return executeSafe(() -> {
            List<Map<String, Object>> corrects = userMybatisRepos.listCorrect();
            List<UserManagerDTO> users = userMybatisRepos.getUserListByRole("USER");

            if (isEmpty(corrects)) return Result.failure(UserErrorCode.NO_RANK, null);
            if (isEmpty(users)) return Result.failure(UserErrorCode.NO_USER, null);

            // 1. 构建题目分数映射
            Map<String, Integer> problemScores = userMybatisRepos.points().stream()
                    .collect(Collectors.toMap(
                            m -> m.get("name").toString().trim(),
                            m -> Integer.parseInt(m.get("difficulty").toString())));

            // 2. 计算聚合数据
            Map<String, Integer> userTotalScore = new HashMap<>();
            Map<String, Integer> userPassCount = new HashMap<>();

            for (Map<String, Object> record : corrects) {
                String uName = safeString(record.get("userName"), record.get("user_name"));
                String pName = safeString(record.get("name"));
                if (uName == null || pName == null) continue;

                userPassCount.merge(uName, 1, Integer::sum);
                userTotalScore.merge(uName, problemScores.getOrDefault(pName, 0), Integer::sum);
            }

            // 3. 构建并排序
            Map<String, UserManagerDTO> userMap = users.stream()
                    .collect(Collectors.toMap(UserManagerDTO::getUserName, Function.identity()));

            List<Map<String, Object>> topList = userTotalScore.entrySet().stream()
                    .filter(entry -> userMap.containsKey(entry.getKey()))
                    .map(entry -> buildRankItem(entry, userMap.get(entry.getKey()), userPassCount))
                    .sorted(this::compareRank)
                    .limit(20)
                    .collect(Collectors.toList());

            return Result.success(topList);
        });
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
    private ResultVO<JwtInfoVO> authenticate(String username, String rawPassword, boolean checkPassword) {
        UserManagerDTO user = userMybatisRepos.getPasswordByUserName(username);
        if (user == null) {
            return Result.failure(UserErrorCode.NAME_OR_CODE_WRONG, null);
        }
        if (checkPassword && !PasswordUtil.matches(rawPassword, user.getPassword())) {
            return Result.failure(UserErrorCode.NAME_OR_CODE_WRONG, null);
        }
        if (user.getStatus() == 0) {
            return Result.failure(UserErrorCode.NOT_ENABLE, null);
        }
        JwtInfoVO jwt = new JwtInfoVO(username, user.getCnname(), user.getRole(), System.currentTimeMillis());
        return Result.success(jwt);
    }

    /**
     * 状态变更模板方法
     */
    private ResultVO<Void> handleStatusChange(String username, Function<String, Integer> operation, boolean needLogout) {
        return executeSafe(() -> {
            Integer rows = operation.apply(username);
            if (rows != null && rows == 1) {
                if (needLogout) {
                    logout(username); // 强制登出
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
    private ResultVO<byte[]> getFileSafe(String username, String fileName, ErrorCode failCode) {
        return executeSafe(() -> {
            try {
                String objectName = "user/" + username + "/" + fileName;
                return Result.success(minioRepos.getFile(BUCKET_NAME, objectName));
            } catch (Exception e) {
                return Result.failure(failCode, null);
            }
        });
    }

    /**
     * 安全上传文件
     */
    private ResultVO<Void> updateFileSafe(String username, String fileName, byte[] data) {
        return executeSafe(() -> {
            String objectName = "user/" + username + "/" + fileName;
            minioRepos.addFile(BUCKET_NAME, objectName, data);
            return Result.success();
        });
    }

    // --- 业务辅助工具 ---

    private boolean isValidRegisterParams(RegisterRequestDTO dto) {
        return USERNAME_PATTERN.matcher(dto.getUsername()).matches() &&
                PASSWORD_PATTERN.matcher(dto.getPassword()).matches() &&
                dto.getCnname() != null && !dto.getCnname().isEmpty() && dto.getCnname().length() <= 20;
    }

    private void sendSecretKeyEmail(String email, String username, String secretKey) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(email);
        message.setSubject("您在 LetucOJ 的密钥");
        message.setText(String.format("尊敬的 %s,\n\n您请求的密钥如下:\n\n密钥: %s\n\n请使用此密钥继续操作。", username, secretKey));
        mailSender.send(message);
    }

    private Map<String, Object> buildRankItem(Map.Entry<String, Integer> entry, UserManagerDTO user, Map<String, Integer> passCounts) {
        Map<String, Object> map = new HashMap<>();
        map.put("userName", entry.getKey());
        map.put("cnname", user.getCnname());
        map.put("count", passCounts.get(entry.getKey()));
        map.put("totalScore", entry.getValue());
        return map;
    }

    private int compareRank(Map<String, Object> a, Map<String, Object> b) {
        int scoreCompare = Integer.compare((int) b.get("totalScore"), (int) a.get("totalScore")); // 分数降序
        if (scoreCompare != 0) return scoreCompare;
        return ((String) a.get("userName")).compareTo((String) b.get("userName")); // 名字升序
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