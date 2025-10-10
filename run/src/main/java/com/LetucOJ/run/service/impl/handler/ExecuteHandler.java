package com.LetucOJ.run.service.impl.handler;

import com.LetucOJ.common.result.Result;
import com.LetucOJ.common.result.ResultVO;
import com.LetucOJ.common.result.errorcode.BaseErrorCode;
import com.LetucOJ.run.service.Handler;
import com.LetucOJ.run.tool.RunPath;
import lombok.Data;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Data
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
    public ResultVO handle(List<String> inputFiles, int boxid, String language) {

        String containerName = "box-" + language + "-" + boxid + "-" + System.currentTimeMillis();
        String imageName = "run_" + RunPath.getSuffix(language);
        String numTestCases = String.valueOf(inputFiles.size() - 1);

        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "docker", "run", "--rm", "--name", containerName,
                    "--network", "none",
                    "--memory", "256m",
                    "--cpus", "0.5",
                    "--ulimit", "fsize=512000",
                    "-v", HOST_DIR + "/" + boxid + ":" + CONTAINER_PATH,
                    imageName,
                    numTestCases
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
                System.err.println("[ExecuteHandler] code.txt file not found after execution.");
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
                case 0 -> { // 正常完成
                    List<String> results = new ArrayList<>();
                    for (int i = 1; i <= Integer.parseInt(numTestCases); i++) {
                        Path outTxt = Path.of(RunPath.getOutputPath(boxid, i));
                        String answer = Files.exists(outTxt)
                                ? Files.readString(outTxt).trim()
                                : "message: output file missing";
                        results.add(answer);
                    }
                    System.out.println("[ExecuteHandler] Execution successful for boxid " + boxid);
                    return Result.success(results);
                }
                case 1 -> { // 脚本内部的错误
                    return Result.failure(BaseErrorCode.SERVICE_ERROR);
                }
                case 2 -> { // 编译错误
                    Path compileErr = Path.of(RunPath.getCompilePath(boxid));
                    String errMsg = Files.exists(compileErr)
                            ? Files.readString(compileErr)
                            : "Compilation message, but compile.txt missing";
                    System.out.println("[ExecuteHandler] Compile Error: " + errMsg);
                    return Result.failure(BaseErrorCode.COMPILE_ERROR, errMsg.substring(0, Math.min(1000, errMsg.length())));
                }
                case 3 -> { // 运行时错误
                    Path errTxt = Path.of(RunPath.getErrorPath(boxid));
                    String errMsg = Files.exists(errTxt)
                            ? Files.readString(errTxt)
                            : "Runtime message, but err.txt missing";
                    return Result.failure(BaseErrorCode.RUNTIME_ERROR, errMsg.substring(0, Math.min(1000, errMsg.length())));
                }
                case 4 -> { // 超时
                    System.out.println("[ExecuteHandler] Runtime timeout (exit 4) from script.");
                    String errMsg = "Execution exceeded time limit";
                    return Result.failure(BaseErrorCode.OUT_OF_TIME, errMsg);
                }
                case 5 -> { // 脚本内部的异常
                    return Result.failure(BaseErrorCode.SERVICE_ERROR);
                }
                default -> {
                    return Result.failure(BaseErrorCode.SERVICE_ERROR);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure(BaseErrorCode.SERVICE_ERROR);
        } finally {
            forceCleanup(boxid);
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
