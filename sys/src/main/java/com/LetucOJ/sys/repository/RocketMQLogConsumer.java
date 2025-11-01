package com.LetucOJ.sys.repository;

import com.LetucOJ.common.mq.impl.Message;
import com.LetucOJ.common.oss.MinioRepos;
import jakarta.annotation.Resource;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@Component
@ConditionalOnProperty(name = "mq.type", havingValue = "rocketmq")
@RocketMQMessageListener(
        topic = "log",
        consumerGroup = "log",
        consumeMode = ConsumeMode.ORDERLY
)
public class RocketMQLogConsumer implements RocketMQListener<Message> {

    @Resource
    private MybatisRepos mybatisRepos;

    @Resource
    private MinioRepos minioRepos;
    @Override
    public void onMessage(Message message) {
        System.out.println("get log message: " + message.getBody());
        String body = message.getBody();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(formatter);
        mybatisRepos.appendLog(timestamp, body);
    }
}