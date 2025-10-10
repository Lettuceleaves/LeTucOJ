package com.LetucOJ.gateway.tool;

import io.jsonwebtoken.lang.Collections;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Order(0)
public class RoleRedirectFilter implements WebFilter {

    private static final String ROLE_PREFIX = "ROLE_";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .switchIfEmpty(Mono.defer(() -> Mono.just(
                        new UsernamePasswordAuthenticationToken(
                                "anonymous", null,
                                List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS")))
                )))
                .flatMap(auth -> {
                    if (auth != null && auth.isAuthenticated()) {
                        String path = exchange.getRequest().getURI().getPath();
                        if ("/practice/list".equals(path)
                                && (hasRole(auth, ROLE_PREFIX + "ROOT")
                                || hasRole(auth, ROLE_PREFIX + "MANAGER"))) {
                            return internalForward(exchange, "/practice/listRoot", chain);
                        }
                        if ("/practice/full/get".equals(path)
                                && (hasRole(auth, ROLE_PREFIX + "ROOT")
                                || hasRole(auth, ROLE_PREFIX + "MANAGER"))) {
                            return internalForward(exchange, "/practice/fullRoot/get", chain);
                        }
                        if ("/practice/submit".equals(path)
                                && (hasRole(auth, ROLE_PREFIX + "ROOT")
                                || hasRole(auth, ROLE_PREFIX + "MANAGER"))) {
                            return internalForward(exchange, "/practice/submitInRoot", chain);
                        }
                        if ("/practice/searchList".equals(path)
                                && (hasRole(auth, ROLE_PREFIX + "ROOT")
                                || hasRole(auth, ROLE_PREFIX + "MANAGER"))) {
                            return internalForward(exchange, "/practice/searchListInRoot", chain);
                        }
                        if ("/contest/list/problem".equals(path)
                                && (hasRole(auth, ROLE_PREFIX + "ROOT")
                                || hasRole(auth, ROLE_PREFIX + "MANAGER"))) {
                            return internalForward(exchange, "/contest/list/problemInRoot", chain);
                        }
                        if ("/contest/submit".equals(path)
                                && (hasRole(auth, ROLE_PREFIX + "ROOT")
                                || hasRole(auth, ROLE_PREFIX + "MANAGER"))) {
                            return internalForward(exchange, "/contest/submitInRoot", chain);
                        }
                        if ("/contest/list/board".equals(path)
                                && (hasRole(auth, ROLE_PREFIX + "ROOT")
                                || hasRole(auth, ROLE_PREFIX + "MANAGER"))) {
                            return internalForward(exchange, "/contest/list/boardInRoot", chain);
                        }
                        if ("/contest/full/getProblem".equals(path)
                                && (hasRole(auth, ROLE_PREFIX + "ROOT")
                                || hasRole(auth, ROLE_PREFIX + "MANAGER"))) {
                            return internalForward(exchange, "/contest/full/getProblemInRoot", chain);
                        }
                        if ("/contest/full/getContest".equals(path)
                                && (hasRole(auth, ROLE_PREFIX + "ROOT")
                                || hasRole(auth, ROLE_PREFIX + "MANAGER"))) {
                            return internalForward(exchange, "/contest/full/getContestInRoot", chain);
                        }
                    }
                    return chain.filter(exchange);
                });
    }

    private boolean hasRole(Authentication auth, String role) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(role));
    }

    private Mono<Void> internalForward(ServerWebExchange exchange,
                                       String newPath,
                                       WebFilterChain chain) {
        ServerHttpRequest forwardReq = exchange.getRequest()
                .mutate()
                .path(newPath)
                .build();
        ServerWebExchange forwardExchange = exchange.mutate()
                .request(forwardReq)
                .build();
        return chain.filter(forwardExchange);
    }
}
