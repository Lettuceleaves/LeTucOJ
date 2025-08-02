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

@Configuration
@EnableWebFluxSecurity
@EnableMethodSecurity(prePostEnabled = true)
@Order(-1)
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        System.out.println("Configuring SecurityWebFilterChain...");

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable).authorizeExchange(exchange -> exchange
                        .pathMatchers(HttpMethod.OPTIONS).permitAll()




                        // 公开接口
                        .pathMatchers(
                                "/user/register", "/user/login"
                        ).permitAll()

                        // 用户权限接口
                        .pathMatchers(
                                "/practice/list", "/practice/searchList", "/practice/full/get",
                                "/practice/submit", "/contest/full/getProblem", "/contest/full/getContest",
                                "/contest/attend", "/contest/list/problem", "/contest/list/contest",
                                "/contest/list/board", "/contest/submit", "/user/logout", "/user/changePassword", "/advice"
                        ).hasAnyRole("USER", "MANAGER", "ROOT")

                        // 管理员权限接口
                        .pathMatchers(
                                "/practice/fullRoot/insert", "/practice/fullRoot/update", "/practice/fullRoot/delete", "/practice/getCase",
                                "/practice/submitCase", "/user/activate", "/user/deactivate",
                                "/user/users", "/user/managers", "/user/promote", "/user/demote",
                                "/contest/insertContest", "/contest/updateContest", "/contest/insertProblem",
                                "/contest/deleteProblem", "/sys/doc/get", "/sys/doc/update"
                        ).hasAnyRole("MANAGER", "ROOT")





                        .anyExchange().authenticated()
                )

                .addFilterBefore(jwtFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }
}