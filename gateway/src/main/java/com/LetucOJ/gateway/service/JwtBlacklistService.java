package com.LetucOJ.gateway.service;

import java.time.Duration;

public interface JwtBlacklistService {
    /** 将 token 加入黑名单，过期时间与 JWT 自身剩余有效期一致 */
    void blacklist(String token, Duration ttl);

    /** 判断 token 是否已在黑名单 */
    boolean isBlacklisted(String token);
}
