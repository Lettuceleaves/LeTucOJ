package com.LetucOJ.gateway.tool;

import com.LetucOJ.common.log.LogLevel;
import com.LetucOJ.common.log.Logger;
import com.LetucOJ.common.log.Type;
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
@EnableMethodSecurity(prePostEnabled = true)
@Order(10)
@RequiredArgsConstructor
public class SecurityConfig {

    // =================================================================================
    // 1. 定义白名单路径 (无需登录即可访问)
    // =================================================================================
    private static final String[] PUBLIC_PATHS = {
            "/",
            "/index.html",
            "/favicon.ico",
            "/code.txt",
            "/user/login",
            "/user/register",
            "/sys/doc/get",
            "/static/**",
            "/assets/**"
    };

    // =================================================================================
    // 2. 定义普通用户权限路径 (需登录，角色为 USER, MANAGER, ROOT 之一)
    // =================================================================================
    private static final String[] AUTHENTICATED_PATHS = {
            // 用户基础信息
            "/user/info/get",
            "/user/info/update",
            "/user/logout",
            "/user/changePassword",
            "/user/headPortrait/**",
            "/user/background/**",
            "/user/heatmap",
            "/user/rank",
            "/advice",

            // 练习相关
            "/practice/list",
            "/practice/searchList",
            "/practice/full/get",
            "/practice/submit",
            "/practice/count",

            // 比赛相关
            "/contest/list/contest",
            "/contest/list/problem",
            "/contest/list/board",
            "/contest/full/getContest",
            "/contest/full/getProblem",
            "/contest/attend",
            "/contest/submit",
            "/contest/inContest",

            // 记录相关
            "/recordList/self"
    };

    // =================================================================================
    // 3. 定义管理员权限路径 (仅限 MANAGER, ROOT)
    // =================================================================================
    private static final String[] ADMIN_PATHS = {
            // 用户管理
            "/user/users",
            "/user/managers",
            "/user/activate",
            "/user/deactivate",
            "/user/promote",
            "/user/demote",

            // 练习题目管理 (Root版本)
            "/practice/listRoot",
            "/practice/searchListRoot", // 原代码拼写可能是 searchListInRoot，请确认
            "/practice/fullRoot/**",    // 使用通配符简化 insert/update/delete/get
            "/practice/getCase",
            "/practice/submitCase",
            "/practice/submitInRoot",
            "/practice/searchListInRoot",

            // 比赛管理
            "/contest/insertContest",
            "/contest/updateContest",
            "/contest/insertProblem",
            "/contest/deleteProblem",

            // 系统维护
            "/sys/doc/update",
            "/sys/refresh/sql",
            "/recordList/all",
            "/recordList/any"
    };

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                // 禁用 CSRF 和 Basic Auth (因为是前后端分离且使用 Token)
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)

                // 权限拦截配置
                .authorizeExchange(exchange -> exchange
                        // 1. 允许所有的 OPTIONS 请求 (解决跨域预检问题)
                        .pathMatchers(HttpMethod.OPTIONS).permitAll()

                        // 2. 白名单放行
                        .pathMatchers(PUBLIC_PATHS).permitAll()

                        // 3. 管理员接口 (注意：范围小的权限通常放在前面，虽然 Spring Security 是按顺序匹配的)
                        // 这里将 ROOT 和 MANAGER 合并处理
                        .pathMatchers(ADMIN_PATHS).hasAnyRole("MANAGER", "ROOT")

                        // 4. 普通用户接口 (包含管理员，因为管理员也应该能访问用户接口)
                        .pathMatchers(AUTHENTICATED_PATHS).hasAnyRole("USER", "MANAGER", "ROOT")

                        // 5. 其他所有请求必须认证
                        .anyExchange().authenticated()
                )
                .addFilterBefore(new JwtFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }
}