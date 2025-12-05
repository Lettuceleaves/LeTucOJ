package com.LetucOJ.gateway.tool;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.LetucOJ.common.cache.Redis;
import com.LetucOJ.common.result.Result;
import com.LetucOJ.common.result.errorcode.BaseErrorCode;
import com.LetucOJ.common.result.errorcode.GatewayErrorCode;
import com.LetucOJ.common.trace.TraceContext;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

@Slf4j
public class JwtFilter implements WebFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final List<String> WHITELIST = List.of(
            "/user/login", "/user/register", "/sys/doc/get"
    );

    // 白名单
    private static final List<String> STATIC = List.of(
            "/", "/index.html",
            "/static/**", "/assets/**",
            "/**/*.js", "/**/*.css", "/**/*.ico", "/**/*.png", "/**/*.woff2", "/code.txt"
    );

    private static final AntPathMatcher MATCHER = new AntPathMatcher();

    private boolean isStatic(String path) {
        return STATIC.stream().anyMatch(p -> MATCHER.match(p, path));
    }

    private static final List<String> NAME_REQUIRED = List.of(
            "/contest/attend", "/contest/submit", "/contest/submitInRoot", "/practice/recordList/self", "/user/info/update",
            "/practice/submit", "/practice/submitInRoot", "/user/change-password", "/contest/inContest", "/practice/list",
            "/practice/listRoot", "/practice/searchList", "/practice/searchListInRoot", "/user/logout", "/user/background/update", "/user/headPortrait/update"
    );

    private static final List<String> CNNAME_REQUIRED = List.of(
            "/contest/attend", "/contest/submit", "/contest/submitInRoot", "/practice/submit", "/practice/submitInRoot"
    );

    private static final List<String> ROLE_REQUIRED = List.of(
    );

    @NotNull
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, @NotNull WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        System.out.println("------" + "Method:" + exchange.getRequest().getMethod() + " " + exchange + " " + chain);

        // 默认放过OPTIONS方法
        if (HttpMethod.OPTIONS.equals(exchange.getRequest().getMethod()) || isStatic(path) || WHITELIST.contains(path)) {
            return chain.filter(exchange);
        }

        // 没有JWT，直接UNAUTHORIZED
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // 获取token内容
        String token = authHeader.substring(BEARER_PREFIX.length());

        Claims claims;
        try {
            claims = JwtUtil.parseToken(token);
        } catch (JwtException ex) {
            return writeErrorResponse(exchange);
        }

        // 黑名单拦截
        if (Redis.mapGet("black:" + claims.getSubject()) != null) {
            return JwtUtil.writeErrorResponse(exchange, GatewayErrorCode.USER_BLOCKED);
        }

        // uri构造器
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUri(exchange.getRequest().getURI());

        String userName = claims.getSubject();
        String nickName = claims.get("nick_name", String.class);
        String role = claims.get("role", String.class);


        // 保存用户名上下文
        exchange.getAttributes().put("user_name", userName);

        // 修改http标志位
        boolean shouldMutate = false;

        if (NAME_REQUIRED.contains(path)) {
            uriBuilder.replaceQueryParam("user_name", userName);
            shouldMutate = true;
        }

        if (CNNAME_REQUIRED.contains(path)) {
            uriBuilder.replaceQueryParam("nick_name", nickName);
            shouldMutate = true;
        }

        if (ROLE_REQUIRED.contains(path)) {
            uriBuilder.replaceQueryParam("role", role);
            shouldMutate = true;
        }

        URI finalUri = shouldMutate ? uriBuilder.build().encode().toUri() : null;



        ServerWebExchange mutated = exchange.mutate()
                .request(r -> {
                    if (finalUri != null) {
                        r.uri(finalUri);
                    }
                })
                .build();

        // 把信息传递给Spring Gateway，方便调用者方法进行拦截
        Authentication auth = new UsernamePasswordAuthenticationToken(
                claims.getSubject(), null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role)));

        return chain.filter(mutated)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));

    }

    private Mono<Void> writeErrorResponse(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();

        // 设置响应类型
        response.setStatusCode(HttpStatus.OK);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // 生成 JSON 数据
        String body = JSONUtil.toJsonStr(Result.failure(BaseErrorCode.NEED_LOGIN));
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);

        // 写入响应
        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }
}