package com.LetucOJ.common.mq.impl;

import com.LetucOJ.common.mq.MessageQueueConsumer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "mq.type", havingValue = "rocketmq")
public class RocketMQConsumer implements MessageQueueConsumer {

    @Override
    public void registerListener(String topic, String tag, MessageListener listener) {
    }
}

