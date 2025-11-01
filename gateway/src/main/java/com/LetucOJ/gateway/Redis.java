package com.LetucOJ.gateway;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.Nullable;

import java.time.Duration;

/** 静态工具，封装最常用 set/get 以及 Set 操作 */
public final class Redis {

    private static StringRedisTemplate template;

    public Redis(StringRedisTemplate template) {
        Redis.template = template;
    }

    /* ------------------- map ------------------- */

    public static void mapPut(String key, String value) {
        template.opsForValue().set(key, value);
    }

    public static void mapPutDuration(String key, String value, long seconds) {
        template.opsForValue().set(key, value, Duration.ofSeconds(seconds));
    }

    @Nullable
    public static String mapGet(String key) {
        return template.opsForValue().get(key);
    }

    public static void mapRemove(String key) {
        template.delete(key);
    }

    /* ------------------- set ------------------- */

    public static boolean setContains(String key, String member) {
        return Boolean.TRUE.equals(template.opsForSet().isMember(key, member));
    }

    public static boolean setAdd(String key, String member) {
        Long ret = template.opsForSet().add(key, member);
        return ret != null && ret > 0;
    }

    public static boolean setRemove(String key, String member) {
        Long ret = template.opsForSet().remove(key, member);
        return ret != null && ret > 0;
    }

    private Redis() {}
}

