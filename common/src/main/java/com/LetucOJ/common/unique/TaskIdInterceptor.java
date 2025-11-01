package com.LetucOJ.common.unique;

import cn.hutool.core.util.IdUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Component
@ConditionalOnClass(WebMvcConfigurer.class)
public class TaskIdInterceptor implements HandlerInterceptor {

    @Value("${snowflake.workerId:1}")
    private long workerId;

    @Value("${snowflake.datacenterId:1}")
    private long datacenterId;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String taskId = request.getHeader("X-Task-Id");
        if (taskId == null || taskId.isEmpty()) {
            taskId = generateTaskId();
        }
        TaskIdContext.setTaskId(taskId);
        response.setHeader("X-Task-Id", taskId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        TaskIdContext.clear();
    }

    private String generateTaskId() {
        return "task_" + IdUtil.getSnowflake(workerId, datacenterId).nextId();
    }
}