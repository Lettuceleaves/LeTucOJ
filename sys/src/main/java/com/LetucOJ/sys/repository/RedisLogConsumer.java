package com.LetucOJ.sys.repository;

import cn.hutool.core.util.IdUtil;
import com.LetucOJ.common.log.LogLevel;
import com.LetucOJ.common.log.Logger;
import com.LetucOJ.common.log.Type;
import com.alibaba.fastjson.JSON;
import com.LetucOJ.common.cache.Redis;
import com.LetucOJ.common.mq.impl.Message;
import com.LetucOJ.sys.model.Log;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RedisLogConsumer {

    @Resource
    private MybatisRepos mybatisRepos;

    @Scheduled(fixedRate = 1000) // 每秒检查一次
    public void consumeLogMessages() {
        try {
            String messageJson = Redis.listPop("log");
            while (messageJson != null) {
                Message message = JSON.parseObject(messageJson, Message.class);
                mybatisRepos.appendLog(new Log(IdUtil.getSnowflake().nextIdStr(), message.getKey(), message.getBody()));
                messageJson = Redis.listPop("log");
            }
        } catch (Exception e) {
            Logger.log(Type.SERVER, LogLevel.ERROR, e.getMessage());
        }
    }
}
