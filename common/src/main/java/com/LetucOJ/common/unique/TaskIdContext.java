package com.LetucOJ.common.unique;

import com.alibaba.ttl.TransmittableThreadLocal;
import org.springframework.stereotype.Component;

@Component
public class TaskIdContext {

    private static final TransmittableThreadLocal<String> TASK_ID = new TransmittableThreadLocal<>();

    public static void setTaskId(String taskId) {
        TASK_ID.set(taskId);
    }

    public static String getTaskId() {
        return TASK_ID.get();
    }

    public static void clear() {
        TASK_ID.remove();
    }
}
