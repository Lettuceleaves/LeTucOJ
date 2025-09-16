package com.LetucOJ.practice;

import com.LetucOJ.practice.repos.RedisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

@SpringBootApplication
@EnableFeignClients
public class PracticeApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx =
                SpringApplication.run(PracticeApplication.class, args);   // 返回容器

        TestRedis test = ctx.getBean(TestRedis.class);   // 让容器给你实例
        test.test();                                       // 此时 redisRepository 已注入
    }

}

