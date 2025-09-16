package com.LetucOJ.practice;

import com.LetucOJ.practice.repos.RedisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component   // 1. 让 Spring 扫描并创建
public class TestRedis {

    @Autowired
    private RedisRepository redisRepository;

    public void test() {
        redisRepository.save("test", "Hello, Redis!");
        System.out.println("Value for 'key': " + redisRepository.find("test"));
    }
}
