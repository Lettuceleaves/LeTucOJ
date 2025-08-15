package com.LetucOJ.run.tool;

public class Commands {
    public static ProcessBuilder getCompilerProcessBuilder() {
        String osName = System.getProperty("os.name").toLowerCase();

        String compileCommand = "gcc -fsanitize=address \"" + RunPath.getUserCodePath() + "\" -o \"" + RunPath.getExecutablePath() + "\" 2> \"" + RunPath.getErrorPath() + "\"";

        if (osName.contains("win")) {
            // Windows: cmd + shell 重定向
            return new ProcessBuilder(
                    "cmd.exe", "/c",
                    compileCommand
            );
        } else if (osName.contains("linux")) {
            // Linux: sh + shell 重定向
            return new ProcessBuilder(
                    "/bin/sh", "-c",
                    compileCommand
            );
        } else {
            throw new RuntimeException("Unsupported OS: " + osName);
        }
    }
}
