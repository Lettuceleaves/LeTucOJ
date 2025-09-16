package com.LetucOJ.sys.service.impl;

import com.LetucOJ.sys.model.ResultVO;
import com.LetucOJ.sys.repos.MinioRepos;
import com.LetucOJ.sys.service.SysService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private MinioRepos minioRepos;

    @Override
    public ResultVO getDoc() {
        try {
            String result = minioRepos.get("doc.md", "letucoj");
            if (result == null || result.isEmpty()) {
                return new ResultVO(5, null, "sys/getDoc: Document not found in MinIO");
            } else {
                return new ResultVO(0, new String(result.getBytes(), StandardCharsets.UTF_8), null);
            }
        } catch (Exception e) {
            return new ResultVO(5, null, "sys/getDoc: " + e.getMessage());
        }
    }

    @Override
    public ResultVO updateDoc(byte[] doc) {
        try {
            String result = minioRepos.put("doc.md", "letucoj", doc, "text/plain");
            if (result == null || result.isEmpty()) {
                return new ResultVO(0, null, null);
            } else {
                return new ResultVO(5, null, result);
            }
        } catch (Exception e) {
            return new ResultVO(5, null, "sys/updateDoc: " + e.getMessage());
        }
    }

    @Override
    public ResultVO refreshSql() {
        String objectName = "backup_" + LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".sql";

        try {
            // 1. 生成临时文件
            Path temp = Files.createTempFile("dump", ".sql");

            // 2. 构建 mysqldump 命令
            List<String> cmd = List.of(
                    "mysqldump",
                    "-h" + host,
                    "-P" + String.valueOf(port),
                    "-u" + user,
                    "-p" + password,
                    "--databases", "letucoj",
                    "--result-file", temp.toString()
            );

            // 3. 执行
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);
            Process proc = pb.start();
            int exitCode = proc.waitFor();
            if (exitCode != 0) {
                try (InputStream in = proc.getInputStream()) {
                    String err = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                    return new ResultVO(5, null, "mysqldump failed: " + err);
                }
            }

            // 4. 上传到 MinIO
            byte[] data = Files.readAllBytes(temp);
            String err = minioRepos.put(objectName, "mysql", data, "application/sql");
            if (err != null) {
                return new ResultVO(5, null, "Upload to MinIO failed: " + err);
            }

            // 5. 清理临时文件
            Files.deleteIfExists(temp);

            return new ResultVO(0, objectName, "OK");
        } catch (Exception e) {
            return new ResultVO(5, null, "sys/refreshSql: " + e.getMessage());
        }
    }
}
