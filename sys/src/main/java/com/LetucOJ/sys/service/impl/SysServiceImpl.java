package com.LetucOJ.sys.service.impl;

import com.LetucOJ.common.log.LogLevel;
import com.LetucOJ.common.log.Logger;
import com.LetucOJ.common.log.Type;
import com.LetucOJ.common.oss.MinioRepos;
import com.LetucOJ.common.result.Result;
import com.LetucOJ.common.result.ResultVO;
import com.LetucOJ.common.result.errorcode.BaseErrorCode;
import com.LetucOJ.common.trace.TraceContext;
import com.LetucOJ.sys.service.SysService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class SysServiceImpl implements SysService {

    @Value("${mysql.host}")      private String host;
    @Value("${mysql.port}")      private int port;
    @Value("${mysql.user}")      private String user;
    @Value("${mysql.password}")  private String password;

    private final MinioRepos minioRepos;

    public SysServiceImpl(MinioRepos minioRepos) {
        this.minioRepos = minioRepos;
    }

    @Override
    public ResultVO<byte[]> getDoc() {
        try {
            String bucketName = "letucoj";
            String objectName = "doc.md";
            byte[] file = minioRepos.getFile(bucketName, objectName);
            return Result.success(file);
        } catch (Exception e) {
            return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
        }
    }

    @Override
    public ResultVO<Void> updateDoc(byte[] doc) {
        try {
            String bucketName = "letucoj";
            String objectName = "doc.md";
            minioRepos.addFile(bucketName, objectName, doc);
            return Result.success();
        } catch (Exception e) {
            return Result.failure(BaseErrorCode.SERVICE_ERROR);
        }
    }

    /**
     * 修正后：异步执行 + 跳过SSL验证
     */
    @Override
    public ResultVO<Void> refreshSql() {
        // 生成文件名
        String objectName = "backup_" + LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".sql";
        String taskId = TraceContext.getTraceId();

        // 【关键点1】使用 CompletableFuture 开启异步线程
        // 这样接口会立即返回 Success，而备份逻辑在后台慢慢跑
        CompletableFuture.runAsync(() -> {
            TraceContext.setTraceId(taskId);
            Path temp = null;
            try {
                // 创建临时文件
                temp = Files.createTempFile("dump", ".sql");

                Process proc = getProcess(temp);

                // 等待进程结束
                int exitCode = proc.waitFor();

                if (exitCode != 0) {
                    // 读取错误信息并记录日志
                    try (InputStream in = proc.getInputStream()) {
                        String err = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                        // 注意：这里不能 return Result.failure 了，因为主线程已经返回了
                        // 只能打 Log
                        Logger.log(Type.SERVER, LogLevel.ERROR, "Mysqldump failed: " + err);
                    }
                } else {
                    // 成功：上传到 MinIO
                    byte[] data = Files.readAllBytes(temp);
                    String bucketName = "mysql";
                    minioRepos.addFile(bucketName, objectName, data);
                    Logger.log(Type.SERVER, LogLevel.INFO, "Database backup success: " + objectName);
                }

            } catch (Exception e) {
                Logger.log(Type.SERVER, LogLevel.ERROR, "Async Backup Exception: " + e.getMessage());
            } finally {
                // 清理临时文件
                try {
                    if (temp != null) {
                        Files.deleteIfExists(temp);
                    }
                } catch (Exception ignored) {}
                TraceContext.clear();
            }
        });

        // 异步任务已提交，立即告知前端“请求已接收”
        return Result.success();
    }

    @NotNull
    private Process getProcess(Path temp) throws IOException {
        List<String> cmd = List.of(
                "mysqldump",
                "-h" + host,
                "-P" + port,
                "-u" + user,
                "-p" + password,
                "--ssl-mode=DISABLED",
                "--databases", "letucoj",
                "--result-file", temp.toString()
        );

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true); // 将错误流合并到标准输出
        return pb.start();
    }
}