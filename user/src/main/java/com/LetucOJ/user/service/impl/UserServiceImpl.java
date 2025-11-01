package com.LetucOJ.user.service.impl;

import com.LetucOJ.common.cache.Redis;
import com.LetucOJ.common.log.Logger;
import com.LetucOJ.common.log.LogLevel;
import com.LetucOJ.common.log.Type;
import com.LetucOJ.common.oss.MinioRepos;
import com.LetucOJ.common.result.Result;
import com.LetucOJ.common.result.ResultVO;
import com.LetucOJ.common.result.errorcode.BaseErrorCode;
import com.LetucOJ.common.result.errorcode.UserErrorCode;
import com.LetucOJ.user.model.JwtInfoVO;
import com.LetucOJ.user.model.RegisterRequestDTO;
import com.LetucOJ.user.model.UserInfoDTO;
import com.LetucOJ.user.model.UserManagerDTO;
import com.LetucOJ.user.repos.UserMybatisRepos;
import com.LetucOJ.user.service.UserService;
import com.LetucOJ.user.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMybatisRepos userMybatisRepos;

    @Autowired
    private MinioRepos minioRepos;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public ResultVO register(RegisterRequestDTO dto) {
        String username = dto.getUsername();
        String cnname = dto.getCnname();
        String rawPwd = dto.getPassword();

        Pattern usernamePattern = Pattern.compile("^[A-Za-z]{2,10}\\d{12}$");
        if (!usernamePattern.matcher(username).matches()) {
            return Result.failure(UserErrorCode.PARAM_FORMAT_ERROR);
        }

        if (cnname == null || cnname.isEmpty() || cnname.length() > 20) {
            return Result.failure(UserErrorCode.PARAM_FORMAT_ERROR);
        }

        Pattern passwordPattern = Pattern.compile("^[A-Za-z0-9]{6,20}$");
        if (!passwordPattern.matcher(rawPwd).matches()) {
            return Result.failure(UserErrorCode.PARAM_FORMAT_ERROR);
        }
        System.out.println(username);
        UserInfoDTO existingUser = userMybatisRepos.getUserFullInfo(username);

        if (existingUser != null) {
            return Result.failure(UserErrorCode.USERNAME_ALREADY_EXISTS);
        }

        String encodedPwd = PasswordUtil.encrypt(rawPwd);
        UserManagerDTO userManagerDTO = new UserManagerDTO(username, cnname, encodedPwd, "USER", 1);

        try {
            Integer result = userMybatisRepos.saveUserInfo(userManagerDTO);

            if (!result.equals(1)) {
                System.out.println(result);
                return Result.failure(UserErrorCode.REGISTER_FAILED);
            }
            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure(UserErrorCode.REGISTER_FAILED);
        }
    }

    @Override
    public ResultVO login(RegisterRequestDTO dto) {
        String username = dto.getUsername();
        String rawPwd = dto.getPassword();
        try {
            long version = System.currentTimeMillis();
            UserManagerDTO userManagerDTO = userMybatisRepos.getPasswordByUserName(username);
            if (userManagerDTO == null || !PasswordUtil.matches(rawPwd, userManagerDTO.getPassword())) {
                return Result.failure(UserErrorCode.NAME_OR_CODE_WRONG);
            } else if (userManagerDTO.getStatus() == 0) {
                return Result.failure(UserErrorCode.NOT_ENABLE);
            }
            String cnname = userManagerDTO.getCnname();
            System.out.println(cnname);
            JwtInfoVO jwtInfoVO = new JwtInfoVO(username, cnname, userManagerDTO.getRole(), version);
            return Result.success(jwtInfoVO);
        } catch (Exception e) {
            return Result.failure(BaseErrorCode.SERVICE_ERROR);
        }
    }

    @Override
    public ResultVO refreshToken(String userName) {
        try {
            long version = System.currentTimeMillis();
            UserManagerDTO userManagerDTO = userMybatisRepos.getPasswordByUserName(userName);
            if (userManagerDTO == null ) {
                return Result.failure(UserErrorCode.NAME_OR_CODE_WRONG);
            } else if (userManagerDTO.getStatus() == 0) {
                return Result.failure(UserErrorCode.NOT_ENABLE);
            }
            String cnname = userManagerDTO.getCnname();
            System.out.println(cnname);
            JwtInfoVO jwtInfoVO = new JwtInfoVO(userName, cnname, userManagerDTO.getRole(), version);
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
            ResultVO res = logout(userName);
            if (rows != null && rows == 1 && res.getCode().equals("0")) {
                return Result.success();
            } else {
                return Result.failure(BaseErrorCode.SERVICE_ERROR);
            }
        } catch (Exception e) {
            return Result.failure(BaseErrorCode.SERVICE_ERROR);
        }
    }

    @Override
    public ResultVO logout(String username) {
        try {
            // redisTemplate.opsForValue().set("black:" + username, "1", Duration.ofSeconds(ttl));
            Redis.mapPutDuration("black:" + username, "0", 7 * 24 * 60 * 60);
            return Result.success();
        } catch (Exception e) {
            return Result.failure(BaseErrorCode.SERVICE_ERROR);
        }
    }

    @Override
    public ResultVO getAllUsers() {
        try {
            List<UserManagerDTO> list = userMybatisRepos.getUserListByRole("USER");
            if (list == null || list.isEmpty()) {
                return Result.failure(UserErrorCode.NO_USER);
            }
            for (UserManagerDTO user : list) {
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
            List<UserManagerDTO> list = userMybatisRepos.getUserListByRole("MANAGER");
            if (list == null || list.isEmpty()) {
                return Result.failure(UserErrorCode.NO_MANAGER);
            }
            for (UserManagerDTO user : list) {
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
            ResultVO res = logout(userName);
            if (rows != null && rows == 1 && res.getCode().equals("0")) {
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
        List<Map<String, Object>> corrects = userMybatisRepos.listCorrect();
        List<UserManagerDTO> users = userMybatisRepos.getUserListByRole("USER");

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

        Map<String, UserManagerDTO> userMap = users.stream()
                .collect(Collectors.toMap(UserManagerDTO::getUserName, Function.identity()));

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
            UserManagerDTO u = userMap.get(userName);
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

    @Override
    public ResultVO updateUserFullInfo(UserInfoDTO userInfoDTO) {
        if (userInfoDTO == null || userInfoDTO.getUserName() == null) {
            return Result.failure(UserErrorCode.EMPTY_PARAMETER);
        }
        Integer res = userMybatisRepos.updateUserInfo(userInfoDTO);
        if (res == null || res != 1) {
            return Result.failure(BaseErrorCode.SERVICE_ERROR);
        }
        return Result.success();
    }

    @Override
    public ResultVO getBackground(String username) {
        String bucketName = "letucoj";
        String objectName = "user/" + username + "/background.txt";
        try {
            byte[] data = minioRepos.getFile(bucketName, objectName);
            return Result.success(data);
        } catch (Exception e) {
            return Result.failure(UserErrorCode.NO_BACKGROUND);
        }
    }

    @Override
    public ResultVO getUserFullInfo(String username) {
        System.out.println(username);
        if (username == null) {
            return Result.failure(UserErrorCode.EMPTY_PARAMETER);
        }
        UserInfoDTO userInfoDTO = userMybatisRepos.getUserFullInfo(username);
        if (userInfoDTO == null) {
            return Result.failure(UserErrorCode.NO_USER);
        }
        return Result.success(userInfoDTO);
    }

    @Override
    public ResultVO updateBackground(String username, byte[] data) {
        String bucketName = "letucoj";
        String objectName = "user/" + username + "/background.txt";
        try {
            minioRepos.addFile(bucketName, objectName, data);
            return Result.success();
        } catch (Exception e) {
            return Result.failure(BaseErrorCode.SERVICE_ERROR);
        }
    }

    @Override
    public ResultVO getHeadPortrait(String username) {
        String bucketName = "letucoj";
        String objectName = "user/" + username + "/headPortrait.txt";
        try {
            byte[] data = minioRepos.getFile(bucketName, objectName);
            return Result.success(data);
        } catch (Exception e) {
            return Result.failure(UserErrorCode.NO_HEADPORTRAIT);
        }
    }

    @Override
    public ResultVO updateHeadPortrait(String username, byte[] data) {
        String bucketName = "letucoj";
        String objectName = "user/" + username + "/headPortrait.txt";
        try {
            minioRepos.addFile(bucketName, objectName, data);
            return Result.success();
        } catch (Exception e) {
            return Result.failure(BaseErrorCode.SERVICE_ERROR);
        }
    }

    @Override
    public ResultVO getHeatmap(String username, int year) {
        String bucketName = "letucoj";
        String objectName = "user/" + username + "/heatmap/" + year + ".json";
        if (!minioRepos.isObjectExist(bucketName, objectName)) {
            return Result.failure(UserErrorCode.NO_HEATMAP);
        }
        byte[] data = minioRepos.getFile(bucketName, objectName);
        return Result.success(data);
    }

    @Override
    public ResultVO getSecretKey(String username) {
        UserInfoDTO userInfoDTO = userMybatisRepos.getUserFullInfo(username);
        if (userInfoDTO == null) {
            return Result.failure(UserErrorCode.NO_USER);
        }
        String email = userInfoDTO.getEmail();
        if (email == null || email.isEmpty()) {
            return Result.failure(UserErrorCode.NO_EMAIL);
        }
        String secretKey = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail); // 设置发件人
            message.setTo(email);       // 设置收件人
            message.setSubject("您在 LetucOJ 的密钥 (Your Secret Key for LetucOJ)"); // 设置邮件主题

            // 构造邮件正文内容
            String messageText = "尊敬的 " + username + ",\n\n"
                    + "您请求的密钥如下:\n\n"
                    + "密钥 (Secret Key): " + secretKey + "\n\n"
                    + "请使用此密钥继续操作。如果您没有请求此密钥，请忽略本邮件。\n\n"
                    + "祝好,\n"
                    + "LetucOJ 团队";
            message.setText(messageText);

            // 发送邮件
            mailSender.send(message);

            // 在这里，您通常需要将 secretKey 与用户关联并进行缓存或保存
            // 例如: userSecretKeyCache.put(username, secretKey);

            Redis.mapPutDuration(username, secretKey, 120); // 2分钟过期

            return Result.success("密钥已成功发送至您的邮箱: " + email);

        } catch (MailException e) {
            // 捕获邮件发送异常，返回通用错误
            e.printStackTrace(); // 在服务器日志中打印详细错误，方便调试
            return Result.failure(BaseErrorCode.SERVICE_ERROR, "邮件发送失败，请稍后重试。");
        }
    }

    @Override
    public ResultVO changePassword(String username, String secretKey, String newPassword) {
        String cachedKey = Redis.mapGet(username);
        if (cachedKey == null || !cachedKey.equals(secretKey)) {
            Logger.log(Type.SERVER, LogLevel.WARN, "Password change failed for user " + username + ": invalid secret key.");
            return Result.failure(UserErrorCode.SECRET_KEY_INVALID);
        }

//        Pattern passwordPattern = Pattern.compile("^[A-Za-z0-9]{6,20}$");
//        if (!passwordPattern.matcher(newPassword).matches()) {
//            return Result.failure(UserErrorCode.PARAM_FORMAT_ERROR);
//        }

        String encodedPwd = PasswordUtil.encrypt(newPassword);
        try {
            Integer res = userMybatisRepos.updatePassword(username, encodedPwd);
            if (res == null || res != 1) {
                return Result.failure(BaseErrorCode.SERVICE_ERROR);
            }
            return Result.success();
        } catch (Exception e) {
            return Result.failure(BaseErrorCode.SERVICE_ERROR);
        }
    }

}
