package com.LetucOJ.gateway.tool;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Set;

/**
 * 角色重定向过滤器
 * 如果用户拥有 ROOT 或 MANAGER 权限，访问特定普通接口时，自动转发到管理员专用接口
 */
@Slf4j
@Component
@Order(20)
public class RoleRedirectFilter implements WebFilter {

    // 1. 定义特权角色集合
    private static final Set<String> PRIVILEGED_ROLES = Set.of("ROLE_ROOT", "ROLE_MANAGER");

    // 2. 定义路径映射表 (Key: 原始路径 -> Value: 管理员转发路径)
    // 以后如果需要新增转发规则，只需要在这里添加一行即可，无需修改逻辑代码
    private static final Map<String, String> PATH_MAPPING = Map.of(
            "/practice/list",              "/practice/listRoot",
            "/practice/full/get",          "/practice/fullRoot/get",
            "/practice/submit",            "/practice/submitInRoot",
            "/practice/searchList",        "/practice/searchListInRoot",
            "/contest/list/problem",       "/contest/list/problemInRoot",
            "/contest/submit",             "/contest/submitInRoot",
            "/contest/list/board",         "/contest/list/boardInRoot",
            "/contest/full/getProblem",    "/contest/full/getProblemInRoot",
            "/contest/full/getContest",    "/contest/full/getContestInRoot"
    );

    @NotNull
    @Override
    public Mono<Void> filter(@NotNull ServerWebExchange exchange, WebFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated) // 过滤掉未认证的用户
                .flatMap(auth -> {
                    // 检查是否拥有特权角色
                    if (hasPrivilegedRole(auth)) {
                        String currentPath = exchange.getRequest().getURI().getPath();

                        // 查找是否命中映射表
                        String redirectPath = PATH_MAPPING.get(currentPath);
                        if (redirectPath != null) {
                            log.debug("Privileged access detected. Redirecting from {} to {}", currentPath, redirectPath);
                            return internalForward(exchange, redirectPath, chain);
                        }
                    }
                    // 有认证但无特权，或路径不在映射表中，直接放行
                    return chain.filter(exchange);
                })
                // 如果 Context 为空或未认证 (filter过滤掉了)，则直接放行
                .switchIfEmpty(chain.filter(exchange));
    }

    /**
     * 判断用户是否包含任意一个特权角色
     */
    private boolean hasPrivilegedRole(Authentication auth) {
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(PRIVILEGED_ROLES::contains);
    }

    /**
     * 内部请求转发
     */
    private Mono<Void> internalForward(ServerWebExchange exchange, String newPath, WebFilterChain chain) {
        ServerHttpRequest newRequest = exchange.getRequest()
                .mutate()
                .path(newPath)
                .build();

        return chain.filter(exchange.mutate().request(newRequest).build());
    }
}