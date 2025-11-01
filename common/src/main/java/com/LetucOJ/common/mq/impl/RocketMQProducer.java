package com.LetucOJ.common.mq.impl;

import com.LetucOJ.common.mq.MessageQueueProducer;
import com.LetucOJ.common.result.Result;
import com.LetucOJ.common.result.ResultVO;
import jakarta.annotation.PostConstruct;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "mq.type", havingValue = "rocketmq")
public class RocketMQProducer implements MessageQueueProducer {

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Override
    public ResultVO send(Message message) {
        Object payload = message;
        Map<String, Object> headers = new HashMap<>();
        headers.put("rocketmq_KEYS", message.getKey());
        headers.put(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON);
        org.springframework.messaging.Message<?> springMessage =
                MessageBuilder.withPayload(payload)
                        .copyHeaders(headers)
                        .build();
        int delayLevel = 0;
        if (message.getDelayLevel() != null) {
            delayLevel = message.getDelayLevel();
        }

        if (delayLevel > 0) {
            rocketMQTemplate.syncSend(message.getTopic(), springMessage, 10000, delayLevel);
        } else {
            rocketMQTemplate.syncSend(message.getTopic(), springMessage, 10000);
        }

        return Result.success();
    }

    @Override
    public void sendAsync(Message message, ResultVO callback) {
    }

    @Override
    public void sendOneWay(Message message) {
    }
}

