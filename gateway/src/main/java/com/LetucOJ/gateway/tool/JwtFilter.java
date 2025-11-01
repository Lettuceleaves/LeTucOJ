package com.LetucOJ.gateway.tool;

import com.LetucOJ.gateway.Redis;
import com.LetucOJ.gateway.result.Result;
import com.LetucOJ.gateway.result.errorcode.BaseErrorCode;
import com.LetucOJ.gateway.result.errorcode.GatewayErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
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
import org.springframework.stereotype.Component;
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

@Component
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

    @NotNull
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, @NotNull WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        System.out.println("------" + "Method:" + exchange.getRequest().getMethod() + " " + exchange + " " + chain);

        if (HttpMethod.OPTIONS.equals(exchange.getRequest().getMethod()) || isStatic(path) || WHITELIST.contains(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        Claims claims;
        try {
            claims = JwtUtil.parseToken(token);
        } catch (JwtException ex) {
            String body = Result.failure(BaseErrorCode.NEED_LOGIN).toJsonString();
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);

            ServerHttpResponse resp = exchange.getResponse();

            resp.getHeaders().set(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
            resp.getHeaders().set(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET,POST,PUT,DELETE,OPTIONS");
            resp.getHeaders().set(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "Authorization,Content-Type");
            resp.getHeaders().set(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");

            resp.setStatusCode(HttpStatus.OK);
            resp.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            resp.getHeaders().setContentLength(bytes.length);

            return resp.writeWith(
                    Mono.just(resp.bufferFactory().wrap(bytes))
            );
        }

        if (Redis.mapGet("black:" + claims.getSubject()) != null && !"/user/login".equals(path)) {
            return JwtUtil.writeErrorResponse(exchange, GatewayErrorCode.USER_BLOCKED);
        }

        ServerWebExchange mutated = exchange;
        URI originalUri = exchange.getRequest().getURI();
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUri(originalUri);

        String pname = claims.getSubject();
        if (NAME_REQUIRED.contains(path)) {
            uriBuilder.replaceQueryParam("pname", pname);
        }
        exchange.getAttributes().put("username", pname);

        String cnname = claims.get("cnname", String.class);
        if (CNNAME_REQUIRED.contains(path)) {
            uriBuilder.replaceQueryParam("cnname", cnname);
        }

        if (NAME_REQUIRED.contains(path) || CNNAME_REQUIRED.contains(path)) {
            URI updatedUri = uriBuilder.build().encode().toUri();
            mutated = exchange.mutate()
                    .request(r -> r.uri(updatedUri))
                    .build();
        }

        String role = claims.get("role", String.class);
        Authentication auth = new UsernamePasswordAuthenticationToken(
                claims.getSubject(), null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role)));

        return chain.filter(mutated)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));

    }
}