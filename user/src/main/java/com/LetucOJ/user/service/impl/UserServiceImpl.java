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
@RequiredArgsConstructor // 推荐：使用构造器注入代替 @Autowired
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

    // =================================================================================
    // 核心业务接口实现
    // =================================================================================

    @Override
    public ResultVO<Void> register(RegisterRequestDTO dto) {
        return executeSafely(() -> {
            if (!USERNAME_PATTERN.matcher(dto.getUsername()).matches() ||
                    !PASSWORD_PATTERN.matcher(dto.getPassword()).matches() ||
                    dto.getCnname() == null || dto.getCnname().isEmpty() || dto.getCnname().length() > 20) {
                return Result.failure(UserErrorCode.PARAM_FORMAT_ERROR);
            }

            if (userMybatisRepos.getUserFullInfo(dto.getUsername()) != null) {
                return Result.failure(UserErrorCode.USERNAME_ALREADY_EXISTS);
            }

            String encodedPwd = PasswordUtil.encrypt(dto.getPassword());
            UserManagerDTO newUser = new UserManagerDTO(dto.getUsername(), dto.getCnname(), encodedPwd, "USER", 1);

            Integer result = userMybatisRepos.saveUserInfo(newUser);
            if (!Integer.valueOf(1).equals(result)) {
                return Result.failure(UserErrorCode.REGISTER_FAILED);
            }
            return Result.success();
        });
    }

    @Override
    public ResultVO<JwtInfoVO> login(RegisterRequestDTO dto) {
        return executeSafely(() -> authenticate(dto.getUsername(), dto.getPassword(), true));
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
        return executeSafely(() -> {
            Redis.mapPutDuration("black:" + username, "0", 7 * 24 * 60 * 60);
            return Result.success();
        });
    }

    @Override
    public ResultVO<List<UserManagerDTO>> getAllUsers() {
        return getUsersInternal("USER", UserErrorCode.NO_USER);
    }

    @Override
    public ResultVO<List<UserManagerDTO>> getAllManagers() {
        return getUsersInternal("MANAGER", UserErrorCode.NO_MANAGER);
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
        if (username == null) return Result.failure(UserErrorCode.EMPTY_PARAMETER, null);
        return executeSafely(() -> {
            UserInfoDTO info = userMybatisRepos.getUserFullInfo(username);
            return info != null ? Result.success(info) : Result.failure(UserErrorCode.NO_USER, null);
        });
    }

    @Override
    public ResultVO<Void> updateUserFullInfo(UserInfoDTO userInfoDTO) {
        if (userInfoDTO == null || userInfoDTO.getUserName() == null) {
            return Result.failure(UserErrorCode.EMPTY_PARAMETER);
        }
        return executeSafely(() -> {
            Integer res = userMybatisRepos.updateUserInfo(userInfoDTO);
            return (res != null && res == 1) ? Result.success() : Result.failure(BaseErrorCode.SERVICE_ERROR);
        });
    }

    // --- MinIO 相关 ---

    @Override
    public ResultVO<byte[]> getBackground(String username) {
        return getFileInternal(username, BACKGROUND_FILE, UserErrorCode.NO_BACKGROUND);
    }

    @Override
    public ResultVO<Void> updateBackground(String username, byte[] data) {
        return updateFileInternal(username, BACKGROUND_FILE, data);
    }

    @Override
    public ResultVO<byte[]> getHeadPortrait(String username) {
        return getFileInternal(username, HEAD_PORTRAIT_FILE, UserErrorCode.NO_HEADPORTRAIT);
    }

    @Override
    public ResultVO<Void> updateHeadPortrait(String username, byte[] data) {
        return updateFileInternal(username, HEAD_PORTRAIT_FILE, data);
    }

    @Override
    public ResultVO<byte[]> getHeatmap(String username, int year) {
        return executeSafely(() -> {
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
        return executeSafely(() -> {
            UserInfoDTO user = userMybatisRepos.getUserFullInfo(username);
            if (user == null) return Result.failure(UserErrorCode.NO_USER, null);
            if (user.getEmail() == null || user.getEmail().isEmpty()) return Result.failure(UserErrorCode.NO_EMAIL, null);

            String secretKey = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
            sendSecretKeyEmail(user.getEmail(), username, secretKey);

            Redis.mapPutDuration(username, secretKey, 120);
            return Result.success("密钥已成功发送至您的邮箱: " + user.getEmail());
        });
    }

    @Override
    public ResultVO<Void> changePassword(String username, String secretKey, String newPassword) {
        return executeSafely(() -> {
            String cachedKey = Redis.mapGet(username);
            if (!Objects.equals(cachedKey, secretKey)) {
                Logger.log(Type.SERVER, LogLevel.WARN, "Password change failed: invalid key for " + username);
                return Result.failure(UserErrorCode.SECRET_KEY_INVALID);
            }
            Integer res = userMybatisRepos.updatePassword(username, PasswordUtil.encrypt(newPassword));
            return (res != null && res == 1) ? Result.success() : Result.failure(BaseErrorCode.SERVICE_ERROR);
        });
    }

    // --- 排行榜 (逻辑较重，单独保留但简化内部逻辑) ---

    @Override
    public ResultVO<Object> getUserRankings() {
        return executeSafely(() -> {
            List<Map<String, Object>> corrects = userMybatisRepos.listCorrect();
            List<UserManagerDTO> users = userMybatisRepos.getUserListByRole("USER");

            if (isEmpty(corrects)) return Result.failure(UserErrorCode.NO_RANK, null);
            if (isEmpty(users)) return Result.failure(UserErrorCode.NO_USER, null);

            // 1. 构建题目分数映射
            Map<String, Integer> problemScores = userMybatisRepos.points().stream()
                    .collect(Collectors.toMap(
                            m -> m.get("name").toString().trim(),
                            m -> Integer.parseInt(m.get("difficulty").toString())));

            // 2. 计算用户统计数据 (分数和通过数)
            Map<String, Integer> userTotalScore = new HashMap<>();
            Map<String, Integer> userPassCount = new HashMap<>();

            for (Map<String, Object> record : corrects) {
                String uName = safeString(record.get("userName"), record.get("user_name"));
                String pName = safeString(record.get("name"));
                if (uName == null || pName == null) continue;

                userPassCount.merge(uName, 1, Integer::sum);
                userTotalScore.merge(uName, problemScores.getOrDefault(pName, 0), Integer::sum);
            }

            // 3. 构建用户详细信息并排序 (Top 20)
            Map<String, UserManagerDTO> userMap = users.stream()
                    .collect(Collectors.toMap(UserManagerDTO::getUserName, Function.identity()));

            List<Map<String, Object>> topList = userTotalScore.entrySet().stream()
                    .filter(entry -> userMap.containsKey(entry.getKey()))
                    .map(entry -> {
                        String uname = entry.getKey();
                        Map<String, Object> map = new HashMap<>();
                        map.put("userName", uname);
                        map.put("cnname", userMap.get(uname).getCnname());
                        map.put("count", userPassCount.get(uname));
                        map.put("totalScore", entry.getValue());
                        return map;
                    })
                    .sorted((a, b) -> {
                        int cmp = Integer.compare((int) b.get("totalScore"), (int) a.get("totalScore")); // 分数降序
                        if (cmp != 0) return cmp;
                        return ((String) a.get("userName")).compareTo((String) b.get("userName")); // 名字升序
                    })
                    .limit(20)
                    .collect(Collectors.toList());

            return Result.success(topList);
        });
    }

    // =================================================================================
    // ⚠️ 核心重构：通用模板与辅助方法 (Private Helpers)
    // =================================================================================

    /**
     * [设计模式：模板方法/策略]
     * 统一处理 try-catch，日志记录和服务端错误返回。
     * 所有业务逻辑只需要关注成功时的处理和特定的业务失败。
     */
    private <T> ResultVO<T> executeSafely(Supplier<ResultVO<T>> action) {
        try {
            return action.get();
        } catch (Exception e) {
            Logger.log(Type.SERVER, LogLevel.ERROR, e.getMessage() != null ? e.getMessage() : "Unknown Error");
            // 泛型强制转换，虽然 unsafe 但在 Result.failure 里通常是 null 数据
            return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
        }
    }

    /**
     * 统一的认证逻辑 (登录 & 刷新 Token)
     */
    private ResultVO<JwtInfoVO> authenticate(String username, String rawPassword, boolean checkPassword) {
        UserManagerDTO user = userMybatisRepos.getPasswordByUserName(username);

        // 1. 账号不存在
        if (user == null) {
            return Result.failure(UserErrorCode.NAME_OR_CODE_WRONG, null);
        }
        // 2. 密码错误 (仅登录时检查)
        if (checkPassword && !PasswordUtil.matches(rawPassword, user.getPassword())) {
            return Result.failure(UserErrorCode.NAME_OR_CODE_WRONG, null);
        }
        // 3. 账号被禁用
        if (user.getStatus() == 0) {
            return Result.failure(UserErrorCode.NOT_ENABLE, null);
        }

        JwtInfoVO jwt = new JwtInfoVO(username, user.getCnname(), user.getRole(), System.currentTimeMillis());
        return Result.success(jwt);
    }

    /**
     * 统一的 MinIO 文件获取逻辑
     */
    private ResultVO<byte[]> getFileInternal(String username, String fileName, ErrorCode failCode) {
        return executeSafely(() -> {
            String objectName = "user/" + username + "/" + fileName;
            try {
                return Result.success(minioRepos.getFile(BUCKET_NAME, objectName));
            } catch (Exception e) {
                // 这里捕获 MinIO 特定异常并返回业务错误码
                return Result.failure(failCode, null);
            }
        });
    }

    /**
     * 统一的 MinIO 文件上传逻辑
     */
    private ResultVO<Void> updateFileInternal(String username, String fileName, byte[] data) {
        return executeSafely(() -> {
            String objectName = "user/" + username + "/" + fileName;
            minioRepos.addFile(BUCKET_NAME, objectName, data);
            return Result.success();
        });
    }

    /**
     * 统一的用户状态变更逻辑 (激活/冻结/提权/降级)
     * @param operation DB操作函数 (Mybatis Mapper的方法引用)
     * @param needLogout 是否需要强制登出
     */
    private ResultVO<Void> handleStatusChange(String username, Function<String, Integer> operation, boolean needLogout) {
        return executeSafely(() -> {
            Integer rows = operation.apply(username);
            boolean success = (rows != null && rows == 1);

            if (success && needLogout) {
                ResultVO<Void> logoutRes = logout(username);
                success = "0".equals(logoutRes.getCode());
            }

            return success ? Result.success() : Result.failure(BaseErrorCode.SERVICE_ERROR);
        });
    }

    /**
     * 统一的用户列表获取逻辑
     */
    private ResultVO<List<UserManagerDTO>> getUsersInternal(String role, ErrorCode emptyError) {
        return executeSafely(() -> {
            List<UserManagerDTO> list = userMybatisRepos.getUserListByRole(role);
            if (isEmpty(list)) {
                return Result.failure(emptyError, null);
            }
            list.forEach(u -> u.setPassword(null));
            return Result.success(list);
        });
    }

    private void sendSecretKeyEmail(String email, String username, String secretKey) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(email);
        message.setSubject("您在 LetucOJ 的密钥 (Your Secret Key for LetucOJ)");
        message.setText(String.format("尊敬的 %s,\n\n您请求的密钥如下:\n\n密钥 (Secret Key): %s\n\n请使用此密钥继续操作。\n\n祝好,\nLetucOJ 团队", username, secretKey));
        mailSender.send(message);
    }

    // --- 工具函数 ---

    private boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    private String safeString(Object... objs) {
        for (Object o : objs) {
            if (o != null) return o.toString().trim();
        }
        return null;
    }
}