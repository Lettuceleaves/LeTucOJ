package com.LetucOJ.common.trace.filter;

import cn.hutool.core.util.IdUtil;
import com.LetucOJ.common.trace.TraceContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.annotation.*;
import java.util.Map;

/**
 * Servlet 环境下的 Trace 配置类
 * <p>
 * 修复说明：
 * 1. 解决了 setImportMetadata 中调用 @Bean 方法导致的 BeanCurrentlyInCreationException。
 * 2. 保持了对 WebFlux 环境的兼容性（通过内部类隔离）。
 */
@Configuration
public class ServletTraceFilter implements ImportAware {

    // 1. 定义成员变量暂存注解配置
    private boolean isGateway = false;

    /**
     * 用于在配置类之间传递注解参数的简单 Bean
     */
    @Data
    public static class TraceProperties {
        private boolean isGateway = false;
    }

    @Bean
    public TraceProperties traceProperties() {
        // 3. 在创建 Bean 时使用成员变量的值
        TraceProperties properties = new TraceProperties();
        properties.setGateway(this.isGateway);
        return properties;
    }

    @Override
    public void setImportMetadata(AnnotationMetadata importMetadata) {
        Map<String, Object> attributes = importMetadata.getAnnotationAttributes(EnableTrace.class.getName());
        if (attributes != null) {
            // 2. 这里只负责读取配置并保存到成员变量，绝对不要调用 @Bean 方法
            this.isGateway = (Boolean) attributes.get("isGateway");
        }
    }

    /**
     * 真正的 MVC 配置逻辑被隔离在这个静态内部类中。
     * 只有当类路径下存在 DispatcherServlet 时（即 Web 环境），Spring 才会尝试加载这个类。
     * Gateway 环境下条件不满足，直接跳过，不会触发 NoClassDefFoundError。
     */
    @Configuration
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnClass(name = "org.springframework.web.servlet.DispatcherServlet")
    @Slf4j
    @AllArgsConstructor
    protected static class ServletTraceConfiguration implements WebMvcConfigurer {

        private TraceProperties traceProperties;

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(new HandlerInterceptor() {
                @Override
                public boolean preHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) {
                    String traceId = request.getHeader(TraceContext.TRACE_HEADER);
                    boolean isGateway = traceProperties.isGateway();

                    if (traceId == null || traceId.isEmpty()) {
                        if (!isGateway) {
                            log.warn("【Trace告警】Web服务检测到缺失 TraceId 的直接访问或链路断裂! URI: {}, Method: {}, IP: {}",
                                    request.getRequestURI(),
                                    request.getMethod(),
                                    request.getRemoteAddr());
                        }
                        traceId = IdUtil.getSnowflake().nextIdStr();
                    }

                    TraceContext.setTraceId(traceId);
                    return true;
                }

                @Override
                public void afterCompletion(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler, Exception ex) {
                    TraceContext.clear();
                }
            });
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @Documented
    @Import(ServletTraceFilter.class)
    public @interface EnableTrace {
        boolean isGateway();
    }
}