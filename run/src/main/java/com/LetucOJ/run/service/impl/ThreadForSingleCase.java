//package com.LetucOJ.run.service.impl;
//
//import com.LetucOJ.run.tool.RunPath;
//
//import java.io.*;
//import java.nio.file.*;
//import java.util.Arrays;
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.TimeUnit;
//
//public class ThreadForSingleCase extends Thread {
//
//    private final int index;
//    public  boolean error = false;
//    public  String  answer;
//    private final CountDownLatch latch;
//
//    /* 资源上限 */
//    private static final String CGROUP_ROOT = "/sys/fs/cgroup/oj";
//    private static final int    CPU_MILLICORE = 1000;          // 1 核
//    private static final long   MEMORY_BYTES  = 512 * 1024 * 1024L; // 512 MB
//    private static final int    PIDS_MAX      = 20;
//
//    public ThreadForSingleCase(int index, CountDownLatch latch) {
//        this.index = index;
//        this.latch = latch;
//        setName("SingleCase-" + index);
//    }
//
//    @Override
//    public void run() {
//        try {
//            /* 0. 确保 cgroup 目录存在（服务启动时做一次即可） */
//            prepareCgroup();
//
//            /* 1. 为本次评测创建独立沙箱目录 */
//            String box = RunPath.root + "/box/" + index;
//            Files.createDirectories(Paths.get(box));
//
//            /* 2. 生成隔离脚本 */
//            String isolateScript = writeIsolateScript(box);
//
//            /* 3. 启动命令：unshare -> 隔离脚本 -> 用户程序 */
//            ProcessBuilder pb = new ProcessBuilder(
//                    "unshare",
//                    "--user", "--map-root-user",
//                    "--pid", "--fork",
//                    "--mount", "--cgroup",
//                    isolateScript
//            );
//            pb.directory(new File(box));          // 工作目录无实质作用，脚本里会 pivot_root
//            pb.environment().clear();             // 不给用户任何环境变量
//            pb.environment().put("PATH", "/usr/bin:/bin");
//
//            Process proc = pb.start();
//
//            boolean finished = proc.waitFor(10, TimeUnit.SECONDS);
//            if (!finished) {
//                proc.destroyForcibly();
//                error  = true;
//                answer = "Case " + index + " timed out";
//                return;
//            }
//
//            /* 4. 收集结果 */
//            File outFile = new File(box + "/out/out.txt");
//            File errFile = new File(box + "/out/err.txt");
//
//            if (proc.exitValue() != 0) {
//                error = true;
//                answer = errFile.exists() ? readString(errFile.toPath()).trim()
//                        : "Runtime error (exit " + proc.exitValue() + ")";
//            } else {
//                answer = readString(outFile.toPath()).trim();
//            }
//
//        } catch (Exception e) {
//            error  = true;
//            answer = "Internal error: " + e.getMessage();
//            e.printStackTrace();
//        } finally {
//            latch.countDown();
//            System.out.println("Case " + index + " finished. error=" + error + " answer=" + answer);
//        }
//    }
//
//    /* -------------------- 下面都是工具方法 -------------------- */
//
//    private static void prepareCgroup() throws IOException {
//        Path root = Paths.get(CGROUP_ROOT);
//        if (Files.notExists(root)) {
//            Files.createDirectories(root);
//            Files.write(root.resolve("cpu.max"),     (CPU_MILLICORE + " 100000").getBytes());
//            Files.write(root.resolve("memory.max"), String.valueOf(MEMORY_BYTES).getBytes());
//            Files.write(root.resolve("pids.max"),   String.valueOf(PIDS_MAX).getBytes());
//        }
//    }
//
//    private String writeIsolateScript(String box) throws IOException {
//        String script = String.join("\n",
//                "#!/bin/bash",
//                "set -e",
//                "# 1. 加入 cgroup",
//                "echo $$ > " + CGROUP_ROOT + "/cgroup.procs",
//                "",
//                "# 2. 准备沙箱根",
//                "mkdir -p " + box + "/{old_root,bin,usr,lib,lib64,dev,proc,tmp,in,out,work}",
//                "mount -t tmpfs none " + box,
//                "",
//                "# 3. 只读挂载系统目录",
//                "mount -o ro,bind /bin   " + box + "/bin",
//                "mount -o ro,bind /usr   " + box + "/usr",
//                "mount -o ro,bind /lib   " + box + "/lib 2>/dev/null || true",
//                "mount -o ro,bind /lib64 " + box + "/lib64 2>/dev/null || true",
//                "",
//                "# 4. 基本设备",
//                "mknod -m 666 " + box + "/dev/null c 1 3",
//                "mknod -m 666 " + box + "/dev/zero c 1 5",
//                "",
//                "# 5. proc",
//                "mount -t proc none " + box + "/proc",
//                "",
//                "# 6. 用户程序和输入",
//                "mount -o ro,bind '" + RunPath.getExecutablePath() + "' " + box + "/work/program",
//                "mount -o ro,bind '" + RunPath.getInputPath(index)  + "' " + box + "/in",
//                "",
//                "# 7. 可写输出目录",
//                "mount -t tmpfs none " + box + "/out",
//                "",
//                "# 8. pivot_root 并降级",
//                "cd " + box,
//                "pivot_root . old_root",
//                "exec chroot . /usr/bin/env -i /work/program </in >/out/out.txt 2>/out/err.txt"
//        );
//
//        Path sh = Paths.get(box + "/isolate.sh");
//        Files.write(sh, script.getBytes());
//        sh.toFile().setExecutable(true);
//        return sh.toString();
//    }
//
//    private static String readString(Path p) {
//        try { return new String(Files.readAllBytes(p)); }
//        catch (IOException e) { return ""; }
//    }
//}
