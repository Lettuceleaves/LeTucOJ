package com.LetucOJ.gateway.chain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * 网关安全配置类
 * 负责定义 URL 访问权限、关闭 CSRF、以及配置 JWT 过滤器
 */
@Slf4j
@Configuration
@EnableWebFluxSecurity
@EnableMethodSecurity()
@Order(10)
@RequiredArgsConstructor
public class SecurityConfig {

    // =================================================================================
    // 1. 定义白名单路径 (无需登录即可访问)
    // =================================================================================
    private static final String[] PUBLIC_PATHS = {
            "/user/register",
            "/user/login",
            "/user/secret_key",
            "/user/password",
    };

    // =================================================================================
    // 2. 权限混合路径
    // =================================================================================
    private static final String[] READ_ONLY_FOR_USER_PATHS = {
            "/user/info",
            "/user/background",
            "/user/headPortrait",

            "/practice/problem",

            "/contest/problem",
            "/contest/contest"
    };
    private static final String[] READ_ONLY_FOR_PUBLIC_PATHS = {
            "/sys/doc"
    };

    // =================================================================================
    // 3. 定义普通用户权限路径 (需登录，角色为 USER, MANAGER, ROOT 之一)
    // =================================================================================
    private static final String[] AUTHENTICATED_PATHS = {
            "/user/logout",
            "/user/rank",
            "/user/heatmap",

            "/practice/list",
            "/practice/list_search",
            "/practice/list_record/self",
            "/practice/test_case",
            "/practice/submit",

            "/contest/attend",
            "/contest/attended",
            "/contest/problems",
            "/contest/contests",
            "/contest/board",
            "/contest/submit",

            "/advice",
    };

    // =================================================================================
    // 4. 定义管理员权限路径 (仅限 MANAGER, ROOT)
    // =================================================================================
    private static final String[] ADMIN_PATHS = {
            "/user/activate",
            "/user/deactivate",
            "/user/users",
            "/user/managers",
            "/user/promote",
            "/user/demote",

            "/sys/log/list",
            "/sys/mysqldump",

            "/practice/list_record/any",
            "/practice/list_record/all",
            "/practice/get_case",
            "/practice/config_file",
            "/practice/save_case",
    };

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                // 禁用 CSRF 和 Basic Auth (因为是前后端分离且使用 Token)
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)

                // 权限拦截配置
                .authorizeExchange(exchange -> exchange
                        // 允许所有的 OPTIONS 请求 (解决跨域预检问题)
                        .pathMatchers(HttpMethod.OPTIONS).permitAll()

                        // 白名单放行
                        .pathMatchers(PUBLIC_PATHS).permitAll()

                        // 管理员能修改，普通用户能读
                        .pathMatchers(HttpMethod.GET, READ_ONLY_FOR_USER_PATHS).hasAnyRole("USER", "MANAGER", "ROOT")
                        .pathMatchers(READ_ONLY_FOR_USER_PATHS).hasAnyRole("MANAGER", "ROOT")

                        // 管理员能修改，可公开获取
                        .pathMatchers(HttpMethod.GET, READ_ONLY_FOR_PUBLIC_PATHS).permitAll()
                        .pathMatchers(READ_ONLY_FOR_PUBLIC_PATHS).hasAnyRole("MANAGER", "ROOT")

                        // 管理员接口 (注意：范围小的权限通常放在前面，虽然 Spring Security 是按顺序匹配的)
                        .pathMatchers(ADMIN_PATHS).hasAnyRole("MANAGER", "ROOT")

                        // 已登录用户接口
                        .pathMatchers(AUTHENTICATED_PATHS).hasAnyRole("USER", "MANAGER", "ROOT")

                        // 其他所有请求必须认证
                        .anyExchange().authenticated()
                )
                .addFilterBefore(new JwtFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }
}