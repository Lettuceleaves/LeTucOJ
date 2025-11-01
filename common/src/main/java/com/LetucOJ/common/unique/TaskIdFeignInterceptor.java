package com.LetucOJ.common.unique;

import com.LetucOJ.common.unique.TaskIdContext;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;

// 添加Feign拦截器
@Component
public class TaskIdFeignInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        String taskId = TaskIdContext.getTaskId();
        if (taskId != null) {
            template.header("X-Task-Id", taskId);
        }
    }
}