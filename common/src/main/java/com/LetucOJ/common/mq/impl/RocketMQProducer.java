package com.LetucOJ.common.mq.impl;

import com.LetucOJ.common.mq.MessageQueueProducer;
import com.LetucOJ.common.result.ResultVO;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "mq.type", havingValue = "rocketmq")
public class RocketMQProducer implements MessageQueueProducer {

    @Override
    public ResultVO send(Message message) {
        return null;
    }

    @Override
    public void sendAsync(Message message, ResultVO callback) {
    }

    @Override
    public void sendOneWay(Message message) {
    }
}

