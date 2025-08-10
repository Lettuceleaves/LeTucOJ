package com.LetucOJ.sys.service;

import com.LetucOJ.sys.client.RunClientFactory;
import com.LetucOJ.sys.model.ConfigDTO;
import com.LetucOJ.sys.model.ResultVO;
import com.LetucOJ.sys.repos.SysMybatisRepos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class Monitor implements CommandLineRunner {

    @Autowired
    private SysMybatisRepos sysMybatisRepos;

    @Autowired
    private RunClientFactory runClientFactory;

    private String name = "sys";
    Map<String, ContainerPool> containerPools = new ConcurrentHashMap<>();
    ConfigDTO configDTO;

    @Override
    public void run(String... args) throws UnknownHostException {
        ResultVO resultVO = monitorInit();
        System.out.println(resultVO.toJson());
    }

    private ResultVO monitorInit() throws UnknownHostException {
//        name = java.net.InetAddress.getLocalHost().getHostName();
        configDTO = sysMybatisRepos.getConfig(name);
        if (configDTO == null || configDTO.getConfigList() == null || configDTO.getConfigList().isEmpty()) {
            return new ResultVO(5, null, "Monitor initialization failed: No configuration found for " + name);
        }

        System.out.println(configDTO);

        for (List<String> cfg : configDTO.getConfigList()) {
            String lang = cfg.get(0);
            System.out.println(cfg.get(0) + ": " + cfg.get(1));
            String cap = cfg.get(1);
            if (cap == null || cap.isEmpty()) {
                System.out.println("Not need language: " + lang);
                continue;
            }
            int capacity = Integer.parseInt(cap);
            if (capacity <= 0) {
                System.out.println("Not need language: " + lang);
                continue;
            }
            ContainerPool containerPool = new ContainerPool(runClientFactory, lang);
            containerPools.put(lang, containerPool);
            System.out.println(lang + ": " + containerPool);
        }
        return new ResultVO(0, null, "Monitor initialized successfully");
    }

    public ResultVO submit(List<String> files, String lang) {
        ContainerPool containerPool = containerPools.get(lang);

        if (containerPool == null) {
            return new ResultVO(5, null, "Unsupported language: " + lang);
        }

        try {
            return containerPool.submit(files);
        } catch (Exception e) {
            return new ResultVO(5, null, "Error submitting files: " + e.getMessage());
        }
    }
}

/*

package com.LetucOJ.sys.service;

import com.LetucOJ.sys.model.ConfigDTO;
import com.LetucOJ.sys.model.ResultVO;
import com.LetucOJ.sys.repos.SysMybatisRepos;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.lang.Thread.sleep;

@Slf4j
@Component
public class Monitor implements CommandLineRunner {

    @Autowired
    private SysMybatisRepos sysMybatisRepos;

    private String name;
    private ConfigDTO config;
    private Map<String, RunMissionQueue> queues;

    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final ReentrantReadWriteLock rw = new ReentrantReadWriteLock();

    @Override
    public void run(String... args) throws Exception {
        System.out.println("start run");
        ResultVO init = initMethod();
        if (init.getStatus() != 0) {
            System.out.println(init.toJson());
            return;
        }
        startInfiniteLoop();
        scan(5);
    }

    private ResultVO initMethod() {
        try {
            System.out.println("start init");
//            name = java.net.InetAddress.getLocalHost().getHostName();
            name = "test";
            config = sysMybatisRepos.getConfig(name);
            queues = new ConcurrentHashMap<>();
            for (List<String> cfg : config.getConfigList()) {
                String langName = cfg.get(0);
                int capacity = Integer.parseInt(cfg.get(1));
                String timeStamp = String.valueOf(System.currentTimeMillis());
                RunMissionQueue missionQueue = new RunMissionQueue(langName, timeStamp, capacity, capacity);
                queues.put(langName, missionQueue);
            }
            System.out.println("end init");
            return new ResultVO((byte) 0, "System Monitor Initialized", null);
        } catch (Exception e) {
            return new ResultVO((byte) 5, null, "Monitor Initialization Failed: " + e.getMessage());
        }
    }

    private void startInfiniteLoop() {
        System.out.println("start loop");
        isRunning.set(true);
        ThreadPoolTaskExecutor workerExecutor;
        workerExecutor = new ThreadPoolTaskExecutor();
        workerExecutor.setCorePoolSize(1);
        workerExecutor.setMaxPoolSize(1);
        workerExecutor.setQueueCapacity(1);
        workerExecutor.initialize();

        workerExecutor.submit(() -> {
            while (isRunning.get() && !Thread.currentThread().isInterrupted()) {
                try {
                    monitorWork();
                } catch (Throwable t) {
                    log.error("Worker loop error", t);
                    return;
                }
            }
        });
    }

    private void scan(int periodSeconds) {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(1);
        taskScheduler.initialize();
        taskScheduler.scheduleAtFixedRate(() -> {
            scanWork();
        }, Duration.ofSeconds(periodSeconds));
    }

    private void monitorWork() {
    }

    private void scanWork() {
        config = sysMybatisRepos.getConfig(name);
    }
}

*/