//package com.LetucOJ.practice.repos.impl;
//import com.LetucOJ.practice.repos.RedisRepository;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//import java.util.Objects;
//import java.util.Set;
//import java.util.stream.Collectors;
//
///**
// * 通用 Redis 仓库实现
// * @param <V> 存储对象类型
// */
//@Repository
//public class RedisRepositoryImpl<V> implements RedisRepository<V> {   // 这里也要加 <V>
//
//    private final RedisTemplate<String, V> redisTemplate;
//
//    public RedisRepositoryImpl(RedisTemplate<String, V> redisTemplate) {
//        this.redisTemplate = redisTemplate;
//    }
//
//    @Override
//    public void save(String key, V value) {
//        redisTemplate.opsForValue().set(key, value);
//    }
//
//    @Override
//    public V find(String key) {
//        return redisTemplate.opsForValue().get(key);
//    }
//
//    @Override
//    public List<V> findAll(String pattern) {
//        Set<String> keys = redisTemplate.keys(pattern);
//        if (keys.isEmpty()) {
//            return List.of();
//        }
//        return Objects.requireNonNull(redisTemplate.opsForValue()
//                        .multiGet(keys))
//                .stream()
//                .filter(Objects::nonNull)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public void delete(String key) {
//        redisTemplate.delete(key);
//    }
//
//    @Override
//    public boolean exists(String key) {
//        return redisTemplate.hasKey(key);
//    }
//}
