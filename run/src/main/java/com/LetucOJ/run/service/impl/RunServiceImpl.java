package com.LetucOJ.run.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.FileUtil;
import com.LetucOJ.common.log.LogLevel;
import com.LetucOJ.common.log.Logger;
import com.LetucOJ.common.log.Type;
import com.LetucOJ.common.oss.MinioRepos;
import com.LetucOJ.common.result.Result;
import com.LetucOJ.common.result.ResultVO;
import com.LetucOJ.common.result.errorcode.BaseErrorCode;
import com.LetucOJ.common.result.errorcode.RunErrorCode;
import com.LetucOJ.run.model.TestCaseDTO;
import com.LetucOJ.run.model.TestTaskDTO;
import com.LetucOJ.run.model.TestTaskVO;
import com.LetucOJ.run.service.RunService;
import com.LetucOJ.run.tool.DockerCmdBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Service;
import com.LetucOJ.run.tool.RunPath;
import org.yaml.snakeyaml.Yaml;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Data
@AllArgsConstructor
public class RunServiceImpl implements RunService {

    private MinioRepos minioRepos;

    private static final long EXECUTION_TIMEOUT_SECONDS = 30; // 容器执行超时时间
    public static final String HOST_DIR = System.getenv("HOST_DIR"); // 占位符，表示宿主机目录
    public static final String CONTAINER_PATH = "/submission"; // 容器文件挂载位置

