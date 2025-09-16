package com.LetucOJ.run.tool;

public class Commands {
    public static ProcessBuilder getCompilerProcessBuilder() {
        String osName = System.getProperty("os.name").toLowerCase();

        // 核心：在最后追加 -lm
        String compileCommand = "gcc -fsanitize=address \"" + RunPath.getUserCodePath() +
                "\" -o \"" + RunPath.getExecutablePath() +
                "\" -lm 2> \"" + RunPath.getErrorPath() + "\"";

        if (osName.contains("win")) {
            return new ProcessBuilder("cmd.exe", "/c", compileCommand);
        } else if (osName.contains("linux")) {
            return new ProcessBuilder("/bin/sh", "-c", compileCommand);
        } else {
            throw new RuntimeException("Unsupported OS: " + osName);
        }
    }
}
