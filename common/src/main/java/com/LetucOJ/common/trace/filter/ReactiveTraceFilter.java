package com.LetucOJ.common.trace.filter;

import cn.hutool.core.util.IdUtil;
import com.LetucOJ.common.log.LogLevel;
import com.LetucOJ.common.log.Logger;
import com.LetucOJ.common.log.Type;
import com.LetucOJ.common.trace.TraceContext;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.lang.annotation.*;
import java.util.Map;

/**
 * WebFlux 环境下的 Trace 过滤器 (策略实现 A)
 * 场景：Gateway, Reactive Microservices
 * <p>
 * 作用：
 * 1. 在网关：作为链路起点，使用 Hutool 生成雪花算法 ID。
 * 2. 在非网关 WebFlux 服务：检查 ID 是否缺失，缺失则视为异常访问并告警。
 * <p>
 * 使用说明：
 * 在 WebFlux 服务的启动类或配置类上添加注解：
 * <pre>
 * // 普通服务（默认）：
 * &#64;ReactiveTraceFilter.EnableTrace
 * * // 网关/入口服务：
 * &#64;ReactiveTraceFilter.EnableTrace(isGateway = true)
 * </pre>
 */
@Slf4j
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@Order(Ordered.HIGHEST_PRECEDENCE) // 确保优先级最高
public class ReactiveTraceFilter implements WebFilter, ImportAware {

    private boolean isGateway = false;

    /**
     * 实现 ImportAware 接口
     * 自动回调此方法，读取 @EnableTrace 注解中的属性值
     */
    @Override
    public void setImportMetadata(AnnotationMetadata importMetadata) {
        Map<String, Object> attributes = importMetadata.getAnnotationAttributes(EnableTrace.class.getName());
        if (attributes != null) {
            this.isGateway = (Boolean) attributes.get("isGateway");
        }
    }

    /**
     * 定义开启 Trace 的注解 (内部静态注解)
     * 使用 @Import 导入当前配置类，触发 ImportAware 机制
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @Documented
    @Import(ReactiveTraceFilter.class)
    public @interface EnableTrace {
        /**
         * 是否作为流量入口（网关/降级网关）
         * true: 缺失 ID 时自动生成
         * false: 缺失 ID 时记录告警
         */
        boolean isGateway() default false;
    }

    @NotNull
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, @NotNull WebFilterChain chain) {
        // 1. 尝试从 Header 获取 (上游传递)
        String traceId = exchange.getRequest().getHeaders().getFirst(TraceContext.TRACE_HEADER);

        // 2. 如果 ID 缺失
        if (traceId == null || traceId.isEmpty()) {
            if (!isGateway) {
                // 【场景B：非网关】异常流量，记录告警日志
                // 注意：这里使用了你提供的自定义 Logger
                try {
                    Logger.log(Type.SERVER, LogLevel.WARN, "【Trace告警】检测到缺失 TraceId 的直接访问或链路断裂! Path: " + exchange.getRequest().getPath() + ", Method: " + exchange.getRequest().getMethod() + ", IP: " + exchange.getRequest().getRemoteAddress());
                } catch (Exception e) {
                    log.error("记录Trace告警日志失败", e);
                }

                // 兜底策略：为了不影响业务执行，仍然生成一个临时 ID 用于当前服务内部追踪
            }
            traceId = IdUtil.getSnowflake().nextIdStr();
            Logger.log(Type.SERVER, LogLevel.INFO, "generate id: " +  traceId);
        }

        final String finalTraceId = traceId;

        // 3. 传递给下游：将 TraceId 放入 Request Header
        // (即使是下游服务，也可能继续调用更下游，所以需要透传)
        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header(TraceContext.TRACE_HEADER, finalTraceId)
                .build();

        ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();

        // 4. 写入 Reactor Context 和 MDC
        return chain.filter(mutatedExchange)
                .contextWrite(Context.of(TraceContext.MDC_KEY, finalTraceId))
                .doOnEach(signal -> {
                    if (!signal.isOnComplete()) {
                        // 简易 MDC 注入，供日志打印
                        MDC.put(TraceContext.MDC_KEY, finalTraceId);
                    }
                })
                .doFinally(signal -> MDC.remove(TraceContext.MDC_KEY));
    }
}