    @Override
    public ResultVO<TestTaskVO> runTestTask(TestTaskDTO testTaskDTO) {
        int boxId = RunPath.borrowBoxId();
        int amount = testTaskDTO.getCaseAmount();
        try {
            // 获取全部文件
            byte[] config = minioRepos.getFile("letucoj", "system_files/problems/" + testTaskDTO.getProblemName() + "/config.yaml");
            List<byte[]> inputs = new ArrayList<>();
            for (int i = 0; i < amount; i++) {
                inputs.add(minioRepos.getFile("letucoj", "system_files/problems/" + testTaskDTO.getProblemName() + "/input/" + i + ".txt"));
            }
            List<byte[]> outputs = new ArrayList<>();
            for (int i = 0; i < amount; i++) {
                outputs.add(minioRepos.getFile("letucoj", "system_files/problems/" + testTaskDTO.getProblemName() + "/output/" + i + ".txt"));
            }

            // 创建沙盒环境
            Path boxDir = Paths.get(RunPath.getBoxDir(boxId));
            Files.createDirectories(boxDir);

            Path codePath = Paths.get(RunPath.userCodePath(boxId, testTaskDTO.getLanguage()));
            Files.write(codePath,
                    testTaskDTO.getCode().getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);

            Path configPath = Paths.get(RunPath.getConfigPath(boxId));
            Files.write(configPath,
                    config,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);

            for (int i = 0; i < amount; i++) {
                Path inputPath = Paths.get(RunPath.getInputPath(boxId, i));
                Files.write(inputPath,
                        inputs.get(i),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING);

                Path outputPath = Paths.get(RunPath.getOutputPath(boxId, i));
                Files.write(outputPath,
                        new byte[0],
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING);
            }

            Path compileErr = Paths.get(RunPath.getCompilePath(boxId));
            Files.write(compileErr, new byte[0],
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);

            Path errFile = Paths.get(RunPath.getErrorPath(boxId));
            Files.write(errFile, new byte[0],
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);

            Path statusFile = Paths.get(RunPath.getStatusPath(boxId));
            Files.write(statusFile, new byte[0],
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);

            // 启动沙盒
            String containerName = "box-" + testTaskDTO.getLanguage() + "-" + boxId + "-" + System.currentTimeMillis();

            // 【修改处】匹配本地 Registry 镜像格式: localhost:5000/run_xx:latest
            String imageName = "localhost:5000/run_" + RunPath.getSuffix(testTaskDTO.getLanguage()) + ":latest";

            try {

                // 解析配置文件的字节数组
                Yaml yaml = new Yaml();
                Map<String, Object> configMap = yaml.load(new ByteArrayInputStream(config));

                // 语言专属的默认配置
                Map<String, Object> languageDefaults = Convert.toMap(String.class, Object.class, configMap.get("language_defaults"));
                if (languageDefaults == null || !languageDefaults.containsKey(testTaskDTO.getLanguage())) {
                    Logger.log(Type.CLIENT, LogLevel.ERROR, "Language defaults missing or language not found: " + testTaskDTO.getLanguage() + "language_defaults");
                    return Result.failure(RunErrorCode.VALIDATE_ERROR, null);
                }

                // 该语言的默认配置
                Map<String, Object> specificLangConfig = Convert.toMap(String.class, Object.class, languageDefaults.get(testTaskDTO.getLanguage()));
                // 内存限制
                Integer memoryLimitMb = Convert.toInt(specificLangConfig.get("memory_limit_mb"));
                if (memoryLimitMb == null) {
                    Logger.log(Type.CLIENT, LogLevel.ERROR, "Language defaults missing or language not found: " + testTaskDTO.getLanguage() + " memory_limit_mb");
                    return Result.failure(RunErrorCode.VALIDATE_ERROR, null);
                }

                // CPU 核心数 默认为 0.5
                Object cpusObj = specificLangConfig.get("cpus");
                String cpusLimit = (cpusObj != null) ? cpusObj.toString() : "0.5";

                // docker指令
                ProcessBuilder pb = new DockerCmdBuilder()
                        .name(containerName)
                        .network("none")
                        .resourceLimit(memoryLimitMb, cpusLimit)
                        .volume(HOST_DIR + "/" + boxId, CONTAINER_PATH)
                        .imageAndArgs(imageName, String.valueOf(amount), testTaskDTO.getLanguage())
                        .build();
                Logger.log(Type.SERVER, LogLevel.INFO, "Executing command: " + String.join(" ", pb.command()));

                // 启动容器
                Process proc = pb.start();

                // 超时限制
                boolean finished = proc.waitFor(EXECUTION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                if (!finished) {
                    Logger.log(Type.EXTERNAL, LogLevel.ERROR, "Execution timeout, attempting to kill container: " + containerName);
                    try {
                        new ProcessBuilder("docker", "kill", containerName).start().waitFor();
                    } catch (Exception e) {
                        Logger.log(Type.EXTERNAL, LogLevel.ERROR, "Killing container: " + containerName + e.getMessage());
                    }
                    return Result.failure(RunErrorCode.OUT_OF_TIME, null);
                }

                // 结果状态文件意外缺失
                if (!Files.exists(statusFile)) {
                    Logger.log(Type.EXTERNAL, LogLevel.ERROR, "Status file not found after execution.");
                    return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
                }

                // 获取沙盒状态值
                String status = Files.readString(statusFile).trim();
                int exitCodeFromScript = 5;
                try {
                    if (!status.isEmpty()) {
                        exitCodeFromScript = Integer.parseInt(status);
                    } else {
                        Logger.log(Type.EXTERNAL, LogLevel.ERROR, "Status is empty.");
                    }
                } catch (NumberFormatException nfe) {
                    Logger.log(Type.EXTERNAL, LogLevel.ERROR, "Invalid content in status.txt: '" + status + "'");
                }

                Path errTxt = Path.of(RunPath.getErrorPath(boxId));
                if (!Files.exists(errTxt)) throw new RuntimeException("Error file not found after execution in: " + boxDir + " name: " + testTaskDTO.getProblemName() + " lang: " + testTaskDTO.getLanguage());
                switch (exitCodeFromScript) {
                    case 0: { // 正常完成
                        String errMsg = Files.readString(errTxt);
                        Logger.log(Type.EXTERNAL, LogLevel.INFO, "Memory top point: " + errMsg);

                        // 对比答案
                        for (int i = 0; i < amount; i++) {
                            Path outTxt = Path.of(RunPath.getOutputPath(boxId, i));
                            if (!Files.exists(outTxt)) throw new RuntimeException("Output file not found after execution in: " + boxDir + " name: " + testTaskDTO.getProblemName() + " lang: " + testTaskDTO.getLanguage());
                            String answer = Files.readString(outTxt);
                            if (!answer.trim().equals(new String(outputs.get(i), StandardCharsets.UTF_8).trim())) {
                                Logger.log(Type.EXTERNAL, LogLevel.INFO, "Expect: " + new String(outputs.get(i)) + "\nFound: " + answer.trim());
                                return Result.failure(RunErrorCode.WRONG_ANSWER, new TestTaskVO(i, "Expect: " + new String(outputs.get(i)) + ", found: " + answer.trim()));
                            }
                        }
                        return Result.success(null);
                    }
                    case 1: { // 脚本内部的错误
                        Logger.log(Type.EXTERNAL, LogLevel.ERROR, "sandbox return 1, inner error question name: " + testTaskDTO.getProblemName() + " lang: " + testTaskDTO.getLanguage());
                        return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
                    }
                    case 2: { // 编译错误
                        String errMsg = Files.exists(compileErr)
                                ? Files.readString(compileErr)
                                : "Compilation message, but compile.txt missing";
                        Logger.log(Type.EXTERNAL, LogLevel.ERROR, "Compilation error: " + errMsg);
                        return Result.failure(RunErrorCode.COMPILE_ERROR, new TestTaskVO(0, errMsg));
                    }
                    case 3: { // 运行时错误
                        String errMsg = Files.readString(errTxt);
                        Logger.log(Type.EXTERNAL, LogLevel.ERROR, "Runtime Error" + errMsg);
                        return Result.failure(RunErrorCode.RUNTIME_ERROR, new TestTaskVO(0, errMsg));
                    }
                    case 4: { // 超时
                        Logger.log(Type.EXTERNAL, LogLevel.ERROR, "Runtime timeout from script.");
                        return Result.failure(RunErrorCode.OUT_OF_TIME, null);
                    }
                    case 5: { // 脚本内部的异常
                        Logger.log(Type.EXTERNAL, LogLevel.ERROR, "Container ErrorCode 5: " + containerName);
                        return Result.failure(RunErrorCode.SERVICE_ERROR, null);
                    }
                    default: {
                        Logger.log(Type.EXTERNAL, LogLevel.ERROR, "Container ErrorCode != 5: " + containerName);
                        return Result.failure(RunErrorCode.SERVICE_ERROR, null);
                    }
                }
            } catch (ClassCastException cce) {
                Logger.log(Type.SERVER, LogLevel.ERROR, cce.getMessage());
                return Result.failure(RunErrorCode.VALIDATE_ERROR, null);
            } catch (Exception e) {
                Logger.log(Type.SERVER, LogLevel.ERROR, e.getMessage());
                return Result.failure(RunErrorCode.SERVICE_ERROR, null);
            } finally {
                forceCleanup(boxId);
            }

        } catch (Exception e) {
            Logger.log(Type.SERVER, LogLevel.ERROR, e.getMessage());
            return Result.failure(RunErrorCode.SERVICE_ERROR, null);
        } finally {
            RunPath.returnBoxId();
        }
    }

    @Override
    public ResultVO<TestTaskVO> runTestCase(TestCaseDTO testCaseDTO) {
        return null;
    }


    private void forceCleanup(int boxId) {
        boolean success = FileUtil.del(RunPath.getBoxDir(boxId));
        if (success) {
            Logger.log(Type.SERVER, LogLevel.INFO, "Cleanup boxId: " + boxId);
        } else {
            Logger.log(Type.SERVER, LogLevel.ERROR, "Failed to cleanup boxId: " + boxId);
        }
    }
}