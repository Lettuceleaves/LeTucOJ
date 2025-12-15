package com.LetucOJ.gateway.chain;

import cn.hutool.json.JSONUtil;
import com.LetucOJ.common.cache.Redis;
import com.LetucOJ.common.log.LogLevel;
import com.LetucOJ.common.log.Logger;
import com.LetucOJ.common.log.Type;
import com.LetucOJ.common.result.Result;
import com.LetucOJ.common.result.errorcode.BaseErrorCode;
import com.LetucOJ.common.result.errorcode.GatewayErrorCode;
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

    // 白名单
    private static final List<String> WHITELIST = List.of(
            "/user/register",
            "/user/login",
            "/user/secret_key",
            "/user/password"
    );

    private static final List<String> USER_NAME_REQUIRED = List.of(
            "/contest/problem",
            "/contest/attend",
            "/contest/attended",
            "/contest/submit",

            "/practice/list",
            "/practice/list_search",
            "/practice/list_record/self",
            "/practice/submit",

            "/user/logout",
            "/user/promote",
            "/user/demote"
    );

    private static final List<String> NICK_NAME_REQUIRED = List.of(
            "/contest/attend",
            "/contest/submit",
            "/practice/submit"
    );

    private static final List<String> ROLE_REQUIRED = List.of(
            "/contest/problem",
            "/contest/contest",
            "/contest/problems",
            "/contest/board",
            "/contest/submit",

            "/practice/list",
            "/practice/list_search",
            "/practice/problem",
            "/practice/test_case",
            "/practice/submit",
            "/practice/test_case"
    );

    @NotNull
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, @NotNull WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        System.out.println("------" + "Method:" + exchange.getRequest().getMethod() + " " + exchange + " " + chain);

        // 默认放过OPTIONS方法
        if (HttpMethod.OPTIONS.equals(exchange.getRequest().getMethod())  || WHITELIST.contains(path)) {
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

        Logger.log(Type.CLIENT, LogLevel.INFO, "UserName: " + userName + " NickName: " + nickName + " Role: " + role);

        // 保存用户名上下文
        exchange.getAttributes().put("user_name", userName);

        // 修改http标志位
        boolean shouldMutate = false;

        if (USER_NAME_REQUIRED.contains(path)) {
            uriBuilder.replaceQueryParam("user_name", userName);
            shouldMutate = true;
        }

        if (NICK_NAME_REQUIRED.contains(path)) {
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