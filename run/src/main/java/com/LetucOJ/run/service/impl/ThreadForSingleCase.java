package com.LetucOJ.run.service.impl;

import com.LetucOJ.run.tool.path;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;

public class ThreadForSingleCase extends Thread {

    path path;
    int index;
    public boolean error = false;
    public String answer;
    private CountDownLatch latch;

    public ThreadForSingleCase(path path, int index, CountDownLatch latch) {
        this.path = path;
        this.index = index;
        this.latch = latch;
    }

    @Override
    public void run() {

        ProcessBuilder pb = new ProcessBuilder(path.getExecutablePath());
        pb.redirectInput(new File(path.getInputPath(index)));
        pb.redirectOutput(new File(path.getOutputPath(index)));
        pb.redirectError(new File(path.getErrorPath()));
        pb.directory(new File(path.getRoot()));
        pb.environment().put("ASAN_OPTIONS", "detect_leaks=1");
        Process process = null;
        try {
            process = pb.start();
            if (process.waitFor(1000, java.util.concurrent.TimeUnit.MILLISECONDS)) {
                if (process.exitValue() != 0) {
                    // 打印出错信息
                    String errorMessage = new String(java.nio.file.Files.readAllBytes(Paths.get(path.getErrorPath())));
                    error = true;
                    answer = errorMessage.trim();
                } else {
                    // 打印输出结果
                    String outputMessage = new String(java.nio.file.Files.readAllBytes(Paths.get(path.getOutputPath(index))));
                    answer = outputMessage.trim();
                }
            } else {
                error = true;
                answer = "Execution timed out for case " + index;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (process != null) {
                process.destroy();
            }
            latch.countDown();
        }
    }
}
