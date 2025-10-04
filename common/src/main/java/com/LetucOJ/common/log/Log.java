package com.LetucOJ.common.log;

import java.time.format.DateTimeFormatter;

import com.LetucOJ.common.mq.MessageQueueProducer;
import com.LetucOJ.common.mq.impl.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Log {

    @Autowired
    private MessageQueueProducer messageQueueProducer;

    public void sys(SysLogType type, LogLevel level, String info) {
        String payload = "[" + level.message() + ": " + type.message() + "] " + "(" + time() + ")" + " " + info;
        Message message = new Message("log", "sys", "0", payload, time(), 0);
        messageQueueProducer.send(message);
    }

    public void judge(JudgeLogType type, LogLevel level,  String info) {
        String payload = "[" + level.message() + ": " + type.message() + "] " + "(" + time() + ")" + " " + info;
        Message message = new Message("log", "judge", "0", payload, time(), 0);
        messageQueueProducer.send(message);
    }

    private static String time() {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(java.time.LocalDateTime.now());
    }
}
