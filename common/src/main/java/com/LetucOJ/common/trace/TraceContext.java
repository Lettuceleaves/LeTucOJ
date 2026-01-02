package com.LetucOJ.common.trace;

import com.alibaba.ttl.TransmittableThreadLocal;
import org.slf4j.MDC;

/**
 * 核心 Trace 上下文管理器
 * <p>
 * 作用：在当前线程中持有 TraceId。
 * 兼容性：主要服务于 Servlet (Web) 和 阻塞式调用。
 * 对于 WebFlux，TraceId 主要流转于 Reactor Context 中，
 * 但为了兼容日志打印，通常也会尝试同步到 MDC。
 */
public class TraceContext {

    // TraceId Header 名称
    public static final String TRACE_HEADER = "X-Trace-Id";

    // MDC Key
    public static final String MDC_KEY = "traceId";

    // 使用 TransmittableThreadLocal 确保在使用线程池时 ID 不丢失
    // 如果没有引入 alibaba ttl 依赖，可以使用 new InheritableThreadLocal<>() 或 new ThreadLocal<>()
    private static final ThreadLocal<String> TRACE_ID_HOLDER = new TransmittableThreadLocal<>();

    /**
     * 获取当前 TraceId
     */
    public static String getTraceId() {
        return TRACE_ID_HOLDER.get();
    }

    /**
     * 设置 TraceId
     * 同时写入 ThreadLocal 和 MDC (用于日志)
     */
    public static void setTraceId(String traceId) {
        if (traceId != null) {
            TRACE_ID_HOLDER.set(traceId);
            MDC.put(MDC_KEY, traceId);
        }
    }

    /**
     * 清理上下文
     */
    public static void clear() {
        TRACE_ID_HOLDER.remove();
        MDC.remove(MDC_KEY);
    }
}