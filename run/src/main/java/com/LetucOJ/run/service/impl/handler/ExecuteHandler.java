package com.LetucOJ.run.service.impl.handler;

import com.LetucOJ.common.result.ResultVO;
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
    public static final String CONTAINER_PATH = "/submission"; // 占位符，表示宿主机目录

    public ExecuteHandler() {}

    @Override
    public void setNextHandler(Handler nextHandler) {
        this.nextHandler = nextHandler;
    }

    @Override
    public ResultVO handle(List<String> inputFiles, int boxid, String language) {

        ResultVO finalResult = null;
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

            // 2. 等待进程完成（带超时）
            boolean finished = proc.waitFor(EXECUTION_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            if (!finished) {
                // 如果超时，强制杀死容器
                System.out.println("[ExecuteHandler] Execution timeout, attempting to kill container: " + containerName);
                try {
                    new ProcessBuilder("docker", "kill", containerName).start().waitFor();
                } catch (Exception e) {
                    System.err.println("[ExecuteHandler] Failed to kill container: " + e.getMessage());
                }
                finalResult = new ResultVO((byte) 4, null, "Runtime error: 运行超时 (超过 " + EXECUTION_TIMEOUT_SECONDS + " 秒)");
                return finalResult;
            }

            // 3. 读取脚本的退出状态码
            Path statusFile = Path.of(RunPath.getStatusPath(boxid));
            if (!Files.exists(statusFile)) {
                System.err.println("[ExecuteHandler] status.txt file not found after execution.");
                finalResult = new ResultVO((byte) 5, null, "Unknown error: 状态文件丢失");
                return finalResult;
            }
            String status = Files.readString(statusFile).trim();

            int exitCodeFromScript = 5; // Default to unknown error
            try {
                if (!status.isEmpty()) {
                    exitCodeFromScript = Integer.parseInt(status);
                } else {
                    System.err.println("[ExecuteHandler] status.txt file is empty.");
                }
            } catch (NumberFormatException nfe) {
                System.err.println("[ExecuteHandler] Invalid content in status.txt: '" + status + "'");
            }

            // 4. 根据脚本的退出码处理结果
            switch (exitCodeFromScript) {
                case 0 -> { // 正常完成
                    List<String> results = new ArrayList<>();
                    // 读取所有输出文件
                    for (int i = 1; i <= Integer.parseInt(numTestCases); i++) {
                        Path outTxt = Path.of(RunPath.getOutputPath(boxid, i));
                        String answer = Files.exists(outTxt)
                                ? Files.readString(outTxt).trim()
                                : "error: output file missing";
                        results.add(answer);
                    }
                    System.out.println("[ExecuteHandler] Execution successful for boxid " + boxid);
                    finalResult = new ResultVO((byte) 0, results, "Execution successful");
                }
                case 1 -> { // 脚本内部的错误
                    finalResult = new ResultVO((byte) 5, null, "Internal Script Error");
                }
                case 2 -> { // 编译错误
                    Path compileErr = Path.of(RunPath.getCompilePath(boxid));
                    String errMsg = Files.exists(compileErr)
                            ? Files.readString(compileErr)
                            : "Compilation error, but compile.txt missing";
                    System.out.println("[ExecuteHandler] Compile Error: " + errMsg);
                    finalResult = new ResultVO((byte) 2, null, "Compile error: " + errMsg);
                }
                case 3 -> { // 运行时错误
                    Path errTxt = Path.of(RunPath.getErrorPath(boxid));
                    String errMsg = Files.exists(errTxt)
                            ? Files.readString(errTxt)
                            : "Runtime error, but err.txt missing";
                    System.out.println("[ExecuteHandler] Runtime Error: " + errMsg);
                    finalResult = new ResultVO((byte) 3, null, "Runtime error: " + errMsg);
                }
                case 4 -> { // 脚本内部的超时
                    System.out.println("[ExecuteHandler] Runtime timeout (exit 4) from script.");
                    finalResult = new ResultVO((byte) 4, null, "Runtime error: 运行超时（超过 5 秒）");
                }
                case 5 -> { // 脚本内部的异常
                    finalResult = new ResultVO((byte) 5, null, "Execution failed: " + Files.readString(Path.of(RunPath.getErrorPath(boxid))));
                }
                default -> {
                    finalResult = new ResultVO((byte) 5, null, "Unknown error from script.");
                }
            }

            return finalResult;

        } catch (Exception e) {
            e.printStackTrace();
            finalResult = new ResultVO((byte) 5, null, "Unexpected error: " + e.getMessage());
            return finalResult;
        } finally {
            // 在 finally 块中调用清理方法，确保目录被删除
            forceCleanup(boxid);
        }
    }

    /**
     * 清理宿主机上被挂载的目录
     * @param boxid 盒子ID
     */
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
