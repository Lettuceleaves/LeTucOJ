package com.LetucOJ.gateway.service.impl;

import com.LetucOJ.gateway.service.gateWayService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class gateWayServiceImpl implements gateWayService {

    @Autowired
    private WebClient WebClient;

    @Value("${auth.service.url}")
    private String authServiceUrl;

    public Mono<String> authenticate(String username, String password) {
        return WebClient.post()
                .uri(authServiceUrl + "/auth/login")
                .bodyValue(new LoginRequest(username, password))
                .retrieve()
                .bodyToMono(String.class);
    }

    @Data
    private static class LoginRequest {
        private String username;
        private String password;

        public LoginRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }
}
