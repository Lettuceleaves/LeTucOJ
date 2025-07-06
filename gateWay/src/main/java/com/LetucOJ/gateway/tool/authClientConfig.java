package com.LetucOJ.gateway.tool;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class authClientConfig {
    @Bean
    public WebClient authClientConfigWeb() {
        return WebClient.builder().build();
    }
}
