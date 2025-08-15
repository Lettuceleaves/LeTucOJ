package com.LetucOJ.sys.service;

import com.LetucOJ.sys.client.RunClientFactory;
import com.LetucOJ.sys.model.ConfigDTO;
import com.LetucOJ.sys.model.ResultVO;
import com.LetucOJ.sys.repos.SysMybatisRepos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
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
        if (!waitForMysqlReady(30)) {
            return new ResultVO(5, null, "Monitor initialization failed: MySQL not ready");
        }
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

    private boolean waitForMysqlReady(int totalSeconds) {
        int sleep = 1; // 每次检查间隔，秒
        for (int i = 0; i < totalSeconds; i += sleep) {
            try (Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://mysql:3306/letucoj?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
                    "letuc", "430103535")) {
                conn.createStatement().execute("SELECT 1");
                return true;
            } catch (SQLException e) {
                try { Thread.sleep(sleep * 1000L); } catch (InterruptedException ignore) {}
            }
        }
        return false;
    }
}