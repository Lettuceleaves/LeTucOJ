package com.LetucOJ.common.anno;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.LetucOJ.common.log.LogLevel;
import com.LetucOJ.common.log.Logger;
import com.LetucOJ.common.log.Type;
import com.LetucOJ.common.mq.impl.Message;
import lombok.AllArgsConstructor;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(name = "mq.type", havingValue = "rocketmq")
@RocketMQMessageListener(
        topic = "limit",
        consumerGroup = "limit",
        consumeMode = ConsumeMode.ORDERLY
)
@AllArgsConstructor
public class LimitMQConsumer implements RocketMQListener<Message> {

    private StringRedisTemplate redisTemplate;

    public static final String LUA_CLEAR = """
            local status = redis.call('GET', KEYS[1])
            local mem =  ARGV[1]
            redis.call('DEL', KEYS[1])
            if status == 'COMMITING' then
                redis.call('DECRBY', 'used_memory', mem)
            end
            """;

    @Override
    public void onMessage(Message message) {
        JSONObject bodyJson = JSONUtil.parseObj(message.getBody());
        Logger.log(Type.SERVER, LogLevel.INFO, bodyJson.getStr("submitKey"));
        redisTemplate.execute(
                new DefaultRedisScript<>(LUA_CLEAR, Long.class),
                List.of(bodyJson.getStr("submitKey")),
                bodyJson.getStr("mem"));
    }
}