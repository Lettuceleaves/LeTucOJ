package com.LetucOJ.run.service.impl;

import com.LetucOJ.run.tool.RunPath;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;

public class ThreadForSingleCase extends Thread {

    int index;
    public boolean error = false;
    public String answer;
    private CountDownLatch latch;

    public ThreadForSingleCase(int index, CountDownLatch latch) {
        this.index = index;
        this.latch = latch;
    }

    @Override
    public void run() {

        ProcessBuilder pb = new ProcessBuilder(com.LetucOJ.run.tool.RunPath.getExecutablePath());
        pb.redirectInput(new File(com.LetucOJ.run.tool.RunPath.getInputPath(index)));
        pb.redirectOutput(new File(com.LetucOJ.run.tool.RunPath.getOutputPath(index)));
        pb.redirectError(new File(com.LetucOJ.run.tool.RunPath.getErrorPath()));
        pb.directory(new File(com.LetucOJ.run.tool.RunPath.root));
        pb.environment().put("ASAN_OPTIONS", "detect_leaks=1");
        Process process = null;
        try {
            process = pb.start();
            if (process.waitFor(10000, java.util.concurrent.TimeUnit.MILLISECONDS)) {
                if (process.exitValue() != 0) {
                    // 打印出错信息
                    sleep(50);
                    String errorMessage = new String(Files.readAllBytes(Paths.get(RunPath.getErrorPath())));

                    error = true;
                    answer = errorMessage.trim();
                } else {
                    // 打印输出结果
                    String outputMessage = new String(Files.readAllBytes(Paths.get(RunPath.getOutputPath(index))));
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
                try {
                    sleep(20);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                process.destroy();
            }
            latch.countDown();
            System.out.println("info:" + answer + process.exitValue());
        }
    }
}
