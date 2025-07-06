package com.LetucOJ.practice.tool;

import java.nio.charset.StandardCharsets;

public class Docker {

    // 根据容器名run从docker中获取到ip
    public static String getRunIP () {
        // 在powershell中输入docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' run，返回字符串
        String command = "docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' run";
        try {
            Process process = new ProcessBuilder("powershell.exe", "-Command", command).start();
            process.waitFor();
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()));
            return reader.readLine();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public static void main(String[] args) {
        String runIP = getRunIP();
        if (runIP != null) {
            String command = "ping " + runIP;
            try {
                Process process = Runtime.getRuntime().exec(command);
                process.waitFor();
                java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Run container not found.");
        }
    }

}
