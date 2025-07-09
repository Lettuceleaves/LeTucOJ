package com.LetucOJ.gateway.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "auth.service")
public class authSetting {
    // Getters and Setters
    private String url;
    private boolean on;

}
