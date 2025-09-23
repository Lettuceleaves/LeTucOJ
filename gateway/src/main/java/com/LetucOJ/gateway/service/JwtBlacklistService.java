package com.LetucOJ.gateway.service;

import java.time.Duration;

public interface JwtBlacklistService {
    void blacklist(String token, Duration ttl);
    boolean isBlacklisted(String token);
}
