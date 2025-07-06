package com.LetucOJ.auth.tool;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class securityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 禁用 CSRF 保护
                .csrf(csrf -> csrf.disable())
                // 其他安全配置（根据需要添加）
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/auth/register", "/auth/login").permitAll() // 允许访问注册和登录接口
                        .anyRequest().authenticated() // 其他请求需要认证
                )
//                .formLogin(form -> form
//                        .loginProcessingUrl("/auth/login") // 登录请求的 URL
//                        .permitAll() // 允许访问登录页面
//                )
                .logout(LogoutConfigurer::permitAll // 允许访问登出接口
                );

        return http.build();
    }
}
