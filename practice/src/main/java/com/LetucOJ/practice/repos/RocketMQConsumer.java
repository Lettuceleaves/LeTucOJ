package com.LetucOJ.practice.repos;

import com.LetucOJ.common.mq.impl.Message;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "mq.type", havingValue = "rocketmq")
@RocketMQMessageListener(
        topic = "practice",
        consumerGroup = "practice",
        consumeMode = ConsumeMode.CONCURRENTLY
)
public class RocketMQConsumer implements RocketMQListener<Message> {

    @Override
    public void onMessage(Message message) {
        System.out.println(message.getBody());
    }
}