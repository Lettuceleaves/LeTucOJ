package com.LetucOJ.gateway.config;

import com.LetucOJ.gateway.Redis;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class RedisConfig {

    @Bean
    public Redis redisUtil(StringRedisTemplate template) {
        return new Redis(template);
    }
}
