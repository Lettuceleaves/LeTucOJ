package com.LetucOJ.common.log;

import java.time.format.DateTimeFormatter;
import com.LetucOJ.common.mq.MessageQueueProducer;
import com.LetucOJ.common.mq.impl.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct; // 导入 @PostConstruct

@Component
public class Logger {

    private static MessageQueueProducer staticMessageQueueProducer;

    @Autowired
    private MessageQueueProducer messageQueueProducer;

    @PostConstruct
    public void init() {
        Logger.staticMessageQueueProducer = this.messageQueueProducer;
    }

    public static void log(Type type, LogLevel level, String info) {
        if (staticMessageQueueProducer == null) {
            System.err.println("Log system not initialized. Message lost: " + info);
            return;
        }

        String payload = "[" + level.message() + ": " + type.message() + "] " + "(" + time() + ")" + " " + info;
        Message message = new Message("log", "log", "0", payload, time(), 0);
        System.out.println("send: " + message);
        staticMessageQueueProducer.send(message);
    }

    private static String time() {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(java.time.LocalDateTime.now());
    }
}