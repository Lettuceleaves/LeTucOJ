package com.LetucOJ.sys.repository;

import com.LetucOJ.common.mq.impl.Message;
import jakarta.annotation.Resource;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

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

    @Override
    public void onMessage(Message message) {
        System.out.println("get log message: " + message.getBody());
        mybatisRepos.appendLog(message.getKey(), message.getBody());
    }
}