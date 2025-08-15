package com.LetucOJ.gateway.service.impl;

import com.LetucOJ.gateway.service.JwtBlacklistService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class JwtBlacklistServiceImpl implements JwtBlacklistService {

    private final RedisTemplate<String, String> redis;

    private static final String PREFIX = "jwt:blacklist:";

    public JwtBlacklistServiceImpl(RedisTemplate<String, String> redis) {
        this.redis = redis;
    }

    @Override
    public void blacklist(String token, Duration ttl) {
        // 将整个 token 作为 key，也可以只存 jti
        String key = PREFIX + token;
        // value 可随意，比如 "1"
        redis.opsForValue().set(key, "1", ttl);
    }

    @Override
    public boolean isBlacklisted(String token) {
        return redis.hasKey(PREFIX + token);
    }
}
