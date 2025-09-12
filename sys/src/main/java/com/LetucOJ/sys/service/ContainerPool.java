package com.LetucOJ.sys.service;

import com.LetucOJ.sys.client.RunClient;
import com.LetucOJ.sys.client.RunClientFactory;
import com.LetucOJ.sys.model.ResultVO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ContainerPool {

    private RunClientFactory factory;

    private String lang;
    private final int maxContainers = 10;
    private final int maxTaskQueue = 10;
    private final int warmupBurst = 2;

    private final BlockingQueue<Integer> readyIds = new LinkedBlockingQueue<>(maxContainers);
    private final BlockingQueue<Integer> freeIds = new ArrayBlockingQueue<>(maxContainers);

    private final ThreadPoolExecutor taskExecutor;

    private final ScheduledExecutorService warmupScheduler;
    private final ExecutorService initExecutor;

    public ContainerPool(RunClientFactory factory, String lang) {
        this.lang = lang;
        this.factory = factory;
        if (factory == null) {
            throw new IllegalArgumentException("RunClientFactory cannot be null");
        }
        for (int i = 0; i < maxContainers; i++) {
            freeIds.add(i);
        }

        this.taskExecutor = new ThreadPoolExecutor(
                Math.min(maxContainers, 4),
                maxContainers,
                30L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(maxTaskQueue),
                r -> {
                    Thread t = new Thread(r, "task-worker-" + UUID.randomUUID());
                    t.setDaemon(true);
                    return t;
                },
                new ThreadPoolExecutor.AbortPolicy()
        );

        this.warmupScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "container-warmup");
            t.setDaemon(true);
            return t;
        });

        this.initExecutor = Executors.newFixedThreadPool(
                Math.min(warmupBurst, maxContainers),
                r -> {
                    Thread t = new Thread(r, "container-init-" + UUID.randomUUID());
                    t.setDaemon(true);
                    return t;
                }
        );

        warmupScheduler.scheduleWithFixedDelay(this::ensureWarmPool, 0, 500, TimeUnit.MILLISECONDS);
    }

    public ResultVO submit(List<String> userTaskPayload) {
        try {
            Future<ResultVO> future = taskExecutor.submit(() -> {
                Integer id = readyIds.take();
                ResultVO result = executeTaskOnContainer(id, userTaskPayload);
                freeIds.put(id);
                return result;
            });

            return future.get();

        } catch (RejectedExecutionException ex) {
            return new ResultVO(1, null, "Task queue is full, please try again later");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new ResultVO(1, null, "Task interrupted");
        } catch (ExecutionException e) {
            return new ResultVO(1, null, "Task failed: " + e.getCause().getMessage());
        }
    }

    private void ensureWarmPool() {
        try {
            for (int i = 0; i < warmupBurst; i++) {
                Integer id = freeIds.poll();
                if (id == null) return;
                initExecutor.submit(() -> {
                    try {
                        containerInit(id);
                        readyIds.put(id);
                    } catch (InterruptedException ie) {
                        System.out.println(ie.getMessage());
                        Thread.currentThread().interrupt();
                        freeIds.offer(id);
                    } catch (Exception e) {
                        System.out.println(e);
                        freeIds.offer(id);
                    }
                });
            }
        } catch (Exception ignored) {

        }
    }

    private void containerInit(int id) throws Exception {
        String name = "run" + lang + id;
        destroyRunContainer(name);
        int port = 1010 + id;

        System.out.println("Create Docker Container..." + id);
        createRunContainer(name, "run:latest", port, port);

        // 等待容器服务就绪
        boolean healthy = isContainerHealthy(name, port, 10000); // 最多等10秒
        if (!healthy) {
            destroyRunContainer(name);
            throw new RuntimeException("Container " + name + " failed to become healthy");
        }

        System.out.println("Container " + name + " is ready.");
    }

    private boolean isContainerHealthy(String host, int port, int timeoutMillis) {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < timeoutMillis) {
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(host, port), 1000); // 1s timeout
                return true;
            } catch (IOException e) {
                // 未就绪，等待重试
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
        }
        return false;
    }


    private ResultVO executeTaskOnContainer(int id, List<String> payload) throws IOException, InterruptedException {
        System.out.println("Executing on container " + id + " with payload: " + payload);
        String name = "run" + lang + id;
        RunClient client = factory.forContainer(name, 1010 + id);
        ResultVO resultVO = client.run(payload);
        System.out.println("Task done on container " + id);
        return resultVO;
    }

    /** 可选：关闭资源 */
    public void shutdown() {
        warmupScheduler.shutdownNow();
        initExecutor.shutdownNow();
        taskExecutor.shutdownNow();
    }

    public void createRunContainer(String name, String image, int hostPort, int containerPort) throws IOException, InterruptedException {
        List<String> cmd = new ArrayList<>();
        cmd.add("docker");
        cmd.add("run");
        cmd.add("-d");
        cmd.add("--name");
        cmd.add(name);
        cmd.add("--network"); cmd.add("docker-compose_default");
        cmd.add("-p"); cmd.add(hostPort + ":" + containerPort);
        cmd.add("-e");
        cmd.add("SERVER_PORT=" + containerPort);
        cmd.add(image);

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        System.out.println("Creating run container " + name);

        int exitCode = process.waitFor();
        if (exitCode == 0) {
            System.out.println("Container " + name + " created successfully.");
        } else {
            System.err.println("Failed to create container " + name);
        }
    }

    public void destroyRunContainer(String name) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("docker", "rm", "-f", name);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("Destroy output: " + line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode == 0) {
            System.out.println("Container " + name + " destroyed successfully.");
        } else {
            System.out.println("Failed to destroy container " + name);
        }
    }
}
