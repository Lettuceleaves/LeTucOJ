package com.LetucOJ.gateway.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Hooks;

@Configuration
public class WebFluxTraceConfig {

    @PostConstruct
    public void init() {
        Hooks.enableAutomaticContextPropagation();
    }
}