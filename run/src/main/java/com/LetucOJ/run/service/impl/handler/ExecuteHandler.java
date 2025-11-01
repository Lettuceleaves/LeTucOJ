package com.LetucOJ.run.service.impl.handler;

import com.LetucOJ.common.log.LogLevel;
import com.LetucOJ.common.log.Logger;
import com.LetucOJ.common.log.Type;
import com.LetucOJ.common.result.Result;
import com.LetucOJ.common.result.ResultVO;
import com.LetucOJ.common.result.errorcode.BaseErrorCode;
import com.LetucOJ.common.result.errorcode.RunErrorCode;
import com.LetucOJ.run.service.Handler;
import com.LetucOJ.run.tool.RunPath;
import lombok.Data;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Data
@Service
public class ExecuteHandler implements Handler {
    private Handler nextHandler;
    private static final long EXECUTION_TIMEOUT_SECONDS = 30; // 容器执行超时时间
    public static final String HOST_DIR = System.getenv("HOST_DIR"); // 占位符，表示宿主机目录
    public static final String CONTAINER_PATH = "/submission";

    public ExecuteHandler() {}

    @Override
    public void setNextHandler(Handler nextHandler) {
        this.nextHandler = nextHandler;
    }

    @Override
    public ResultVO handle(List<String> inputFiles, int boxid, String language, String qname, byte[] config) {

        String containerName = "box-" + language + "-" + boxid + "-" + System.currentTimeMillis();
        String imageName = "run_" + RunPath.getSuffix(language);
        String numTestCases = String.valueOf(inputFiles.size() - 1);

        try {
            // --- 核心修改开始 ---

            // 1. 使用 SnakeYAML 解析配置文件的字节数组
            Yaml yaml = new Yaml();
            Map<String, Object> configMap = yaml.load(new ByteArrayInputStream(config));

            // 2. 导航到语言专属的默认配置
            Map<String, Object> languageDefaults = (Map<String, Object>) configMap.get("language_defaults");
            if (languageDefaults == null || !languageDefaults.containsKey(language)) {
                Logger.log(Type.CLIENT, LogLevel.ERROR, "Language defaults missing or language not found: " + language + "language_defaults");
                return Result.failure(RunErrorCode.VALIDATE_ERROR);
            }

            Map<String, Object> langConfig = (Map<String, Object>) languageDefaults.get(language);

            // 3. 提取资源限制
            // 内存限制是必须的
            Integer memoryLimitMb = (Integer) langConfig.get("memory_limit_mb");
            if (memoryLimitMb == null) {
                Logger.log(Type.CLIENT, LogLevel.ERROR, "Language defaults missing or language not found: " + language + " memory_limit_mb");
                return Result.failure(RunErrorCode.VALIDATE_ERROR);
            }

            // CPU 核心数是可选的，如果未配置，则默认为 "0.5"
            Object cpusObj = langConfig.get("cpus");
            String cpusLimit = (cpusObj != null) ? cpusObj.toString() : "0.5";

            // --- 核心修改结束 ---

            ProcessBuilder pb = new ProcessBuilder(
                    "docker", "run", "--rm",
                    "--name", containerName,
                    "--network", "none",
                    // 使用从配置文件中动态读取的值
                    "--memory", memoryLimitMb + "m",
                    "--memory-swap", memoryLimitMb + "m",
                    "--cpus", cpusLimit,
                    "--ulimit", "fsize=512000",
                    "-v", HOST_DIR + "/" + boxid + ":" + CONTAINER_PATH,
                    imageName,
                    numTestCases,
                    language
            );


            String cmdLine = String.join(" ", pb.command());
            System.out.println("[ExecuteHandler] 正在运行的 Docker 指令: " + cmdLine);

            Process proc = pb.start();

            boolean finished = proc.waitFor(EXECUTION_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            if (!finished) {
                System.out.println("[ExecuteHandler] Execution timeout, attempting to kill container: " + containerName);
                try {
                    new ProcessBuilder("docker", "kill", containerName).start().waitFor();
                } catch (Exception e) {
                    System.err.println("[ExecuteHandler] Failed to kill container: " + e.getMessage());
                }
                return Result.failure(BaseErrorCode.OUT_OF_TIME);
            }

            Path statusFile = Path.of(RunPath.getStatusPath(boxid));
            if (!Files.exists(statusFile)) {
                System.err.println("[ExecuteHandler] status.txt file not found after execution.");
                return Result.failure(BaseErrorCode.SERVICE_ERROR);
            }
            String status = Files.readString(statusFile).trim();

            int exitCodeFromScript = 5;
            try {
                if (!status.isEmpty()) {
                    exitCodeFromScript = Integer.parseInt(status);
                } else {
                    System.err.println("[ExecuteHandler] status.txt file is empty.");
                }
            } catch (NumberFormatException nfe) {
                System.err.println("[ExecuteHandler] Invalid content in status.txt: '" + status + "'");
            }

            switch (exitCodeFromScript) {
                case 0: { // 正常完成
                    List<String> results = new ArrayList<>();
                    for (int i = 1; i <= Integer.parseInt(numTestCases); i++) {
                        Path outTxt = Path.of(RunPath.getOutputPath(boxid, i));
                        String answer = Files.exists(outTxt)
                                ? Files.readString(outTxt).trim()
                                : "message: output file missing";
                        results.add(answer);
                    }
                    Path errTxt = Path.of(RunPath.getErrorPath(boxid));
                    String errMsg = Files.exists(errTxt)
                            ? Files.readString(errTxt)
                            : "Runtime message, but err.txt missing";
                    System.out.println("[ExecuteHandler] Execution memory top point: " + errMsg);
                    return Result.success(results);
                }
                case 1: { // 脚本内部的错误
                    return Result.failure(BaseErrorCode.SERVICE_ERROR);
                }
                case 2: { // 编译错误
                    Path compileErr = Path.of(RunPath.getCompilePath(boxid));
                    String errMsg = Files.exists(compileErr)
                            ? Files.readString(compileErr)
                            : "Compilation message, but compile.txt missing";
                    System.out.println("[ExecuteHandler] Compile Error: " + errMsg);
                    return Result.failure(BaseErrorCode.COMPILE_ERROR, errMsg.substring(0, Math.min(1000, errMsg.length())));
                }
                case 3: { // 运行时错误
                    Path errTxt = Path.of(RunPath.getErrorPath(boxid));
                    String errMsg = Files.exists(errTxt)
                            ? Files.readString(errTxt)
                            : "Runtime message, but err.txt missing";
                    return Result.failure(BaseErrorCode.RUNTIME_ERROR, errMsg.substring(0, Math.min(1000, errMsg.length())));
                }
                case 4: { // 超时
                    System.out.println("[ExecuteHandler] Runtime timeout (exit 4) from script.");
                    String errMsg = "Execution exceeded time limit";
                    return Result.failure(BaseErrorCode.OUT_OF_TIME, errMsg);
                }
                case 5: { // 脚本内部的异常
                    return Result.failure(BaseErrorCode.SERVICE_ERROR);
                }
                default: {
                    return Result.failure(BaseErrorCode.SERVICE_ERROR);
                }
            }
        } catch (ClassCastException cce) {
            cce.printStackTrace();
            Logger.log(Type.CLIENT, LogLevel.ERROR, cce.getMessage());
            return Result.failure(RunErrorCode.VALIDATE_ERROR, "Invalid data type in config.yaml for language '" + language + "'. Check your configuration.");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure(BaseErrorCode.SERVICE_ERROR);
        } finally {
//            forceCleanup(boxid);
        }
    }

    private void forceCleanup(int boxid) {
        Path pathToDelete = Path.of(RunPath.getBoxDir(boxid));
        System.out.println("[ExecuteHandler] Final cleanup for boxid: " + boxid + ", path: " + pathToDelete);
        try {
            if (Files.exists(pathToDelete)) {
                try (Stream<Path> walk = Files.walk(pathToDelete)) {
                    walk.sorted(Comparator.reverseOrder())
                            .map(Path::toFile)
                            .forEach(File::delete);
                }
            }
            System.out.println("[ExecuteHandler] Directory " + pathToDelete + " cleaned up successfully.");
        } catch (IOException e) {
            System.err.println("[ExecuteHandler] Failed to force cleanup for boxid " + boxid + ": " + e.getMessage());
        }
    }
}
