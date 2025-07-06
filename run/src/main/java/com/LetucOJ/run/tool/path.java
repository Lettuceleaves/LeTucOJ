package com.LetucOJ.run.tool;

import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class path {
    String root;
    String os;
    boolean test;

    public void pathInit(boolean test) {
        this.test = test;
        if (test) {
            os = "Windows";
            root = "E:\\projects\\LetucOJ\\test";
        } else {
            os = "Linux";
            root = "/app/localRun/";
        }
    }

    public String getInputPath(int i) {
        if (os.equals("Windows")) {
            return getTestSpaceRootPath() + "\\input_" + i + ".txt";
        } else {
            return root + "/input_" + i + ".txt";
        }
    }

    public String getOutputPath(int i) {
        if (os.equals("Windows")) {
            return getTestSpaceRootPath() + "\\output_" + i + ".txt";
        } else {
            return root + "/output_" + i + ".txt";
        }
    }

    public String getErrorPath() {
        if (os.equals("Windows")) {
            return getTestSpaceRootPath() + "\\error" + ".txt";
        } else {
            return root + "/error" + ".txt";
        }
    }

    public String getUserCodePath() {
        if (os.equals("Windows")) {
            return getTestCodeRootPath() + "\\userCode.c";
        } else {
            return root + "/userCode.c";
        }
    }

    public String getExecutablePath() {
        if (os.equals("Windows")) {
            return getTestSpaceRootPath() + "\\userCode.exe";
        } else {
            return root + "/userCode";
        }
    }

    public String getTestSpaceRootPath() {
        if (os.equals("Windows")) {
            return root + "\\testSpace";
        } else {
            return "Error : Unsupported OS in method getTestSpaceRootPath()";
        }
    }

    public String getTestCodeRootPath() {
        if (os.equals("Windows")) {
            return root + "\\testSpace";
        } else {
            return "Error : Unsupported OS in method getTestSpaceRootPath()";
        }
    }
}
