package com.LetucOJ.common.mq.impl;

import com.LetucOJ.common.result.Result;
import com.LetucOJ.common.result.errorcode.BaseErrorCode;
import com.alibaba.fastjson.JSON;
import com.LetucOJ.common.cache.Redis;
import com.LetucOJ.common.mq.MessageQueueProducer;
import com.LetucOJ.common.result.ResultVO;
import org.springframework.stereotype.Component;

@Component
public class RedisMQProducer implements MessageQueueProducer {

    @Override
    public ResultVO<Void> send(Message message) {
        try {
            String messageJson = JSON.toJSONString(message);
            boolean result = Redis.listPush(message.getTopic(), messageJson);
            if (result) {
                return Result.success();
            } else {
                return Result.failure(BaseErrorCode.LOG_ERROR);
            }
        } catch (Exception e) {
            return Result.failure(BaseErrorCode.LOG_ERROR);
        }
    }

    @Override
    public void sendAsync(Message message, ResultVO<Void> callback) {
        new Thread(() -> {
            try {
                String messageJson = JSON.toJSONString(message);
                Redis.listPush(message.getTopic(), messageJson);
            } catch (Exception e) {
                Result.failure(BaseErrorCode.LOG_ERROR, e);
            }
        }).start();
    }

    @Override
    public void sendOneWay(Message message) {
        try {
            String messageJson = JSON.toJSONString(message);
            Redis.listPush(message.getTopic(), messageJson);
        } catch (Exception e) {
            Result.failure(BaseErrorCode.LOG_ERROR, e);
        }
    }
}
