package com.LetucOJ.gateway.tool;

import io.jsonwebtoken.lang.Collections;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
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
@Order(0) // 在 JwtFilter（@Order默认最低）之后执行
public class RoleRedirectFilter implements WebFilter {

    @NotNull
    @Override
    public Mono<Void> filter(@NotNull ServerWebExchange exchange, @NotNull WebFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .switchIfEmpty(Mono.defer(() -> Mono.just(
                        new UsernamePasswordAuthenticationToken(
                                "anonymous",
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
                        )
                )))

                .flatMap(auth -> {
                    if (auth != null && auth.isAuthenticated()) {
                        ServerHttpRequest request = exchange.getRequest();
                        String path = request.getURI().getPath();

                        // 示例逻辑：当访问 /dashboard，根据角色重定向
                        switch (path) {
                            case "/practice/list" -> {
                                if (hasRole(auth, "ROOT") || hasRole(auth, "MANAGER")) {
                                    return redirect(exchange, "/practice/listRoot");
                                }
                            }
                            case "/practice/full/get" -> {
                                if (hasRole(auth, "ROOT") || hasRole(auth, "MANAGER")) {
                                    return redirect(exchange, "/practice/fullRoot/get");
                                }
                            }
                            case "/practice/submit" -> {
                                if (hasRole(auth, "ROOT") || hasRole(auth, "MANAGER")) {
                                    return redirect(exchange, "/practice/submitInRoot");
                                }
                            }
                            case "/practice/searchList" -> {
                                if (hasRole(auth, "ROOT") || hasRole(auth, "MANAGER")) {
                                    return redirect(exchange, "/practice/searchListInRoot");
                                }
                            }
                        }
                    }

                    return chain.filter(exchange);
                });
    }

    private boolean hasRole(Authentication auth, String role) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(role));
    }

    private Mono<Void> redirect(ServerWebExchange exchange, String location) {
        exchange.getResponse().setStatusCode(HttpStatus.FOUND); // 302 Redirect
        exchange.getResponse().getHeaders().setLocation(exchange.getRequest().getURI().resolve(location));
        return exchange.getResponse().setComplete();
    }
}
