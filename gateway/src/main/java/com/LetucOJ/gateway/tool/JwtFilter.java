package com.LetucOJ.gateway.tool;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

/**
 * JWT 过滤器：解析 Token、计算 TTL、检查黑名单，并注入参数
 */
@Component
public class JwtFilter implements WebFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";
    private static final List<String> WHITELIST = List.of(
            "/user/login", "/user/register"
    );

    /** 需要注入 ttl 参数的接口 */
    private static final List<String> TTL_REQUIRED = List.of(
            "/user/logout"
    );

    /** 需要注入 pname 参数的接口 */
    private static final List<String> NAME_REQUIRED = List.of(
            "/contest/attend", "/contest/submit", "/contest/submitInRoot", "/practice/recordList/self", "/practice/submit", "/practice/submitInRoot", "/user/change-password", "/contest/inContest"
    );

    /** 需要注入 cnname 参数的接口 */
    private static final List<String> CNNAME_REQUIRED = List.of(
            "/contest/attend", "/contest/submit", "/contest/submitInRoot", "/practice/submit", "/practice/submitInRoot"
    );

    private final ReactiveStringRedisTemplate redisTemplate;

    @Autowired
    public JwtFilter(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        System.out.println("------" + "Method:" + exchange.getRequest().getMethod() + " " + exchange);

        // 跳过 OPTIONS 和白名单路径
        if (HttpMethod.OPTIONS.equals(exchange.getRequest().getMethod())
                || WHITELIST.contains(path)) {
            return chain.filter(exchange);
        }

        // 验证 Authorization 头
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        // 解析 JWT
        Claims claims;
        try {
            claims = JwtUtil.parseToken(token);
        } catch (JwtException ex) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            byte[] bytes = "无效的 Token".getBytes(StandardCharsets.UTF_8);
            return exchange.getResponse().writeWith(
                    Mono.just(exchange.getResponse().bufferFactory().wrap(bytes))
            );
        }

        // 检查黑名单
        return redisTemplate.hasKey(BLACKLIST_PREFIX + token)
                .flatMap(isBlacklisted -> {
                    if (Boolean.TRUE.equals(isBlacklisted)) {
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        byte[] bytes = "Token 已失效，请重新登录".getBytes(StandardCharsets.UTF_8);
                        return exchange.getResponse().writeWith(
                                Mono.just(exchange.getResponse().bufferFactory().wrap(bytes))
                        );
                    }

                    // 注入参数（ttl, sub, cnname）

                    ServerWebExchange mutated = exchange;
                    URI originalUri = exchange.getRequest().getURI();
                    UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUri(originalUri);

                    // 计算剩余 TTL（秒）
                    long ttlSeconds = (claims.getExpiration().getTime() - System.currentTimeMillis()) / 1000;
                    if (ttlSeconds < 0) {
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        byte[] expired = "Token 已过期".getBytes(StandardCharsets.UTF_8);
                        return exchange.getResponse().writeWith(
                                Mono.just(exchange.getResponse().bufferFactory().wrap(expired))
                        );
                    }
                    if (TTL_REQUIRED.contains(path)) {
                        uriBuilder.replaceQueryParam("ttl", ttlSeconds);
                    }

                    String pname = claims.get("sub", String.class);
                    if (NAME_REQUIRED.contains(path)) {
                        uriBuilder.replaceQueryParam("pname", pname);
                    }

                    String cnname = claims.get("cnname", String.class);
                    if (CNNAME_REQUIRED.contains(path)) {
                        uriBuilder.replaceQueryParam("cnname", cnname);
                    }

                    if (TTL_REQUIRED.contains(path) || NAME_REQUIRED.contains(path) || CNNAME_REQUIRED.contains(path)) {
                        URI updatedUri = uriBuilder.build().encode().toUri();

                        var mutatedRequest = exchange.getRequest().mutate()
                                .uri(updatedUri).build();
                        mutated = exchange.mutate().request(mutatedRequest).build();
                    }

                    // 设置认证上下文
                    String role = claims.get("role", String.class);
                    Authentication auth = new UsernamePasswordAuthenticationToken(
                            claims.getSubject(), null,
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                    );
                    return chain.filter(mutated)
                            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));
                });
    }
}
