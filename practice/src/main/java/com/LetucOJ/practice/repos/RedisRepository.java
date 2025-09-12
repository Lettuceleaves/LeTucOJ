package com.LetucOJ.practice.repos;

import java.util.List;

/**
 * 通用 Redis 仓库接口
 * @param <V> 存储对象类型
 */
public interface RedisRepository<V> {         // 注意这里要加 <V>
    void save(String key, V value);
    V find(String key);
    List<V> findAll(String pattern);
    void delete(String key);
    boolean exists(String key);
}