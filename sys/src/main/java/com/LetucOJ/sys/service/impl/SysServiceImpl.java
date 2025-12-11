package com.LetucOJ.sys.service.impl;

import com.LetucOJ.common.log.LogLevel;
import com.LetucOJ.common.log.Logger;
import com.LetucOJ.common.log.Type;
import com.LetucOJ.common.oss.MinioRepos;
import com.LetucOJ.common.result.Result;
import com.LetucOJ.common.result.ResultVO;
import com.LetucOJ.common.result.errorcode.BaseErrorCode;
import com.LetucOJ.sys.service.SysService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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

    @Override
    public ResultVO<Void> refreshSql() {
        String objectName = "backup_" + LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".sql";

        try {
            Path temp = Files.createTempFile("dump", ".sql");

            List<String> cmd = List.of(
                    "mysqldump",
                    "-h" + host,
                    "-P" + port,
                    "-u" + user,
                    "-p" + password,
                    "--databases", "letucoj",
                    "--result-file", temp.toString()
            );

            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);
            Process proc = pb.start();
            int exitCode = proc.waitFor();
            if (exitCode != 0) {
                try (InputStream in = proc.getInputStream()) {
                    String err = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                    Logger.log(Type.SERVER, LogLevel.ERROR, err);
                    return Result.failure(BaseErrorCode.SERVICE_ERROR);
                }
            }

            byte[] data = Files.readAllBytes(temp);
            String bucketName = "mysql";
            minioRepos.addFile(bucketName, objectName, data);

            Files.deleteIfExists(temp);

            return Result.success();
        } catch (Exception e) {
            Logger.log(Type.SERVER, LogLevel.ERROR, e.getMessage());
            return Result.failure(BaseErrorCode.SERVICE_ERROR);
        }
    }
}
