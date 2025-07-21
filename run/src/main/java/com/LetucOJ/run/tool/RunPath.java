package com.LetucOJ.run.tool;

import lombok.Data;
import org.springframework.stereotype.Component;

@Component
public class RunPath {
    public static String root;
    public static String os;

    static public void pathInit() {
        // 获取操作系统类型
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            os = "Windows";
            root = "E:\\projects\\LetucOJ\\test";
        } else if (osName.contains("linux")) {
            os = "Linux";
            root = "/app/localRun/";
        } else {
            throw new RuntimeException("Unsupported OS: " + osName);
        }
    }

    static public String getInputPath(int i) {
        if (os.equals("Windows")) {
            return getTestSpaceRootPath() + "\\input_" + i + ".txt";
        } else {
            return root + "/input_" + i + ".txt";
        }
    }

    static public String getOutputPath(int i) {
        if (os.equals("Windows")) {
            return getTestSpaceRootPath() + "\\output_" + i + ".txt";
        } else {
            return root + "/output_" + i + ".txt";
        }
    }

    static public String getErrorPath() {
        if (os.equals("Windows")) {
            return getTestSpaceRootPath() + "\\error" + ".txt";
        } else {
            return root + "/error" + ".txt";
        }
    }

    static public String getUserCodePath() {
        if (os.equals("Windows")) {
            return getTestCodeRootPath() + "\\userCode.c";
        } else {
            return root + "/userCode.c";
        }
    }

    static public String getExecutablePath() {
        if (os.equals("Windows")) {
            return getTestSpaceRootPath() + "\\userCode.exe";
        } else {
            return root + "/userCode";
        }
    }

    static public String getTestSpaceRootPath() {
        if (os.equals("Windows")) {
            return root + "\\testSpace";
        } else {
            return root;
        }
    }

    static public String getTestCodeRootPath() {
        if (os.equals("Windows")) {
            return root + "\\testSpace";
        } else {
            return "Error : Unsupported OS (" + os + ")  in method getTestCodeRootPath()";
        }
    }
}
