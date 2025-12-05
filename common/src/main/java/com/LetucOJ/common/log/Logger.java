package com.LetucOJ.common.log;

import java.time.format.DateTimeFormatter;
import com.LetucOJ.common.mq.MessageQueueProducer;
import com.LetucOJ.common.mq.impl.Message;
import com.LetucOJ.common.trace.TraceContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;

@Component
@Data
@AllArgsConstructor
public class Logger {

    private static MessageQueueProducer staticMessageQueueProducer;

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
        Message message = new Message("log", "log", TraceContext.getTraceId(), payload, time(), 0);
        System.out.println("send: " + message);
        staticMessageQueueProducer.send(message);
    }

    private static String time() {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(java.time.LocalDateTime.now());
    }
}