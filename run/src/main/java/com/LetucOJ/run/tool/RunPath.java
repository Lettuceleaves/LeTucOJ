package com.LetucOJ.run.tool;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RunPath {
    public static String work_dir = "/app/user/";
    private static final int MAX_BOX = 1000; // 上限，开区间
    private static final AtomicInteger COUNTER = new AtomicInteger(0);
    private static final ThreadLocal<Integer> BOX_HOLDER = new ThreadLocal<>();

    public static String getBoxDir(int boxId) {
        return work_dir + boxId;
    }

    static public String getInputPath(int boxid, int index) {
        return work_dir + boxid + "/in_" + index + ".txt";
    }

    static public String getOutputPath(int boxid, int index) {
        return work_dir + boxid + "/ou_" + index + ".txt";
    }

    static public String getCompilePath(int boxid) {
        return work_dir + boxid + "/compile.txt";
    }

    static public String getErrorPath(int boxid) {
        return work_dir + boxid + "/err.txt";
    }

    static public String getStatusPath(int boxid) {
        return work_dir + boxid + "/status.txt";
    }

    static public String userCodePath(int boxid, String language) {
        return work_dir + boxid + "/prog." + getSuffix(language);
    }

    public static int borrowBoxId() {
        Integer id = BOX_HOLDER.get();
        if (id != null) {
            return id;
        }
        int next;
        do {
            next = COUNTER.getAndUpdate(v -> (v + 1) % MAX_BOX);
        } while (next == -1);
        BOX_HOLDER.set(next);
        return next;
    }

    public static void returnBoxId() {
        BOX_HOLDER.remove();
    }

    public static String getSuffix(String language) {
        switch (language) {
            case "c" -> {
                return "c";
            }
            case "cpp" -> {
                return "cpp";
            }
            case "java" -> {
                return "java";
            }
            case "py" -> {
                return "py";
            }
            case "node" -> {
                return "js";
            }
            default -> {
                return "txt"; // 默认后缀
            }
        }
    }
}
