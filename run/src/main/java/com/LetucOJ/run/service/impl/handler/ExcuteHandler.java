package com.LetucOJ.run.service.impl.handler;

import com.LetucOJ.run.model.vo.ResultVO;
import com.LetucOJ.run.service.Handler;
import com.LetucOJ.run.service.impl.ThreadForSingleCase;
import com.LetucOJ.run.tool.RunPath;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;

import static java.lang.Thread.sleep;

public class ExcuteHandler implements Handler {
    Handler nextHandler;

    @Override
    public void setNextHandler(Handler nextHandler) {
        this.nextHandler = nextHandler;
    }

    @Override
    public ResultVO handle(List<String> inputFiles) {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(10, 10, 0L, java.util.concurrent.TimeUnit.MILLISECONDS, new java.util.concurrent.LinkedBlockingQueue<>());
        ThreadForSingleCase[] threads = new ThreadForSingleCase[inputFiles.size()];
        CountDownLatch latch = new CountDownLatch(inputFiles.size() - 1);

        for (int i = 1; i < inputFiles.size(); i++) {
            threads[i] = new ThreadForSingleCase(i, latch);
            executor.execute(threads[i]);
        }
        // 等待所有线程执行完成
        List<String> results = new java.util.ArrayList<>();

        boolean completed = false;
        try {
            completed = latch.await(10000, java.util.concurrent.TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            return new ResultVO((byte) 5, null, "Unexpected interrupted: " + e.getMessage());
        }
        if (!completed) {
            for (int i = 1; i < inputFiles.size(); i++) {
                threads[i].interrupt();
            }
            return new ResultVO((byte) 4, null, "Time out and interrupted");
        } else {
            for (int i = 1; i < inputFiles.size(); i++) {
                if (threads[i].error) {
                    return new ResultVO((byte) 3, null, "Runtime error: " + threads[i].answer);
                } else {
                    results.add(threads[i].answer);
                }
            }
        }
        return new ResultVO((byte) 0, results, "Execution completed");
    }
}
