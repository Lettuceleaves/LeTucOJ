package com.LetucOJ.common.trace.interceptor;

import com.LetucOJ.common.trace.TraceContext;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;

/**
 * Feign 拦截器，用于传递 TraceId
 * <p>
 * 解决 Feign 调用时 TraceId 丢失的问题
 */
@Configuration
@ConditionalOnClass(RequestInterceptor.class)
@Slf4j
public class FeignTraceInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        String traceId = TraceContext.getTraceId();
        if (traceId != null && !traceId.isEmpty()) {
            template.header(TraceContext.TRACE_HEADER, traceId);
        }
    }
}
