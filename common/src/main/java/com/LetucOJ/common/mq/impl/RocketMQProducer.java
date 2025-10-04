package com.LetucOJ.common.mq.impl;

import com.LetucOJ.common.mq.MessageQueueProducer;
import com.LetucOJ.common.result.Result;
import com.LetucOJ.common.result.ResultVO;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@ConditionalOnProperty(name = "mq.type", havingValue = "rocketmq")
public class RocketMQProducer implements MessageQueueProducer {

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Override
    public ResultVO send(Message message) {
        rocketMQTemplate.syncSend(message.getTopic(), message);
        return Result.success();
    }

    @Override
    public void sendAsync(Message message, ResultVO callback) {
    }

    @Override
    public void sendOneWay(Message message) {
    }
}

