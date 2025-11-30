package com.LetucOJ.run.service.impl.handler;

import com.LetucOJ.common.log.LogLevel;
import com.LetucOJ.common.log.Logger;
import com.LetucOJ.common.log.Type;
import com.LetucOJ.common.result.Result;
import com.LetucOJ.common.result.ResultVO;
import com.LetucOJ.common.result.errorcode.BaseErrorCode;
import com.LetucOJ.common.result.errorcode.RunErrorCode;
import com.LetucOJ.run.model.TestCaseDTO;
import com.LetucOJ.run.model.TestCaseVO;
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
    public static final String CONTAINER_PATH = "/submission"; // 容器文件挂载位置

    public ExecuteHandler() {}

    @Override
    public void setNextHandler(Handler nextHandler) {
        this.nextHandler = nextHandler;
    }

    @Override
    public ResultVO<TestCaseVO> handle(TestCaseDTO testCaseDTO, int boxid, byte[] config) {

        String containerName = "box-" + testCaseDTO.getLanguage() + "-" + boxid + "-" + System.currentTimeMillis(); // 本次测试使用的沙盒名
        String imageName = "run_" + RunPath.getSuffix(testCaseDTO.getLanguage()); // 本次测试使用的镜像名
        String numTestCases = String.valueOf(testCaseDTO.getCaseFiles().size()); // 本次测试的测试用例数量

        try {

            // 解析配置文件的字节数组
            Yaml yaml = new Yaml();
            Map<String, Object> configMap = yaml.load(new ByteArrayInputStream(config));

            // 语言专属的默认配置
            Map<String, Object> languageDefaults = (Map<String, Object>) configMap.get("language_defaults");
            if (languageDefaults == null || !languageDefaults.containsKey(testCaseDTO.getLanguage())) {
                Logger.log(Type.CLIENT, LogLevel.ERROR, "Language defaults missing or language not found: " + testCaseDTO.getLanguage() + "language_defaults");
                return Result.failure(RunErrorCode.VALIDATE_ERROR, null);
            }

            Map<String, Object> langConfig = (Map<String, Object>) languageDefaults.get(testCaseDTO.getLanguage());

            // 资源限制
            Integer memoryLimitMb = (Integer) langConfig.get("memory_limit_mb");
            if (memoryLimitMb == null) {
                Logger.log(Type.CLIENT, LogLevel.ERROR, "Language defaults missing or language not found: " + testCaseDTO.getLanguage() + " memory_limit_mb");
                return Result.failure(RunErrorCode.VALIDATE_ERROR, null);
            }

            // CPU 核心数 默认为 "0.5"
            Object cpusObj = langConfig.get("cpus");
            String cpusLimit = (cpusObj != null) ? cpusObj.toString() : "0.5";

            // 创建执行任务
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
                    testCaseDTO.getLanguage()
            );


            String cmdLine = String.join(" ", pb.command());
            Logger.log(Type.SERVER, LogLevel.INFO, "Executing command: " + cmdLine);

            Process proc = pb.start();

            boolean finished = proc.waitFor(EXECUTION_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            // 超过系统限制时长
            if (!finished) {
                Logger.log(Type.EXTERNAL, LogLevel.ERROR, "Execution timeout, attempting to kill container: " + containerName);
                try {
                    new ProcessBuilder("docker", "kill", containerName).start().waitFor();
                } catch (Exception e) {
                    Logger.log(Type.EXTERNAL, LogLevel.ERROR, "Killing container: " + containerName + e.getMessage());
                }
                return Result.failure(BaseErrorCode.OUT_OF_TIME, null);
            }

            Path statusFile = Path.of(RunPath.getStatusPath(boxid));

            // 结果状态文件缺失
            if (!Files.exists(statusFile)) {
                Logger.log(Type.EXTERNAL, LogLevel.ERROR, "Status file not found after execution.");
                return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
            }
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
                    Logger.log(Type.EXTERNAL, LogLevel.INFO, "Memory top point: " + errMsg);
                    return Result.success(new TestCaseVO(results, null));
                }
                case 1: { // 脚本内部的错误
                    return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
                }
                case 2: { // 编译错误
                    Path compileErr = Path.of(RunPath.getCompilePath(boxid));
                    String errMsg = Files.exists(compileErr)
                            ? Files.readString(compileErr)
                            : "Compilation message, but compile.txt missing";
                    Logger.log(Type.EXTERNAL, LogLevel.ERROR, "Compilation error: " + errMsg);
                    return Result.failure(BaseErrorCode.COMPILE_ERROR, new TestCaseVO(null, errMsg.substring(0, Math.min(1000, errMsg.length()))));
                }
                case 3: { // 运行时错误
                    Path errTxt = Path.of(RunPath.getErrorPath(boxid));
                    String errMsg = Files.exists(errTxt)
                            ? Files.readString(errTxt)
                            : "Runtime message, but err.txt missing";
                    return Result.failure(BaseErrorCode.RUNTIME_ERROR, new TestCaseVO(null, errMsg.substring(0, Math.min(1000, errMsg.length()))));
                }
                case 4: { // 超时
                    Logger.log(Type.EXTERNAL, LogLevel.ERROR, "Runtime timeout from script.");
                    String errMsg = "Execution exceeded time limit";
                    return Result.failure(BaseErrorCode.OUT_OF_TIME, new TestCaseVO(null, errMsg));
                }
                case 5: { // 脚本内部的异常
                    Logger.log(Type.EXTERNAL, LogLevel.ERROR, "Container ErrorCode 5: " + containerName);
                    return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
                }
                default: {
                    Logger.log(Type.EXTERNAL, LogLevel.ERROR, "Container ErrorCode != 5: " + containerName);
                    return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
                }
            }
        } catch (ClassCastException cce) {
            Logger.log(Type.SERVER, LogLevel.ERROR, cce.getMessage());
            return Result.failure(RunErrorCode.VALIDATE_ERROR, new TestCaseVO(null, "Invalid data type in config.yaml for language '" + testCaseDTO.getLanguage() + "'. Check your configuration."));
        } catch (Exception e) {
            Logger.log(Type.SERVER, LogLevel.ERROR, e.getMessage());
            return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
        } finally {
            forceCleanup(boxid);
        }
    }

    private void forceCleanup(int boxid) {
        Path pathToDelete = Path.of(RunPath.getBoxDir(boxid));
        try {
            if (Files.exists(pathToDelete)) {
                try (Stream<Path> walk = Files.walk(pathToDelete)) {
                    walk.sorted(Comparator.reverseOrder())
                            .map(Path::toFile)
                            .forEach(File::delete);
                }
            }
            Logger.log(Type.SERVER, LogLevel.INFO, "Cleanup boxid: " + boxid);
        } catch (IOException e) {
            Logger.log(Type.SERVER, LogLevel.ERROR, "Failed to force cleanup for boxid " + boxid + ": " + e.getMessage());
        }
    }
}
