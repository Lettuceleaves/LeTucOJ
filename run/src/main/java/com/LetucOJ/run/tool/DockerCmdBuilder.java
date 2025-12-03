package com.LetucOJ.run.tool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DockerCmdBuilder {
    private final List<String> cmd = new ArrayList<>();

    public DockerCmdBuilder() {
        cmd.add("docker");
        cmd.add("run");
        cmd.add("--rm"); // 默认加上 rm
    }

    public DockerCmdBuilder name(String name) {
        cmd.add("--name");
        cmd.add(name);
        return this;
    }

    public DockerCmdBuilder resourceLimit(int memoryMb, String cpuCount) {
        cmd.add("--memory");
        cmd.add(memoryMb + "m");
        cmd.add("--memory-swap");
        cmd.add(memoryMb + "m");
        cmd.add("--cpus");
        cmd.add(cpuCount);
        return this;
    }

    public DockerCmdBuilder volume(String hostPath, String containerPath) {
        cmd.add("-v");
        cmd.add(hostPath + ":" + containerPath);
        return this;
    }

    public DockerCmdBuilder network(String net) {
        cmd.add("--network");
        cmd.add(net);
        return this;
    }

    // ... 其他参数

    public DockerCmdBuilder imageAndArgs(String image, String... args) {
        cmd.add(image);
        Collections.addAll(cmd, args);
        return this;
    }

    public ProcessBuilder build() {
        return new ProcessBuilder(cmd);
    }
}