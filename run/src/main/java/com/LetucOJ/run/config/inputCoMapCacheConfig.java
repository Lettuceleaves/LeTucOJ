package com.LetucOJ.run.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@EnableCaching
public class inputCoMapCacheConfig {
    @Bean
    public CacheManager inputCoMapCacheConfigManager() {
        SimpleCacheManager redisConfig = new SimpleCacheManager();
        redisConfig.setCaches(List.of(
                new ConcurrentMapCache("inputFiles")
        ));
        return redisConfig;
    }
}
