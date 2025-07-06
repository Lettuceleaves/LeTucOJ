package com.LetucOJ.gateway.tool;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Base64;
import java.util.Collections;

import jakarta.validation.constraints.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

@Configuration
@EnableWebFluxSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class securityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable) // 禁用 CSRF 保护
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/auth/**").permitAll() // 允许访问 /auth 路径下的所有请求
                        .pathMatchers("/practice/rootHello").hasRole("ROOT")
                        .pathMatchers("/practice/hello").hasRole("USER")
                        .anyExchange().authenticated()) // 其他路径需要认证
                .addFilterBefore(new JwtFilter(), SecurityWebFiltersOrder.AUTHENTICATION) // 添加 JWT 过滤器
                .build();
    }

    public static class JwtFilter implements WebFilter {

        private static final String SECRET = "yoursecretkeyyoursecretkeyyoursecretkeyyoursecretkeyyoursecretkeyyoursecretkeyyoursecretkey";

        @NotNull
        @Override
        public Mono<Void> filter(ServerWebExchange exchange, @NotNull WebFilterChain chain) {
            ServerHttpRequest request = exchange.getRequest();
            String authorizationHeader = request.getHeaders().getFirst("Authorization");

            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String jwtToken = authorizationHeader.substring(7);

                byte[] secretKeyBytes = Base64.getDecoder().decode(SECRET);
                SecretKey secretKey = new SecretKeySpec(secretKeyBytes, SignatureAlgorithm.HS256.getJcaName());

                try {
                    Claims claims = Jwts.parser()
                            .verifyWith(secretKey)    // 验证所有遇到的JWS签名
                            .build()
                            .parse(jwtToken).accept(Jws.CLAIMS)   // 解析jws
                            .getPayload();  // JWT有效载荷

                    // 获取用户角色
                    String role = claims.get("role", String.class);

                    System.out.println(role);

                    // 创建 Authentication 对象
                    Authentication authentication = new UsernamePasswordAuthenticationToken(
                            claims.getSubject(), null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role)));

                    // 设置 SecurityContext
                    return chain.filter(exchange)
                            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));

                } catch (Exception e) {
                    // 处理解析异常，例如令牌无效或过期
                    return Mono.error(new RuntimeException("Invalid JWT token"));
                }
            }

            return chain.filter(exchange);
        }
    }
}
