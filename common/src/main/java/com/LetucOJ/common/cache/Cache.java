package com.LetucOJ.common.cache;

import java.time.Duration;
import java.util.concurrent.Callable;

/**
 * 统一缓存接口
 * 支持 key-value、过期时间、原子加载
 */
public interface Cache {
    <V> V get(String key, Class<V> clazz);
    void put(String key, Object value);
    void put(String key, Object value, Duration ttl);
    <V> V getOrLoad(String key, Class<V> clazz, Callable<V> loader, Duration ttl);
    void delete(String key);
    void clear();
}
