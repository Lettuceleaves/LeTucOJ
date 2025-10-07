package com.LetucOJ.gateway.tool;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebFluxSecurity
@EnableMethodSecurity(prePostEnabled = true)
@Order(-1)
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable).authorizeExchange(exchange -> exchange
                        .pathMatchers(HttpMethod.OPTIONS).permitAll()

                        // 公开接口
                        .pathMatchers(
                                "/user/register", "/user/login", "/", "/index.html", "/static/**", "/assets/**", "/favicon.ico", "/code.txt", "/sys/doc/get"
                        ).permitAll()

                        // 用户权限接口
                        .pathMatchers(
                                "/practice/list", "/practice/searchList", "/practice/full/get", "/user/info/get", "/user/info/update",
                                "/practice/submit", "/contest/full/getProblem", "/contest/full/getContest", "/user/heatmap",
                                "/contest/attend", "/contest/list/problem", "/contest/list/contest", "/contest/inContest",
                                "/contest/list/board", "/contest/submit", "/user/logout", "/user/changePassword", "/advice", "/practice/count", "/recordList/self",
                                "/user/rank", "/user/background/get", "/user/background/update", "/user/headPortrait/get", "/user/headPortrait/update"
                        ).hasAnyRole("USER", "MANAGER", "ROOT")

                        // 管理员权限接口
                        .pathMatchers(
                                "/practice/fullRoot/insert", "/practice/fullRoot/update", "/practice/fullRoot/delete", "/practice/getCase",
                                "/practice/submitCase", "/user/activate", "/user/deactivate", "/practice/listRoot", "/practice/fullRoot/get",
                                "/user/users", "/user/managers", "/user/promote", "/user/demote", "/practice/submitInRoot", "/practice/searchListInRoot",
                                "/contest/insertContest", "/contest/updateContest", "/contest/insertProblem",
                                "/contest/deleteProblem", "/sys/doc/update", "/sys/refresh/sql", "/recordList/all", "/recordList/any"
                        ).hasAnyRole("MANAGER", "ROOT")


                        .anyExchange().authenticated()
                )

                .addFilterBefore(jwtFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }
}