package com.LetucOJ.gateway.tool;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.util.pattern.PathPatternParser;

@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // ✅ 设置前端地址，必须显式指定
        config.addAllowedOrigin("gateway:7777"); // Vue 项目运行地址（Vite 默认端口）
        // 如有多个地址可改为 addAllowedOriginPattern("*") + setAllowCredentials(false)

        config.addAllowedHeader("*"); // 允许所有请求头
        config.addAllowedMethod("*"); // 允许 GET、POST、PUT 等所有方法
        config.setAllowCredentials(true); // 支持携带 cookie / Authorization 头等凭证
        config.setMaxAge(3600L); // 预检请求有效时间（秒）

        // 注册 CORS 配置
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource(new PathPatternParser());
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}
