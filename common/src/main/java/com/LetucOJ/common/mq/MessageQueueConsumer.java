package com.LetucOJ.common.mq;

import com.LetucOJ.common.mq.impl.Message;

public interface MessageQueueConsumer {
    void registerListener(String topic, String tag, MessageListener listener);

    @FunctionalInterface
    interface MessageListener {
        boolean consume(Message message);
    }
}
